package com.beyond.qiin.domain.booking.dto.reservation.response.slot;

import java.time.Instant;

public record RawReservationSlotResponseDto(Long reservationId, Long assetId, Instant startAt) {}
