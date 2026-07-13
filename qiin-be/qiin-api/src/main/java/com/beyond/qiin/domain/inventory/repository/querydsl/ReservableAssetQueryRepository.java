package com.beyond.qiin.domain.inventory.repository.querydsl;

import com.beyond.qiin.common.dto.PageResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.request.search_condition.ReservableAssetSearchCondition;
import com.beyond.qiin.domain.booking.dto.reservation.response.reservable_asset.ReservableAssetResponseDto;
import org.springframework.data.domain.Pageable;

public interface ReservableAssetQueryRepository {
    PageResponseDto<ReservableAssetResponseDto> getReservableAssets(
            final ReservableAssetSearchCondition condition, final Pageable pageable);
}
