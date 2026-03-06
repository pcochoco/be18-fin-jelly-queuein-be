package com.beyond.qiin.domain.booking.dto.reservation.request.criteria;

import com.beyond.qiin.domain.booking.dto.reservation.request.search_condition.GetAppliedReservationSearchCondition;
import com.beyond.qiin.domain.booking.enums.ReservationStatus;
import com.beyond.qiin.domain.booking.vo.DateRange;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class AppliedReservationSearchCriteria {
    private final DateRange dateRange;
    private final ReservationStatus reservationStatus;
    private final String applicantName;
    private final String assetName;
    private final Long categoryId;

    public static AppliedReservationSearchCriteria from(
            final DateRange dateRange,
            final ReservationStatus reservationStatus,
            final GetAppliedReservationSearchCondition condition) {
        return AppliedReservationSearchCriteria.builder()
                .dateRange(dateRange)
                .reservationStatus(reservationStatus)
                .applicantName(condition.getApplicantName())
                .assetName(condition.getAssetName())
                .categoryId(condition.getCategoryId())
                .build();
    }
}
