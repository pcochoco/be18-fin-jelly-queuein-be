package com.beyond.qiin.infra.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

class KafkaDltMetricsTest {


    //application이 prometheus가 읽을 수 있는 형태로 지표 노출하는지 확인용
    @Test
    void dltPublishedCounterIsExposedWithPrometheusName() {
        //meter registry : micrometer의 지표 수집 및 관리 저장소 (prometheus 형식으로 내보냄)
        PrometheusMeterRegistry meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        KafkaDltMetrics kafkaDltMetrics = new KafkaDltMetrics(meterRegistry);

        kafkaDltMetrics.incrementPublished("reservation-event-topic.DLT", "reservation");

        assertThat(meterRegistry.scrape())
                .contains("kafka_dlt_published_total")
                .contains("consumer=\"reservation\"")
                .contains("topic=\"reservation-event-topic.DLT\"");
    }

    @Test
    void dltPublishedCounterIsNotIncreasedWhenDltPublishFails() {

        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

        KafkaDltMetrics kafkaDltMetrics = new KafkaDltMetrics(meterRegistry);

        KafkaTemplate<Object, Object> kafkaTemplate = mock(KafkaTemplate.class);

        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("dlt publish failure")));

        DeadLetterPublishingRecoverer recoverer =
                new KafkaConsumerErrorHandlerConfig().deadLetterPublishingRecoverer(kafkaTemplate, kafkaDltMetrics);
        //DI : new KafkaConsumerErrorHandlerConfig(), 만들어진 빈의 주입 (Configuration, Bean으로 선언 - Bean 메서드의 반환값)


        recoverer.setVerifyPartition(false);

        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("reservation-event-topic", 0, 1L, "reservation-1", "{\"reservationId\":1}");

        assertThatThrownBy(() -> recoverer.accept(record, null, new RuntimeException("consumer failure")))
                .hasMessageContaining("Dead-letter publication to reservation-event-topic.DLT failed");

        assertThat(meterRegistry
                        .counter(
                                KafkaDltMetrics.DLT_PUBLISHED_COUNTER,
                                "topic",
                                "reservation-event-topic.DLT",
                                "consumer",
                                "reservation")
                        .count())
                .isZero();
    }
}
