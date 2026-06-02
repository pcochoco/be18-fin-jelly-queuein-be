package com.beyond.qiin.infra.kafka.reservation.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationEventPayload {
    private String eventType; // 모든 서비스에서 enum을 맞추는 것보다 string으로 주는게 유연
    private Long reservationId;
    private Long assetId;
    private Long applicantId;
    private Long respondentId;

    private String startAt;
    private String endAt;

    private String status;

    @Builder.Default
    private List<Long> attendantUserIds = new ArrayList<>();
}
