package com.beyond.qiin.domain.booking.dto.reservation.request.search_condition;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GetAppliedReservationSearchCondition {
    @NotNull
    private LocalDate date;

    private LocalDateTime from;
    private LocalDateTime to;

    private String applicantName;
    private String respondentName;
    // TODO : reservation event log 을 통해 ui상 승인 이후 / 이전 취소 구분
    // TODO : isReservable은 dto에 추가 정보로 포함해 전달 - 필터링의 조건은 아님

    private String reservationStatus;

    private String assetName; // 검색용이므로 unique 안하고 여러개 떠도 됨
    private String assetType;
    private Long categoryId;
    private String layerZero;
    private String layerOne;
}
