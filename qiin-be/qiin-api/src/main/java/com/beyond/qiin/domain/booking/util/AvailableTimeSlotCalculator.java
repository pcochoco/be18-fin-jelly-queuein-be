package com.beyond.qiin.domain.booking.util;

import com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawReservationSlotResponseDto;
import com.beyond.qiin.domain.booking.vo.TimeSlot;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AvailableTimeSlotCalculator {

    public static boolean isReservable(
            final List<RawReservationSlotResponseDto> occupiedSlots, final LocalDate date, final ZoneId zoneId) {

        return calculateAvailableSlots(occupiedSlots, date, zoneId).stream().anyMatch(TimeSlot::isAvailable);
    }

    public static List<TimeSlot> calculateAvailableSlots(
            final List<RawReservationSlotResponseDto> occupiedSlots, final LocalDate date, final ZoneId zoneId) {

        Set<Instant> occupiedStarts = occupiedSlots.stream()
                .map(RawReservationSlotResponseDto::startAt)
                .collect(Collectors.toSet());

        return IntStream.range(0, 24)
                .mapToObj(hour -> {
                    Instant startAt = date.atTime(hour, 0).atZone(zoneId).toInstant();

                    Instant endAt =
                            date.atTime(hour, 0).plusHours(1).atZone(zoneId).toInstant();

                    // 응답용 vo : timeslot
                    return TimeSlot.create(startAt, endAt, !occupiedStarts.contains(startAt));
                })
                .toList();
    }
}
