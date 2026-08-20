package com.beyond.qiin.infra.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerErrorHandlerConfig {

    private static final long RETRY_INTERVAL_MS = 1000L;
    private static final long RETRY_COUNT = 2L;
    private static final String RESERVATION_CONSUMER_TAG = "reservation";

    @Bean
    public CommonErrorHandler kafkaCommonErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate, KafkaDltMetrics kafkaDltMetrics) {
        DeadLetterPublishingRecoverer recoverer = deadLetterPublishingRecoverer(kafkaTemplate, kafkaDltMetrics);

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(recoverer, new FixedBackOff(RETRY_INTERVAL_MS, RETRY_COUNT));
        errorHandler.addNotRetryableExceptions(JsonProcessingException.class); //json processing exception인 경우 재시도 x

        //실패가 발생했을 때 그 실패 이벤트를 통지받는 콜백을 등록하는 것 -> 로그 출력
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> log.warn(
                "Kafka consumer processing failed: topic={}, partition={}, offset={}, deliveryAttempt={}, exception={}",
                record.topic(),
                record.partition(),
                record.offset(),
                deliveryAttempt,
                ex.getClass().getName(),
                ex));
        return errorHandler;
    }

    //package private
    DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<Object, Object> kafkaTemplate, KafkaDltMetrics kafkaDltMetrics) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate, (record, ex) -> {
                    TopicPartition dlt = new TopicPartition(record.topic() + ".DLT", record.partition());
                    log.error(
                            "Kafka DLT publish: topic={}, partition={}, offset={}, dltTopic={}, dltPartition={}, exception={}",
                            record.topic(),
                            record.partition(),
                            record.offset(),
                            dlt.topic(),
                            dlt.partition(),
                            ex.getClass().getName(), //ex : 발생한 예외의 정확한 클래스 이름
                            ex);
                    return dlt;
                }) {
                    @Override
                    protected void verifySendResult(
                            KafkaOperations<Object, Object> kafkaOperations,
                            ProducerRecord<Object, Object> outRecord,
                            CompletableFuture<SendResult<Object, Object>> sendResult,
                            ConsumerRecord<?, ?> inRecord) {
                        //dlt로 전달을 대기, 실패 시 예외
                        super.verifySendResult(kafkaOperations, outRecord, sendResult, inRecord);

                        //dlt 전달 성공 시 counter +1
                        kafkaDltMetrics.incrementPublished(outRecord.topic(), RESERVATION_CONSUMER_TAG);
                    }
                };
        return recoverer;
    }
}
