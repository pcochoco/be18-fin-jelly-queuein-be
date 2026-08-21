package com.beyond.qiin.domain.inventory.repository.querydsl;

import com.beyond.qiin.domain.booking.dto.reservation.request.criteria.ReservableAssetSearchCriteria;
import com.beyond.qiin.domain.booking.dto.reservation.response.reservable_asset.ReservableAssetResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservableAssetQueryRepository {
    Page<ReservableAssetResponseDto> getReservableAssets(
            final ReservableAssetSearchCriteria criteria, final Pageable pageable);
}
