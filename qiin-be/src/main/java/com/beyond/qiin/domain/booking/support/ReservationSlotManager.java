package com.beyond.qiin.domain.booking.support;

import com.beyond.qiin.domain.booking.entity.Reservation;
import com.beyond.qiin.domain.booking.entity.ReservationSlot;
import com.beyond.qiin.domain.booking.repository.ReservationSlotJpaRepository;
import com.beyond.qiin.domain.inventory.entity.Asset;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationSlotManager {
    private final ReservationSlotJpaRepository repository;

    public void createSlots(Reservation reservation, Asset asset) {
        List<ReservationSlot> slots = new ArrayList<>();

        Instant cursor = reservation.getStartAt();

        while (cursor.isBefore(reservation.getEndAt())) {
            slots.add(ReservationSlot.create(reservation, cursor, asset));
            cursor = cursor.plus(1, ChronoUnit.HOURS); // 시작 시간으로부터 1시간씩 증가
        }

        repository.saveAll(slots);
    }
}
