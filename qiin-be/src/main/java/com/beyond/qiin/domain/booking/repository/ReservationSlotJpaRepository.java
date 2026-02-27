package com.beyond.qiin.domain.booking.repository;

import com.beyond.qiin.domain.booking.entity.ReservationSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationSlotJpaRepository extends JpaRepository<ReservationSlot, Long> {
    void deleteByReservationId(Long reservationId);
}
