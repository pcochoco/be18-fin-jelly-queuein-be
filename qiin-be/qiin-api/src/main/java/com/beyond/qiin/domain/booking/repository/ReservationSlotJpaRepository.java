package com.beyond.qiin.domain.booking.repository;

import com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawReservationSlotResponseDto;
import com.beyond.qiin.domain.booking.entity.ReservationSlot;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationSlotJpaRepository extends JpaRepository<ReservationSlot, Long> {
    void deleteByReservationId(Long reservationId);

    // all slots that startAt, endAt
    @Query(
            """
            SELECT new com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawReservationSlotResponseDto(
                rs.reservation.id,
                rs.asset.id,
                rs.startAt
            )
            FROM ReservationSlot rs
            WHERE rs.asset.id IN :assetIds
              AND rs.startAt >= :startAt
              AND rs.startAt < :endAt
            """)
    List<RawReservationSlotResponseDto> findAllForReservability(
            @Param("assetIds") List<Long> assetIds, @Param("startAt") Instant startAt, @Param("endAt") Instant endAt);

    @Query(
            """
    SELECT new com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawReservationSlotResponseDto(
        rs.reservation.id,
        rs.asset.id,
        rs.startAt
    )
    FROM ReservationSlot rs
    WHERE rs.asset.id = :assetId
      AND rs.startAt >= :startAt
      AND rs.startAt < :endAt
    ORDER BY rs.startAt
    """)
    List<RawReservationSlotResponseDto> findAllByAssetAndDate(
            @Param("assetId") Long assetId, @Param("startAt") Instant startAt, @Param("endAt") Instant endAt);
}
