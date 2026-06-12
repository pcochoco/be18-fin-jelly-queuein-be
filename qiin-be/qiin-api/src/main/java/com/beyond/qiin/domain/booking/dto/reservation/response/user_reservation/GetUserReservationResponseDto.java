package com.beyond.qiin.domain.booking.dto.reservation.response.user_reservation;

import com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawUserReservationResponseDto;
import com.beyond.qiin.domain.booking.enums.ReservationStatus;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 사용자의 예약에 대한 현황 목록 조회용
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetUserReservationResponseDto {

    private final Long reservationId;

    private final String assetName;

    private final Boolean isApproved;

    private final Instant startAt;

    private final Instant endAt;

    private final String reservationStatus;

    // 응답 시 필수 x
    private final Instant actualStartAt;

    private final Instant actualEndAt;

    private final Long version;

    public static GetUserReservationResponseDto fromRaw(final RawUserReservationResponseDto raw) {
        return GetUserReservationResponseDto.builder()
                .reservationId(raw.getReservationId())
                .assetName(raw.getAssetName())
                .isApproved(raw.isApproved())
                .startAt(raw.getStartAt())
                .endAt(raw.getEndAt())
                .reservationStatus(
                        ReservationStatus.from(raw.getReservationStatus()).name())
                .actualStartAt(raw.getActualStartAt())
                .actualEndAt(raw.getActualEndAt())
                .version(raw.getVersion())
                .build();
    }
}
