package com.beyond.qiin.domain.booking.repository.querydsl;

import com.beyond.qiin.domain.booking.dto.reservation.request.search_condition.GetAppliedReservationSearchCondition;
import com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawAppliedReservationResponseDto;
import com.beyond.qiin.domain.booking.entity.QReservation;
import com.beyond.qiin.domain.booking.enums.ReservationStatus;
import com.beyond.qiin.domain.booking.vo.DateRange;
import com.beyond.qiin.domain.iam.entity.QUser;
import com.beyond.qiin.domain.inventory.entity.QAsset;
import com.beyond.qiin.domain.inventory.entity.QAssetClosure;
import com.beyond.qiin.domain.inventory.entity.QCategory;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AppliedReservationsQueryRepositoryImpl implements AppliedReservationsQueryRepository {

    private final JPAQueryFactory query;

    private static final QReservation reservation = QReservation.reservation;
    private static final QAsset asset = QAsset.asset;
    private static final QCategory category = QCategory.category;
    private static final QAssetClosure closure = QAssetClosure.assetClosure;
    private static final QUser applicant = new QUser("applicant");
    private static final QUser respondent = new QUser("respondent");

    @Override
    public List<RawAppliedReservationResponseDto> search(
            GetAppliedReservationSearchCondition condition, DateRange range, ReservationStatus reservationStatus) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(reservation.isApplied.eq(true)); // 신청된 경우

        // 날짜(Instant)
        if (range.getStartDay() != null) {
            builder.and(reservation.startAt.goe(range.getStartDay()));
        }

        if (range.getEndDay() != null) {
            builder.and(reservation.startAt.lt(range.getEndDay()));
        }

        // reservation status
        if (reservationStatus != null) {

            builder.and(reservation.status.eq(reservationStatus.getCode()));
        }

        // 신청자 이름 검색
        if (condition.getApplicantName() != null) {
            builder.and(applicant.userName.containsIgnoreCase(condition.getApplicantName()));
        }

        // 자원명
        if (condition.getAssetName() != null) {
            builder.and(asset.name.containsIgnoreCase(condition.getAssetName()));
        }

        // category id
        if (condition.getCategoryId() != null) {
            builder.and(asset.category.id.eq(condition.getCategoryId()));
        }

        // 조회
        var queryBuilder = query.select(Projections.constructor(
                        RawAppliedReservationResponseDto.class,
                        asset.id,
                        asset.name,
                        reservation.id,
                        applicant.userName,
                        respondent.userName,
                        reservation.status,
                        reservation.isApproved,
                        reservation.reason,
                        reservation.version,
                        reservation.startAt,
                        reservation.endAt))
                .from(reservation)
                .join(reservation.asset, asset)
                .leftJoin(asset.category, category)
                .leftJoin(reservation.applicant, applicant)
                .leftJoin(reservation.respondent, respondent);

        return queryBuilder.where(builder).orderBy(reservation.id.desc()).fetch();
    }
}
