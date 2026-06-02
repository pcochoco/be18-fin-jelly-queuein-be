package com.beyond.qiin.domain.booking.repository.querydsl;

import com.beyond.qiin.domain.booking.dto.reservation.request.criteria.AppliedReservationSearchCriteria;
import com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawAppliedReservationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppliedReservationsQueryRepository {
    Page<RawAppliedReservationResponseDto> search(
            AppliedReservationSearchCriteria appliedReservationSearchCriteria, Pageable pageable);
}
