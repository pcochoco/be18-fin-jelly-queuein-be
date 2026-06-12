package com.beyond.qiin.domain.booking.dto.reservation.request.search_condition;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GetUserReservationSearchCondition {
    @NotNull
    private LocalDate date;

    private String reservationStatus;
    private String isApproved;
}
