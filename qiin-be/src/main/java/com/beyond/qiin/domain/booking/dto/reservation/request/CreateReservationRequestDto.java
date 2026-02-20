package com.beyond.qiin.domain.booking.dto.reservation.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
public class CreateReservationRequestDto {

    // 예약 시작 시간
    @NotNull
    @FutureOrPresent // 과거 시간 예약 불가
    private Instant startAt;

    // 예약 종료 시간
    @NotNull
    @Future // 미래 시간 가능
    private Instant endAt;

    // 예약 설명
    private String description;

    // 버전 : 생성 시 x

    // 참여자들
    @NotNull
    @Builder.Default
    private List<Long> attendantIds = new ArrayList<>();

    //    public Reservation toEntity(
    //            final Asset asset,
    //            final User applicant,
    //            // final List<Attendant> attendants,
    //            final ReservationStatus reservationStatus) {
    //
    //        Reservation reservation = Reservation.builder()
    //                .asset(asset)
    //                .applicant(applicant)
    //                .startAt(startAt)
    //                .endAt(endAt)
    //                .description(description)
    //                .status(reservationStatus.getCode())
    //                .reservationStatus(reservationStatus)
    //                .build();
    //
    //        // reservation.addAttendants(attendants);
    //
    //        return reservation;
    //    }
}
