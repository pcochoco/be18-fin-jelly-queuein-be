package com.beyond.qiin.domain.booking.dto.reservation.request.search_condition;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GetAppliedReservationSearchCondition {
    private LocalDate startDate;
    private LocalDate endDate;
    private String reservationStatus;
    private String applicantName;
    private String assetName; // 검색용이므로 unique 안하고 여러개 떠도 됨
    private Long categoryId;
}
