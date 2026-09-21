package com.beyond.qiin.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ClientOptions.DisconnectedBehavior;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
@Profile({"dev", "prod", "staging"})
public class RedisConfig {

    private static final Duration REDIS_CONNECT_TIMEOUT = Duration.ofMillis(500);
    private static final Duration REDIS_COMMAND_TIMEOUT = Duration.ofMillis(500);

    @Value("${REDIS_HOST}")
    private String host;

    @Value("${REDIS_PORT}")
    private int port;

    /**
     * Cache와 StringRedisTemplate(JWT blacklist)가 같은 Redis endpoint와 connection policy를 사용한다.
     * Redis command는 재시도하지 않는다. 장애 시 각 호출은 최대 500ms 안에 실패해야 한다.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return createConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // Redis에 데이터를 저장하고 조회, 삭제하는 빈을 생성
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @PostConstruct
    public void verifyRedisConnection() {
        LettuceConnectionFactory tempFactory = createConnectionFactory();
        tempFactory.afterPropertiesSet(); // 팩토리 초기화

        try (RedisConnection connection = tempFactory.getConnection()) {
            String pong = connection.ping();
            log.info("Redis 연결 성공: {}:{}, 응답 = {}", host, port, pong);
        } catch (Exception ex) {
            log.error("Redis 연결 실패: {}:{}, 에러={}", host, port, ex.getMessage());
        } finally {
            // 임시로 생성한 팩토리를 명시적으로 종료합니다.
            tempFactory.destroy();
        }
    }

    private LettuceConnectionFactory createConnectionFactory() {
        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration(host, port);
        ClientOptions clientOptions = ClientOptions.builder()
                // Reconnect 자체는 유지하되, 끊긴 동안 command를 큐잉/재전송하지 않는다.
                .disconnectedBehavior(DisconnectedBehavior.REJECT_COMMANDS)
                .socketOptions(SocketOptions.builder()
                        .connectTimeout(REDIS_CONNECT_TIMEOUT)
                        .build())
                .timeoutOptions(TimeoutOptions.enabled())
                .build();
        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .commandTimeout(REDIS_COMMAND_TIMEOUT)
                .clientOptions(clientOptions)
                .build();

        return new LettuceConnectionFactory(redisConfiguration, clientConfiguration);
    }
}
