package com.beyond.qiin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafka
@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
@EnableScheduling
@EnableRetry
public class QiinApplication {

    public static void main(String[] args) {
        SpringApplication.run(QiinApplication.class, args);
    }
}
