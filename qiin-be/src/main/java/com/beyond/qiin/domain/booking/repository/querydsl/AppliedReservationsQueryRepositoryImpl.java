package com.beyond.qiin.domain.booking.repository.querydsl;

import com.beyond.qiin.domain.booking.dto.reservation.request.criteria.AppliedReservationSearchCriteria;
import com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawAppliedReservationResponseDto;
import com.beyond.qiin.domain.booking.entity.QReservation;
import com.beyond.qiin.domain.iam.entity.QUser;
import com.beyond.qiin.domain.inventory.entity.QAsset;
import com.beyond.qiin.domain.inventory.entity.QCategory;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AppliedReservationsQueryRepositoryImpl implements AppliedReservationsQueryRepository {

    private final JPAQueryFactory query;

    private static final QReservation reservation = QReservation.reservation;
    private static final QAsset asset = QAsset.asset;
    private static final QCategory category = QCategory.category;
    private static final QUser applicant = new QUser("applicant");
    private static final QUser respondent = new QUser("respondent");

    @Override
    public Page<RawAppliedReservationResponseDto> search(
            AppliedReservationSearchCriteria appliedReservationSearchCriteria, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(reservation.isApplied.eq(true)); // 신청된 경우

        // 날짜(Instant)
        if (appliedReservationSearchCriteria.getDateRange().getStartDay() != null) {
            builder.and(reservation.startAt.goe(
                    appliedReservationSearchCriteria.getDateRange().getStartDay()));
        }

        if (appliedReservationSearchCriteria.getDateRange().getEndDay() != null) {
            builder.and(reservation.startAt.lt(
                    appliedReservationSearchCriteria.getDateRange().getEndDay()));
        }

        // reservation status
        if (appliedReservationSearchCriteria.getReservationStatus() != null) {

            builder.and(reservation.status.eq(
                    appliedReservationSearchCriteria.getReservationStatus().getCode()));
        }

        // 신청자 이름 검색
        if (appliedReservationSearchCriteria.getApplicantName() != null) {
            builder.and(applicant.userName.containsIgnoreCase(appliedReservationSearchCriteria.getApplicantName()));
        }

        // 자원명
        if (appliedReservationSearchCriteria.getAssetName() != null) {
            builder.and(asset.name.containsIgnoreCase(appliedReservationSearchCriteria.getAssetName()));
        }

        // category id
        if (appliedReservationSearchCriteria.getCategoryId() != null) {
            builder.and(asset.category.id.eq(appliedReservationSearchCriteria.getCategoryId()));
        }

        // 조회
        // query 정의
        JPAQuery<?> baseQuery = query.from(reservation)
                .join(reservation.asset, asset)
                .leftJoin(asset.category, category)
                .leftJoin(reservation.applicant, applicant)
                .leftJoin(reservation.respondent, respondent)
                .where(builder);

        List<RawAppliedReservationResponseDto> content = baseQuery
                .clone()
                .select(Projections.constructor(
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
                .orderBy(reservation.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch(); // 실행

        Long total = baseQuery.clone().select(reservation.count()).fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
