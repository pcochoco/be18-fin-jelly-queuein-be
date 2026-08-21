package com.beyond.qiin.infra.kafka;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaDltMetrics {

    public static final String DLT_PUBLISHED_COUNTER = "kafka.dlt.published";

    private static final String TOPIC_TAG = "topic";
    private static final String CONSUMER_TAG = "consumer";

    // metric registry : metric 관리 객체 (micrometer : metric 수집용 계층)
    private final MeterRegistry meterRegistry;

    public void incrementPublished(String topic, String consumer) {
        meterRegistry
                .counter(DLT_PUBLISHED_COUNTER, TOPIC_TAG, topic, CONSUMER_TAG, consumer)
                .increment();
    }
}
