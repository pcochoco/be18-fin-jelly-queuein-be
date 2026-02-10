package com.beyond.qiin.domain.booking.dto.reservation.request.search_condition;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetUserReservationSearchCondition {

    private final LocalDate fromDate;

    private final LocalDate toDate;

    private String reservationStatus;
    private String isApproved;
    private Long categoryId;
}
