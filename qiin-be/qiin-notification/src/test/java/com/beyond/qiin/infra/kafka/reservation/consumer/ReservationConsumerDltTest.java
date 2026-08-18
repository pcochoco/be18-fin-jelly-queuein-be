package com.beyond.qiin.infra.kafka.reservation.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.beyond.qiin.domain.alarm.service.NotificationCommandService;
import com.beyond.qiin.infra.kafka.KafkaConsumerErrorHandlerConfig;
import com.beyond.qiin.infra.kafka.reservation.event.ReservationEventPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@EmbeddedKafka(
        partitions = 3,
        topics = {ReservationConsumerDltTest.ORIGINAL_TOPIC, ReservationConsumerDltTest.DLT_TOPIC})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationConsumerDltTest {

    static final String ORIGINAL_TOPIC = "reservation-event-topic";
    static final String DLT_TOPIC = "reservation-event-topic.DLT";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<KafkaMessageListenerContainer<String, String>> containers = new ArrayList<>();
    private final List<Consumer<String, String>> consumers = new ArrayList<>();

    @AfterEach
    void tearDown() {
        containers.forEach(KafkaMessageListenerContainer::stop);
        consumers.forEach(Consumer::close);
    }

    @Test
    void normalMessageIsProcessedOnceAndNotSentToDlt() throws Exception {
        NotificationCommandService notificationService = mock(NotificationCommandService.class);
        AtomicInteger listenerAttempts = new AtomicInteger();
        startReservationContainer(notificationService, listenerAttempts);

        String payload = validPayload("normal");
        send(payload);

        verify(notificationService, timeout(5_000).times(1)).notifyEvent(any(ReservationEventPayload.class));
        assertThat(listenerAttempts.get()).isEqualTo(1);

        ConsumerRecords<String, String> dltRecords = KafkaTestUtils.getRecords(dltConsumer(), Duration.ofSeconds(2));
        assertThat(dltRecords.count()).isZero();
    }

    @Test
    void retryableFailureIsRetriedTwiceAndThenSentToDltWithOriginalPayload() throws Exception {
        NotificationCommandService notificationService = mock(NotificationCommandService.class);
        org.mockito.Mockito.doThrow(new RuntimeException("temporary db failure"))
                .when(notificationService)
                .notifyEvent(any(ReservationEventPayload.class));
        AtomicInteger listenerAttempts = new AtomicInteger();
        startReservationContainer(notificationService, listenerAttempts);

        String payload = validPayload("retryable");
        send(payload);

        ConsumerRecord<String, String> dltRecord =
                KafkaTestUtils.getSingleRecord(dltConsumer(), DLT_TOPIC, Duration.ofSeconds(10));

        verify(notificationService, timeout(5_000).times(3)).notifyEvent(any(ReservationEventPayload.class));
        assertThat(listenerAttempts.get()).isEqualTo(3);
        assertThat(dltRecord.value()).isEqualTo(payload);
        assertThat(dltRecord.topic()).isEqualTo(DLT_TOPIC);
    }

    @Test
    void malformedMessageIsNotRetriedAndSentToDltWithOriginalPayload() throws Exception {
        NotificationCommandService notificationService = mock(NotificationCommandService.class);
        AtomicInteger listenerAttempts = new AtomicInteger();
        startReservationContainer(notificationService, listenerAttempts);

        String malformedPayload = "{ invalid-json";
        send(malformedPayload);

        ConsumerRecord<String, String> dltRecord =
                KafkaTestUtils.getSingleRecord(dltConsumer(), DLT_TOPIC, Duration.ofSeconds(10));

        verify(notificationService, never()).notifyEvent(any(ReservationEventPayload.class));
        assertThat(listenerAttempts.get()).isEqualTo(1);
        assertThat(dltRecord.value()).isEqualTo(malformedPayload);
    }

    private void startReservationContainer(
            NotificationCommandService notificationService, AtomicInteger listenerAttempts) {
        ReservationConsumer reservationConsumer = new ReservationConsumer(notificationService, objectMapper);

        ContainerProperties containerProperties = new ContainerProperties(ORIGINAL_TOPIC);
        containerProperties.setGroupId("reservation-test-group-" + UUID.randomUUID());
        containerProperties.setMessageListener((MessageListener<String, String>) record -> {
            listenerAttempts.incrementAndGet();
            try {
                reservationConsumer.onEvent(record.value());
            } catch (JsonProcessingException e) {
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
        return new KafkaConsumerErrorHandlerConfig().kafkaCommonErrorHandler(kafkaTemplate());
    }

    private KafkaTemplate<Object, Object> kafkaTemplate() {
        Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
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

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable throwable) throws E {
        throw (E) throwable;
    }
}
