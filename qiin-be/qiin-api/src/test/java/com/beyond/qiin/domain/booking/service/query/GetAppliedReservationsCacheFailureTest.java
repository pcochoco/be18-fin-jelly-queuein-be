package com.beyond.qiin.domain.booking.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.beyond.qiin.common.dto.PageResponseDto;
import com.beyond.qiin.config.RedisCacheConfig;
import com.beyond.qiin.domain.booking.dto.reservation.request.search_condition.GetAppliedReservationSearchCondition;
import com.beyond.qiin.domain.booking.dto.reservation.response.applied_reservation.GetAppliedReservationResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawAppliedReservationResponseDto;
import com.beyond.qiin.domain.booking.repository.ReservationSlotJpaRepository;
import com.beyond.qiin.domain.booking.repository.querydsl.AppliedReservationsQueryRepository;
import com.beyond.qiin.domain.booking.repository.querydsl.UserReservationsQueryRepository;
import com.beyond.qiin.domain.booking.support.AppliedReservationCachePolicy;
import com.beyond.qiin.domain.booking.support.ReservationReader;
import com.beyond.qiin.domain.iam.entity.User;
import com.beyond.qiin.domain.iam.support.user.UserReader;
import com.beyond.qiin.domain.inventory.service.query.AssetQueryService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(
        classes = {
            ReservationQueryServiceImpl.class,
            AppliedReservationCachePolicy.class,
            RedisCacheConfig.class,
            GetAppliedReservationsCacheFailureTest.CacheFailureTestConfig.class
        })
class GetAppliedReservationsCacheFailureTest {

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("test.redis.host", redis::getHost);
        registry.add("test.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private ReservationQueryService reservationQueryService;

    // MockitoSpyBean : application context에 이미 있는 실제 bean을 mockito spy로 감싸줌
    // 실제 CacheErrorHandler을 유지하면서 정말 호출되었는지에 대해 verify
    @MockitoSpyBean
    private CacheErrorHandler cacheErrorHandler;

    // mock 객체 - 실제 db 호출 x
    @MockitoBean
    private AppliedReservationsQueryRepository appliedReservationsQueryRepository;

    @MockitoBean
    private UserReader userReader;

    @MockitoBean
    private ReservationReader reservationReader;

    @MockitoBean
    private AssetQueryService assetQueryService;

    @MockitoBean
    private UserReservationsQueryRepository userReservationsQueryRepository;

    @MockitoBean
    private ReservationSlotJpaRepository reservationSlotJpaRepository;

    @DisplayName("Redis 장애 시 CacheErrorHandler가 예외를 삼키고 실제 조회 로직 fallback으로 요청을 정상 처리한다")
    @Test
    void getReservationApplies_should_fallback_to_actual_query_when_redis_cache_fails() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        GetAppliedReservationSearchCondition condition = new GetAppliedReservationSearchCondition();
        condition.setReservationStatus("PENDING");

        given(userReader.findById(userId)).willReturn(mock(User.class));

        RawAppliedReservationResponseDto rawDto = mock(RawAppliedReservationResponseDto.class);
        given(rawDto.getAssetId()).willReturn(100L);
        given(rawDto.getStartAt()).willReturn(Instant.parse("2025-12-04T10:00:00Z"));
        given(rawDto.getEndAt()).willReturn(Instant.parse("2025-12-04T12:00:00Z"));

        Page<RawAppliedReservationResponseDto> rawPage = new PageImpl<>(List.of(rawDto), pageable, 1);
        given(appliedReservationsQueryRepository.search(any(), eq(pageable))).willReturn(rawPage);
        given(reservationSlotJpaRepository.findAllForReservability(any(), any(), any()))
                .willReturn(List.of());

        redis.stop();

        PageResponseDto<GetAppliedReservationResponseDto> result =
                reservationQueryService.getReservationApplies(userId, condition, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(cacheErrorHandler, atLeastOnce())
                .handleCacheGetError(any(RuntimeException.class), any(Cache.class), any());
        verify(userReader, times(1)).findById(userId);
        verify(appliedReservationsQueryRepository, times(1)).search(any(), eq(pageable));
    }

    @TestConfiguration
    @EnableCaching
    static class CacheFailureTestConfig {

        @Bean(destroyMethod = "destroy")
        RedisConnectionFactory redisConnectionFactory(
                @Value("${test.redis.host}") String host, @Value("${test.redis.port}") int port) {
            return new LettuceConnectionFactory(host, port);
        }
    }
}
