package com.beyond.qiin.domain.booking.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.qiin.domain.booking.dto.reservation.response.asset_time.AssetTimeResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawReservationSlotResponseDto;
import com.beyond.qiin.domain.booking.repository.ReservationSlotJpaRepository;
import com.beyond.qiin.domain.booking.repository.querydsl.AppliedReservationsQueryRepository;
import com.beyond.qiin.domain.booking.repository.querydsl.UserReservationsQueryRepository;
import com.beyond.qiin.domain.booking.support.ReservationReader;
import com.beyond.qiin.domain.iam.support.user.UserReader;
import com.beyond.qiin.domain.inventory.service.query.AssetQueryService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAssetTimesTest {

    @InjectMocks
    private ReservationQueryServiceImpl reservationQueryService;

    @Mock
    private UserReader userReader;

    @Mock
    private ReservationReader reservationReader;

    @Mock
    private AssetQueryService assetQueryService;

    @Mock
    private UserReservationsQueryRepository userReservationsQueryRepository;

    @Mock
    private AppliedReservationsQueryRepository appliedReservationsQueryRepository;

    @Mock
    private ReservationSlotJpaRepository reservationSlotJpaRepository;

    @Test
    void getAssetTimes_marksOccupiedSlotUnavailable() {
        Long userId = 1L;
        Long assetId = 100L;
        LocalDate date = LocalDate.of(2025, 12, 4);
        ZoneId zone = ZoneId.of("Asia/Seoul");
        Instant occupiedStart = date.atTime(9, 0).atZone(zone).toInstant();

        when(reservationSlotJpaRepository.findAllByAssetAndDate(
                        eq(assetId),
                        eq(date.atStartOfDay(zone).toInstant()),
                        eq(date.plusDays(1).atStartOfDay(zone).toInstant())))
                .thenReturn(List.of(new RawReservationSlotResponseDto(10L, assetId, occupiedStart)));

        AssetTimeResponseDto result = reservationQueryService.getAssetTimes(userId, assetId, date);

        assertThat(result.getAssetId()).isEqualTo(assetId);
        assertThat(result.getTimeSlots()).hasSize(24);
        assertThat(result.getTimeSlots().get(9).getStart()).isEqualTo("09:00");
        assertThat(result.getTimeSlots().get(9).isAvailable()).isFalse();
        assertThat(result.getTimeSlots().get(10).isAvailable()).isTrue();

        verify(userReader).findById(userId);
        verify(assetQueryService).getAssetById(assetId);
    }
}
