package com.beyond.qiin.domain.booking.entity;

import com.beyond.qiin.domain.inventory.entity.Asset;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class) // created at만 포함하므로 base entity extend x
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        name = "reservation_slot",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_asset_slot",
                    columnNames = {"asset_id", "start_at"})
        })
public class ReservationSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_slot_id", columnDefinition = "bigint")
    private Long id;

    @Column(name = "start_at", nullable = false, updatable = false, columnDefinition = "DATETIME(0)") // 수정이 아니라 삭제되는 형태
    private Instant startAt;

    @Comment("생성 시각")
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME(6)")
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset; // unique key를 위해 추가

    public static ReservationSlot create(Reservation reservation, Instant startAt, Asset asset) {
        return ReservationSlot.builder()
                .reservation(reservation)
                .startAt(startAt)
                .asset(asset)
                .build();
    }
}
