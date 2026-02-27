package com.beyond.qiin.domain.booking.support;

import com.beyond.qiin.domain.booking.entity.Reservation;
import com.beyond.qiin.domain.booking.entity.ReservationSlot;
import com.beyond.qiin.domain.booking.exception.ReservationErrorCode;
import com.beyond.qiin.domain.booking.exception.ReservationException;
import com.beyond.qiin.domain.booking.repository.ReservationSlotJpaRepository;
import com.beyond.qiin.domain.inventory.entity.Asset;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationSlotManager {
    private final ReservationSlotJpaRepository reservationSlotJpaRepository;

    public void createSlots(Reservation reservation, Asset asset) {
        List<ReservationSlot> slots = new ArrayList<>();

        Instant cursor = reservation.getStartAt();

        while (cursor.isBefore(reservation.getEndAt())) {
            slots.add(ReservationSlot.create(reservation, cursor, asset));
            cursor = cursor.plus(1, ChronoUnit.HOURS); // 시작 시간으로부터 1시간씩 증가
        }

        try {
            reservationSlotJpaRepository.saveAll(slots);
            reservationSlotJpaRepository.flush(); // 여기서 UNIQUE 충돌 발생
        } catch (DataIntegrityViolationException e) {

            // uk_asset_slot (asset_id, start_at) 충돌인 경우만 변환
            if (e.getMessage() != null && e.getMessage().contains("uk_asset_slot")) {
                throw new ReservationException(ReservationErrorCode.RESERVATION_TIME_DUPLICATED);
            }

            // 다른 무결성 예외는 그대로 던짐
            throw e;
        }
    }
}
