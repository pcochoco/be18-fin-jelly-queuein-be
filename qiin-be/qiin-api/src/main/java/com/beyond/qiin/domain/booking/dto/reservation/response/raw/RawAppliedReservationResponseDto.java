package com.beyond.qiin.domain.booking.dto.reservation.response.raw;

import com.beyond.qiin.domain.inventory.enums.AssetStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RawAppliedReservationResponseDto {

    private final Long assetId;
    private final String assetName;
    private final int assetStatus;
    private final Long reservationId;
    private final String applicantName;
    private final String respondentName;
    private final int reservationStatus;
    private final Boolean isApproved;
    private final String reason;
    private final Long version;
    private final Instant startAt;
    private final Instant endAt;

    public boolean isAssetAvailable() {
        return assetStatus == AssetStatus.AVAILABLE.getCode();
    }
}
