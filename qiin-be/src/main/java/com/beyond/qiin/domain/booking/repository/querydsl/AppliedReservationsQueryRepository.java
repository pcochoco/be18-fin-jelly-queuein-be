package com.beyond.qiin.domain.booking.repository.querydsl;

import com.beyond.qiin.domain.booking.dto.reservation.request.search_condition.GetAppliedReservationSearchCondition;
import com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawAppliedReservationResponseDto;
import com.beyond.qiin.domain.booking.enums.ReservationStatus;
import com.beyond.qiin.domain.booking.vo.DateRange;
import java.util.List;

public interface AppliedReservationsQueryRepository {
    List<RawAppliedReservationResponseDto> search(
            GetAppliedReservationSearchCondition condition, DateRange range, ReservationStatus reservationStatus);
}
