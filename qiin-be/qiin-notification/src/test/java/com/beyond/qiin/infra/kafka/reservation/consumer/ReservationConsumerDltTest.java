package com.beyond.qiin.infra.kafka.reservation.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.beyond.qiin.domain.alarm.service.NotificationCommandService;
import com.beyond.qiin.infra.kafka.KafkaConsumerErrorHandlerConfig;
import com.beyond.qiin.infra.kafka.KafkaDltMetrics;
import com.beyond.qiin.infra.kafka.reservation.event.ReservationEventPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
//test 용 kafka container
@EmbeddedKafka(
        partitions = 3,
        topics = {ReservationConsumerDltTest.ORIGINAL_TOPIC, ReservationConsumerDltTest.DLT_TOPIC})
//concurrency : 동시에 consumer 여럿을 돌림 (병렬 작업을 의미)
//spring test application context를 버리고 다음 test에서 새로 만들어줌
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationConsumerDltTest {

    static final String ORIGINAL_TOPIC = "reservation-event-topic";
    static final String DLT_TOPIC = "reservation-event-topic.DLT";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker; //producer, consumer 사이에 topic 에 메시지 저장, 전달해주는 중간의 서버 s

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final KafkaDltMetrics kafkaDltMetrics = new KafkaDltMetrics(meterRegistry);
    private final List<KafkaMessageListenerContainer<String, String>> containers = new ArrayList<>();
    private final List<Consumer<String, String>> consumers = new ArrayList<>();

    @AfterEach
    void tearDown() {
        containers.forEach(KafkaMessageListenerContainer::stop);
        consumers.forEach(Consumer::close); //한꺼번에 정리하는 용도로 consumers로 묶어 관리
    }

    //정상 메시지는 dlt로 x
    @Test
    void normalMessageIsProcessedOnceAndNotSentToDlt() throws Exception {
        NotificationCommandService notificationService = mock(NotificationCommandService.class);
        //atomic integer : 여러 스레드에서 동시에 값을 바꿔도 안전하게 증가시키기 위한 정수 객체 (listener thread, test thread)
        AtomicInteger listenerAttempts = new AtomicInteger(); //컨슈머가 메시지를 몇번 받았는지에 대한 횟수

        startReservationContainer(notificationService, listenerAttempts);

        String payload = validPayload("normal");
        send(payload);

        //알림 생성 및 전달 1qjs
        verify(notificationService, timeout(5_000).times(1)).notifyEvent(any(ReservationEventPayload.class));
        assertThat(listenerAttempts.get()).isEqualTo(1);

        ConsumerRecords<String, String> dltRecords = KafkaTestUtils.getRecords(dltConsumer(), Duration.ofSeconds(2));
        assertThat(dltRecords.count()).isZero(); //dlt에 전달된 메시지 x
        assertDltPublishSuccessCount(DLT_TOPIC, 0.0);
    }

    @Test
    void retryableFailureIsRetriedTwiceAndThenSentToDltWithOriginalPayload() throws Exception {
        NotificationCommandService notificationService = mock(NotificationCommandService.class);
        org.mockito.Mockito.doThrow(new RuntimeException("temporary db failure")) //notify event 호출 시 실패
                .when(notificationService)
                .notifyEvent(any(ReservationEventPayload.class));
        AtomicInteger listenerAttempts = new AtomicInteger();
        startReservationContainer(notificationService, listenerAttempts);

        String payload = validPayload("retryable");
        send(payload);

        //DLT_TOPIC에 메시지가 들어왔는지 10초간 기다린 후 해당 메시지를 저장
        ConsumerRecord<String, String> dltRecord =
                KafkaTestUtils.getSingleRecord(dltConsumer(), DLT_TOPIC, Duration.ofSeconds(10));

        verify(notificationService, timeout(5_000).times(3)).notifyEvent(any(ReservationEventPayload.class));
        assertThat(listenerAttempts.get()).isEqualTo(3);
        assertThat(dltRecord.value()).isEqualTo(payload);
        assertThat(dltRecord.topic()).isEqualTo(DLT_TOPIC);
        assertDltPublishSuccessCount(DLT_TOPIC, 1.0);
    }

    @Test
    void retryableFailureRecoveredOnRetryIsNotSentToDltAndDoesNotIncreaseCounter() throws Exception {
        NotificationCommandService notificationService = mock(NotificationCommandService.class);
        org.mockito.Mockito.doThrow(new RuntimeException("temporary db failure"))
                .doThrow(new RuntimeException("temporary db failure"))
                .doNothing()
                .when(notificationService)
                .notifyEvent(any(ReservationEventPayload.class));
        AtomicInteger listenerAttempts = new AtomicInteger();
        startReservationContainer(notificationService, listenerAttempts);

        String payload = validPayload("recovered"); //실패 후 성공
        send(payload);

        verify(notificationService, timeout(5_000).times(3)).notifyEvent(any(ReservationEventPayload.class));

        assertThat(listenerAttempts.get()).isEqualTo(3);
        ConsumerRecords<String, String> dltRecords = KafkaTestUtils.getRecords(dltConsumer(), Duration.ofSeconds(2));
        assertThat(dltRecords.count()).isZero();
        assertDltPublishSuccessCount(DLT_TOPIC, 0.0);
    }

    @Test
    void malformedMessageIsNotRetriedAndSentToDltWithOriginalPayload() throws Exception {
        NotificationCommandService notificationService = mock(NotificationCommandService.class);
        AtomicInteger listenerAttempts = new AtomicInteger();
        startReservationContainer(notificationService, listenerAttempts);

        String malformedPayload = "{ invalid-json"; //} 생략에 의한 문법 예외
        send(malformedPayload);

        ConsumerRecord<String, String> dltRecord =
                KafkaTestUtils.getSingleRecord(dltConsumer(), DLT_TOPIC, Duration.ofSeconds(10));

        verify(notificationService, never()).notifyEvent(any(ReservationEventPayload.class));
        assertThat(listenerAttempts.get()).isEqualTo(1);
        assertThat(dltRecord.value()).isEqualTo(malformedPayload);
        assertDltPublishSuccessCount(DLT_TOPIC, 1.0);
    }

    private void startReservationContainer(
            NotificationCommandService notificationService, AtomicInteger listenerAttempts) {
        ReservationConsumer reservationConsumer = new ReservationConsumer(notificationService, objectMapper);

        ContainerProperties containerProperties = new ContainerProperties(ORIGINAL_TOPIC);
        containerProperties.setGroupId("reservation-test-group-" + UUID.randomUUID()); //계속 새로운 consumer로 테스트
        containerProperties.setMessageListener((MessageListener<String, String>) record -> {
            listenerAttempts.incrementAndGet();
            try {
                reservationConsumer.onEvent(record.value()); //json processing exception error 의 compile error 없이
            } catch (JsonProcessingException e) { //바깥으로 던짐 == default error handler이 consumer 처리 실패 감지하도록
                sneakyThrow(e);
            }
        });

        KafkaMessageListenerContainer<String, String> container =
                new KafkaMessageListenerContainer<>(consumerFactory(), containerProperties);
        container.setCommonErrorHandler(commonErrorHandler());
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
        containers.add(container);
    }

    private CommonErrorHandler commonErrorHandler() {
        return new KafkaConsumerErrorHandlerConfig().kafkaCommonErrorHandler(kafkaTemplate(), kafkaDltMetrics);
    }

    //test용 kafka prodcuer을 다루기 위한 kafka template
    private KafkaTemplate<Object, Object> kafkaTemplate() {
        Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //kafka producer factory 로 kafka template을 넘김
        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
        kafkaTemplate.setDefaultTopic(ORIGINAL_TOPIC);
        return kafkaTemplate;
    }

    private DefaultKafkaConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "false", embeddedKafkaBroker),
                new StringDeserializer(),
                new StringDeserializer());
    }

    //dlt topic을 읽는 consumer 생성
    private Consumer<String, String> dltConsumer() {
        Consumer<String, String> consumer = consumerFactory().createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, DLT_TOPIC);
        consumers.add(consumer);
        return consumer;
    }

    private void send(String payload)
            throws ExecutionException, InterruptedException, java.util.concurrent.TimeoutException {
        kafkaTemplate().send(ORIGINAL_TOPIC, payload).get(10, TimeUnit.SECONDS);
    }

    private String validPayload(String suffix) {
        return """
                {
                  "eventType": "%s",
                  "reservationId": 1,
                  "assetId": 2,
                  "applicantId": 3,
                  "respondentId": 4,
                  "startAt": "2026-08-18T10:00:00",
                  "endAt": "2026-08-18T11:00:00",
                  "status": "CONFIRMED",
                  "attendantUserIds": [3, 4]
                }
                """
                .formatted(suffix);
    }

    private void assertDltPublishSuccessCount(String topic, double expectedCount) {
        assertThat(meterRegistry
                        .counter(KafkaDltMetrics.DLT_PUBLISHED_COUNTER, "topic", topic, "consumer", "reservation")
                        .count())
                .isEqualTo(expectedCount);
    }

    //checked exception인 json processing exception 을 명시적으로 선언하지 않고 던지는 용도
    @SuppressWarnings("unchecked") //unchecked cast를 숨김 : throw (E) throwable 에 의한
    private static <E extends Throwable> void sneakyThrow(Throwable throwable) throws E {
        throw (E) throwable; //generic type으로 casting 해서 compiler check 우회
    }
}
