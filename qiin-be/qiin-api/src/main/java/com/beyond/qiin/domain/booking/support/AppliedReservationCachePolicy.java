package com.beyond.qiin.domain.booking.support;

import com.beyond.qiin.domain.booking.dto.reservation.request.search_condition.GetAppliedReservationSearchCondition;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component("appliedReservationCachePolicy")
@RequiredArgsConstructor
public class AppliedReservationCachePolicy {
    public boolean isCacheable(final GetAppliedReservationSearchCondition condition, final Pageable pageable) {
        return hasNoKeyword(condition)
                && isFirstPage(pageable)
                && isPending(condition)
                && isDefaultMonthlyRange(condition)
                && isDefaultSort(pageable)
                && isDefaultPageSize(pageable);
    }

    // evict 시에는 condition, pageable을 받지 않기 때문에
    public String defaultKey() {
        return String.format(
                "status=pending:month=%s:sort=%s:size=%d",
                YearMonth.from(LocalDate.now(ZoneId.of("Asia/Seoul"))), // 달까지 포함하면 됨
                "createdAt,desc",
                20);
    }

    // size 20인지 확인
    private boolean isDefaultPageSize(final Pageable pageable) {
        return pageable.getPageSize() == 20;
    }

    // 검색어　활용　ｘ
    private boolean hasNoKeyword(final GetAppliedReservationSearchCondition condition) {
        return isBlank(condition.getApplicantName()) && isBlank(condition.getAssetName());
    }

    // 첫　페이지인지　확인
    private boolean isFirstPage(final Pageable pageable) {
        return pageable.getPageNumber() == 0;
    }

    // 대기　상태　요청인지　확인
    private boolean isPending(final GetAppliedReservationSearchCondition condition) {
        return "PENDING".equalsIgnoreCase(condition.getReservationStatus());
    }

    // 최근　한달에　대한　요청인지에　대해　확인용
    private boolean isDefaultMonthlyRange(final GetAppliedReservationSearchCondition condition) {
        LocalDate startDate = condition.getStartDate();
        LocalDate endDate = condition.getEndDate();

        if (startDate == null && endDate == null) {
            return true;
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate defaultEnd = today.plusMonths(1);

        return today.equals(startDate) && defaultEnd.equals(endDate);
    }

    private String resolveDefaultMonthToken(final GetAppliedReservationSearchCondition condition) {
        LocalDate baseDate =
                condition.getStartDate() != null ? condition.getStartDate() : LocalDate.now(ZoneId.of("Asia/Seoul"));

        return YearMonth.from(baseDate).toString(); // 날짜까지 포함하지 않고 달까지만 키로 추가
    }

    private boolean isDefaultSort(final Pageable pageable) {
        if (pageable.getSort().isUnsorted()) { // 정렬 파라미터를 안넘긴 경우 query의 정렬이 desc일 때 보장
            return true;
        }

        if (pageable.getSort().stream().count() != 1) { // sort 조건 하나여야함
            return false;
        }

        Sort.Order createdAtDesc = pageable.getSort().getOrderFor("createdAt"); // createdAt에 해당하는 정렬 조건을 담음
        return createdAtDesc != null
                && createdAtDesc.isDescending(); // createdAt 정렬이 존재하고 desc 기준인지 확인 (null인지 확인 x시 npe)
    }

    private String normalizeSort(final Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return "createdAt,desc";
        }

        StringBuilder sb = new StringBuilder();
        for (Sort.Order order : pageable.getSort()) {
            sb.append(order.getProperty())
                    .append(",")
                    .append(order.getDirection().name().toLowerCase());
        }
        return sb.toString();
    }

    // 값이　비었는지　확인　：　검색어　활용했는지　확인용
    private boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}
