package com.beyond.qiin.domain.booking.service.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.beyond.qiin.domain.booking.dto.reservation.response.slot.RawReservationSlotResponseDto;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationTimeValidationTest {

    @InjectMocks
    ReservationQueryServiceImpl reservationService;

    private RawReservationSlotResponseDto slot(String start, Long id) {
        return new RawReservationSlotResponseDto(id, 1L, Instant.parse(start));
    }

    private boolean check(String start, String end, List<RawReservationSlotResponseDto> existing) {
        return reservationService.isReservationTimeAvailable(
                1L, Instant.parse(start), Instant.parse(end), Map.of(1L, existing));
    }

    @DisplayName("완전 비겹침 - 왼쪽")
    @Test
    void left_non_overlap() {
        List<RawReservationSlotResponseDto> existing = List.of(slot("2025-01-03T09:00:00Z", 1L));

        boolean ok = check("2025-01-03T07:00:00Z", "2025-01-03T08:00:00Z", existing);

        assertThat(ok).isTrue();
    }

    @DisplayName("완전 비겹침 - 오른쪽")
    @Test
    void right_non_overlap() {
        List<RawReservationSlotResponseDto> existing = List.of(slot("2025-01-03T09:00:00Z", 1L));

        boolean ok = check("2025-01-03T11:00:00Z", "2025-01-03T12:00:00Z", existing);

        assertThat(ok).isTrue();
    }

    @DisplayName("시각이 겹치는 경우")
    @Test
    void touching_is_allowed() {
        List<RawReservationSlotResponseDto> existing = List.of(slot("2025-01-03T09:00:00Z", 1L));

        boolean ok = check("2025-01-03T10:00:00Z", "2025-01-03T11:00:00Z", existing);

        assertThat(ok).isTrue();
    }

    @DisplayName("부분 겹침 (왼쪽)")
    @Test
    void partial_overlap_left() {
        List<RawReservationSlotResponseDto> existing = List.of(slot("2025-01-03T09:00:00Z", 1L));

        boolean ok = check("2025-01-03T08:00:00Z", "2025-01-03T10:00:00Z", existing);

        assertThat(ok).isFalse();
    }

    @DisplayName("기존 전체를 감싸는 경우")
    @Test
    void new_includes_existing() {
        List<RawReservationSlotResponseDto> existing = List.of(slot("2025-01-03T09:00:00Z", 1L));

        boolean ok = check("2025-01-03T08:00:00Z", "2025-01-03T12:00:00Z", existing);

        assertThat(ok).isFalse();
    }

    @DisplayName("기존 안쪽에 완전히 포함되는 경우")
    @Test
    void new_inside_existing() {
        List<RawReservationSlotResponseDto> existing = List.of(slot("2025-01-03T09:00:00Z", 1L));

        boolean ok = check("2025-01-03T09:00:00Z", "2025-01-03T10:00:00Z", existing);

        assertThat(ok).isFalse();
    }
}
