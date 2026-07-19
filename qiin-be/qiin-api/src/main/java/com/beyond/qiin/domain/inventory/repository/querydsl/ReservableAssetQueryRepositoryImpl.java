package com.beyond.qiin.domain.inventory.repository.querydsl;

import com.beyond.qiin.domain.booking.dto.reservation.response.reservable_asset.ReservableAssetResponseDto;
import com.beyond.qiin.domain.booking.entity.QReservationSlot;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class ReservableAssetQueryRepositoryImpl implements ReservableAssetQueryRepository {

    @Override
    public Page<ReservableAssetResponseDto> getReservableAssets(
            ReservableAssetSearchCriteria criteria, Pageable pageable) {

        List<ReservableAssetResponseDto> content = queryFactory
                .select(Projections.constructor(
                        ReservableAssetResponseDto.class, asset.id, asset.name, asset.type, category.id, category.name))
                .from(asset)
                .leftJoin(asset.category, category)
                .where(
                        assetNameContains(criteria.getAssetName()),
                        assetTypeEq(criteria.getAssetType()),
                        categoryIdEq(criteria.getCategoryId()),
                        hierarchyCondition(criteria),
                        assetStatusAvailable(),
                        hasReservableTime(criteria.getDayStart(), criteria.getDayEnd()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(asset.id.desc())
                .fetch();

        Long total = queryFactory
                .select(asset.count())
                .from(asset)
                .where(
                        assetNameContains(criteria.getAssetName()),
                        assetTypeEq(criteria.getAssetType()),
                        categoryIdEq(criteria.getCategoryId()),
                        hierarchyCondition(criteria),
                        assetStatusAvailable(),
                        hasReservableTime(criteria.getDayStart(), criteria.getDayEnd()))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    // 자원 조회하는 용의 조건(보조 메서드)
    private BooleanExpression hasEmptySlot(Instant dayStart, Instant dayEnd, long totalSlotCount) {

        QReservationSlot slotSub = new QReservationSlot("slotSub");

        return JPAExpressions.select(slotSub.count())
                .from(slotSub)
                .where(slotSub.asset.id.eq(asset.id), slotSub.startAt.goe(dayStart), slotSub.startAt.lt(dayEnd))
                .lt(totalSlotCount);
    }
}
