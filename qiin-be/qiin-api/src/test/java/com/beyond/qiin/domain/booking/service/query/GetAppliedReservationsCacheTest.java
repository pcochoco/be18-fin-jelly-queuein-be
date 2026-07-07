package com.beyond.qiin.domain.booking.service.query;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.beyond.qiin.common.dto.PageResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.request.search_condition.GetAppliedReservationSearchCondition;
import com.beyond.qiin.domain.booking.dto.reservation.response.applied_reservation.GetAppliedReservationResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawAppliedReservationResponseDto;
import com.beyond.qiin.domain.booking.repository.ReservationSlotJpaRepository;
import com.beyond.qiin.domain.booking.repository.querydsl.AppliedReservationsQueryRepository;
import com.beyond.qiin.domain.booking.repository.querydsl.ReservationQueryRepository;
import com.beyond.qiin.domain.booking.repository.querydsl.UserReservationsQueryRepository;
import com.beyond.qiin.domain.booking.support.ReservationReader;
import com.beyond.qiin.domain.iam.entity.User;
import com.beyond.qiin.domain.iam.support.user.UserReader;
import com.beyond.qiin.domain.inventory.repository.querydsl.AssetQueryRepository;
import com.beyond.qiin.domain.inventory.service.query.AssetQueryService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = {
            ReservationQueryServiceImpl.class,
            com.beyond.qiin.domain.booking.support.AppliedReservationCachePolicy.class,
            GetAppliedReservationsCacheTest.CacheTestConfig.class
        })
public class GetAppliedReservationsCacheTest {

    @Autowired
    private ReservationQueryService reservationQueryService;

    @Autowired
    private CacheManager cacheManager;

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
    private AssetQueryRepository assetQueryRepository;

    @MockitoBean
    private ReservationQueryRepository reservationQueryRepository;

    @MockitoBean
    private ReservationSlotJpaRepository reservationSlotJpaRepository;

    @Configuration
    @EnableCaching
    static class CacheTestConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("appliedReservations");
        }
    }

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache("appliedReservations");
        if (cache != null) {
            cache.clear();
        }
    }

    @DisplayName("같은 조건으로 2번 호출하면 두 번째는 캐시를 사용한다")
    @Test
    void getReservationApplies_should_use_cache_on_second_call() {
        // given
        Long userId = 1L;

        GetAppliedReservationSearchCondition condition = new GetAppliedReservationSearchCondition();
        condition.setReservationStatus("PENDING");
        condition.setApplicantName(null);
        condition.setAssetName(null);
        condition.setCategoryId(null);
        condition.setStartDate(null);
        condition.setEndDate(null);

        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        User mockUser = mock(User.class);
        given(userReader.findById(userId)).willReturn(mockUser);

        RawAppliedReservationResponseDto rawDto = mock(RawAppliedReservationResponseDto.class);
        given(rawDto.getAssetId()).willReturn(100L);
        given(rawDto.getReservationId()).willReturn(200L);
        given(rawDto.getStartAt()).willReturn(Instant.parse("2025-12-04T10:00:00Z"));
        given(rawDto.getEndAt()).willReturn(Instant.parse("2025-12-04T12:00:00Z"));

        Page<RawAppliedReservationResponseDto> rawPage = new PageImpl<>(java.util.List.of(rawDto), pageable, 1);

        given(appliedReservationsQueryRepository.search(any(), any())).willReturn(rawPage);
        given(reservationSlotJpaRepository.findAllForReservability(any(), any(), any()))
                .willReturn(List.of());
        given(assetQueryService.isAvailable(any())).willReturn(true);

        // when
        PageResponseDto<GetAppliedReservationResponseDto> first =
                reservationQueryService.getReservationApplies(userId, condition, pageable);

        PageResponseDto<GetAppliedReservationResponseDto> second =
                reservationQueryService.getReservationApplies(userId, condition, pageable);

        // then
        verify(appliedReservationsQueryRepository, times(1)).search(any(), any());
        verify(userReader, times(1)).findById(userId);
    }
}
