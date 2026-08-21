package com.beyond.qiin.domain.inventory.repository.querydsl;

import static com.beyond.qiin.domain.inventory.entity.QAsset.asset;
import static com.beyond.qiin.domain.inventory.entity.QAssetClosure.assetClosure;
import static com.beyond.qiin.domain.inventory.entity.QCategory.category;

import com.beyond.qiin.domain.booking.dto.reservation.request.criteria.ReservableAssetSearchCriteria;
import com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawReservableAssetResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.reservable_asset.ReservableAssetResponseDto;
import com.beyond.qiin.domain.booking.entity.QReservationSlot;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReservableAssetQueryRepositoryImpl implements ReservableAssetQueryRepository {

    private static final long TOTAL_DAY_SLOT_COUNT = 24L;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ReservableAssetResponseDto> getReservableAssets(
            ReservableAssetSearchCriteria criteria, Pageable pageable) {

        List<ReservableAssetResponseDto> content = queryFactory
                .select(Projections.constructor(
                        RawReservableAssetResponseDto.class,
                        asset.id,
                        asset.name,
                        asset.type,
                        category.name,
                        asset.needsApproval))
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
                .fetch()
                .stream()
                .map(ReservableAssetResponseDto::fromRaw)
                .toList();

        Long total = queryFactory
                .select(asset.count())
                .from(asset)
                .leftJoin(asset.category, category)
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

    private BooleanExpression assetNameContains(String assetName) {
        if (assetName == null) {
            return null;
        }
        return asset.name.containsIgnoreCase(assetName);
    }

    private BooleanExpression assetTypeEq(Integer assetType) {
        if (assetType == null) {
            return null;
        }
        return asset.type.eq(assetType);
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return asset.category.id.eq(categoryId);
    }

    private BooleanExpression hierarchyCondition(ReservableAssetSearchCriteria criteria) {
        if (criteria.getLayerOne() != null) {
            return asset.id.in(JPAExpressions.select(assetClosure.assetClosureId.descendantId)
                    .from(assetClosure)
                    .where(
                            assetClosure.assetClosureId.ancestorId.eq(criteria.getLayerOne()),
                            assetClosure.depth.gt(0)));
        }

        if (criteria.getLayerZero() != null) {
            return asset.id.in(JPAExpressions.select(assetClosure.assetClosureId.descendantId)
                    .from(assetClosure)
                    .where(
                            assetClosure.assetClosureId.ancestorId.eq(criteria.getLayerZero()),
                            assetClosure.depth.gt(0)));
        }

        return null;
    }

    private BooleanExpression assetStatusAvailable() {
        return asset.status.eq(0);
    }

    private BooleanExpression hasReservableTime(Instant dayStart, Instant dayEnd) {
        return hasEmptySlot(dayStart, dayEnd, TOTAL_DAY_SLOT_COUNT);
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
