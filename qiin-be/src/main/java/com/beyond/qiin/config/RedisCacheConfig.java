package com.beyond.qiin.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisSerializer<String> keySerializer = new StringRedisSerializer();

        // value는 list, dto 등 다양한 type 가능하므로 Object
        RedisSerializer<Object> valueSerializer =
                new GenericJackson2JsonRedisSerializer(); // 객체를 json으로 바꾸고 class 정보 저장

        // 공통 config 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(5)); // 기본 5분

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 신청 예약 목록 조회용 캐시
        cacheConfigurations.put("appliedReservations", defaultConfig.entryTtl(Duration.ofSeconds(60))); //1분 ttl 

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig) // default config 추가
                .withInitialCacheConfigurations(cacheConfigurations) // 신청 예약 목록용 캐시 추가
                .transactionAware() // 트랜잭션 커밋 성공 후 캐시에 반영
                .build();
    }
}
