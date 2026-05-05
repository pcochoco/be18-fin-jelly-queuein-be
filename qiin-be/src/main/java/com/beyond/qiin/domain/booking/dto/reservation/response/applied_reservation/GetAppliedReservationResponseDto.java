package com.beyond.qiin.domain.booking.dto.reservation.response.applied_reservation;

import com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawAppliedReservationResponseDto;
import com.beyond.qiin.domain.booking.enums.ReservationStatus;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

// 관리자 승인 / 거절
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Jacksonized
@Getter
public class GetAppliedReservationResponseDto {

    // 자원명
    private final String assetName;

    // 예약 id
    private final Long reservationId;

    // 신청자
    private final String applicantName;

    // 예약 가능 여부
    private final boolean reservable; // jacksonized에서 파악용 필드

    // 응답 시 필수 x
    // 승인자
    private final String respondentName;

    // 승인 여부
    private final Boolean isApproved;

    private final String reservationStatus;

    // 사유
    private final String reason;

    private final Long version;

    public static GetAppliedReservationResponseDto fromRaw(
            final RawAppliedReservationResponseDto raw, final boolean isReservable) {
        return GetAppliedReservationResponseDto.builder()
                .assetName(raw.getAssetName())
                .reservationId(raw.getReservationId())
                .applicantName(raw.getApplicantName())
                .respondentName(raw.getRespondentName())
                .reservationStatus(
                        ReservationStatus.from(raw.getReservationStatus()).name())
                .isApproved(raw.getIsApproved())
                .reservable(isReservable)
                .version(raw.getVersion())
                .reason(raw.getReason())
                .build();
    }
}
