package com.beyond.qiin.infra.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerErrorHandlerConfig {

    private static final long RETRY_INTERVAL_MS = 1000L;
    private static final long RETRY_COUNT = 2L;

    @Bean
    public CommonErrorHandler kafkaCommonErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, (record, ex) -> {
            TopicPartition dlt = new TopicPartition(record.topic() + ".DLT", record.partition());
            log.error(
                    "Kafka DLT publish: topic={}, partition={}, offset={}, dltTopic={}, dltPartition={}, exception={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    dlt.topic(),
                    dlt.partition(),
                    ex.getClass().getName(),
                    ex);
            return dlt;
        });

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(recoverer, new FixedBackOff(RETRY_INTERVAL_MS, RETRY_COUNT));
        errorHandler.addNotRetryableExceptions(JsonProcessingException.class);
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
}
