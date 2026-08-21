package com.beyond.qiin.domain.booking.dto.reservation.request.criteria;

import com.beyond.qiin.domain.booking.dto.reservation.request.search_condition.ReservableAssetSearchCondition;
import com.beyond.qiin.domain.inventory.enums.AssetType;
import java.time.Instant;
import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservableAssetSearchCriteria {

    private final Instant dayStart;
    private final Instant dayEnd;
    private final String assetName;
    private final Integer assetType;
    private final Long categoryId;
    private final Long layerZero;
    private final Long layerOne;

    public static ReservableAssetSearchCriteria from(final ReservableAssetSearchCondition condition) {
        ZoneId zone = ZoneId.of("Asia/Seoul");

        return ReservableAssetSearchCriteria.builder()
                .dayStart(condition.getDate().atStartOfDay(zone).toInstant())
                .dayEnd(condition.getDate().plusDays(1).atStartOfDay(zone).toInstant())
                .assetName(blankToNull(condition.getAssetName()))
                .assetType(parseAssetType(condition.getAssetType()))
                .categoryId(condition.getCategoryId())
                .layerZero(parseLong(condition.getLayerZero()))
                .layerOne(parseLong(condition.getLayerOne()))
                .build();
    }

    private static Integer parseAssetType(final String assetType) {
        if (assetType == null || assetType.isBlank()) {
            return null;
        }
        return AssetType.fromName(assetType).getCode();
    }

    private static Long parseLong(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }

    private static String blankToNull(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
