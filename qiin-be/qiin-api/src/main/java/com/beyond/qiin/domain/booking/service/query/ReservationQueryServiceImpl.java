package com.beyond.qiin.domain.booking.service.query;

import com.beyond.qiin.common.dto.PageResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.request.criteria.AppliedReservationSearchCriteria;
import com.beyond.qiin.domain.booking.dto.reservation.request.search_condition.GetAppliedReservationSearchCondition;
import com.beyond.qiin.domain.booking.dto.reservation.request.search_condition.GetUserReservationSearchCondition;
import com.beyond.qiin.domain.booking.dto.reservation.response.ReservationDetailResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.applied_reservation.GetAppliedReservationResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.asset_time.AssetTimeResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.asset_time.TimeSlotDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.month_reservation.MonthReservationDailyResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.month_reservation.MonthReservationListResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.month_reservation.MonthReservationResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawAppliedReservationResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawReservationSlotResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.raw.RawUserReservationResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.user_reservation.GetUserReservationResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.week_reservation.WeekReservationDailyResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.week_reservation.WeekReservationListResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.week_reservation.WeekReservationResponseDto;
import com.beyond.qiin.domain.booking.entity.Reservation;
import com.beyond.qiin.domain.booking.enums.ReservationStatus;
import com.beyond.qiin.domain.booking.exception.ReservationErrorCode;
import com.beyond.qiin.domain.booking.exception.ReservationException;
import com.beyond.qiin.domain.booking.repository.ReservationSlotJpaRepository;
import com.beyond.qiin.domain.booking.repository.querydsl.AppliedReservationsQueryRepository;
import com.beyond.qiin.domain.booking.repository.querydsl.UserReservationsQueryRepository;
import com.beyond.qiin.domain.booking.support.ReservationReader;
import com.beyond.qiin.domain.booking.util.AvailableTimeSlotCalculator;
import com.beyond.qiin.domain.booking.vo.DateRange;
import com.beyond.qiin.domain.booking.vo.TimeSlot;
import com.beyond.qiin.domain.iam.support.user.UserReader;
import com.beyond.qiin.domain.inventory.service.query.AssetQueryService;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationQueryServiceImpl implements ReservationQueryService {

    private final UserReader userReader;
    private final ReservationReader reservationReader;
    private final AssetQueryService assetQueryService;
    private final UserReservationsQueryRepository userReservationsQueryRepository;
    private final AppliedReservationsQueryRepository appliedReservationsQueryRepository;
    private final ReservationSlotJpaRepository reservationSlotJpaRepository;

    // 예약 상세 조회 (api용)
    @Override
    @Transactional(readOnly = true)
    public ReservationDetailResponseDto getReservation(final Long userId, final Long reservationId) {

        userReader.findById(userId);
        Reservation reservation = reservationReader.getReservationById(reservationId);

        ReservationDetailResponseDto reservationDetailResponseDto =
                ReservationDetailResponseDto.fromEntity(reservation);
        return reservationDetailResponseDto;
    }

    // 사용자 이름으로 예약 목록 조회
    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<GetUserReservationResponseDto> getReservationsByUserId(
            final Long userId, final GetUserReservationSearchCondition condition, final Pageable pageable) {
        // 사용자 있는지 확인
        userReader.findById(userId);

        Page<RawUserReservationResponseDto> rawPage =
                userReservationsQueryRepository.search(userId, condition, pageable);

        Page<GetUserReservationResponseDto> page = rawPage.map(GetUserReservationResponseDto::fromRaw);

        return PageResponseDto.from(page);
    }

    // 신청 예약 목록 조회
    @Cacheable(
            cacheNames = "appliedReservations", // key prefix로 추가해 해당 캐시용으로 구별 용도
            key = "@appliedReservationCachePolicy.defaultKey()", // 조건으로 확인했으므로 키 생성시는 condition, pageable 생략
            // (default와 동일 키이기 때문)
            condition = "@appliedReservationCachePolicy.isCacheable(#condition, #pageable)", // 캐시 적용 여부의 조건
            sync = true // 동시 요청으로 인해 다수 cache miss 발생 가능
            // 따라서 캐시가 비어있는 경우는 1 요청 캐시 생성 후 나머지 요청의 캐시 사용 적용(전부 cache miss로 적용 x이도록)
            )
    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<GetAppliedReservationResponseDto> getReservationApplies(
            final Long userId, final GetAppliedReservationSearchCondition condition, final Pageable pageable) {

        userReader.findById(userId);

        // string -> enum
        ReservationStatus status = parseStatus(condition.getReservationStatus());

        DateRange range = resolveSearchDateRange(condition);

        AppliedReservationSearchCriteria appliedReservationSearchCriteria =
                AppliedReservationSearchCriteria.from(range, status, condition);

        Page<RawAppliedReservationResponseDto> rawPage =
                appliedReservationsQueryRepository.search(appliedReservationSearchCriteria, pageable);

        Map<Long, List<RawReservationSlotResponseDto>> slotsByAssetId = getReservationSlotsInBulk(rawPage.getContent());

        // 예약 가능한지에 대해 포함해서 페이지 제공
        Page<GetAppliedReservationResponseDto> page = rawPage.map(raw -> {
            boolean isAssetAvailable = raw.isAssetAvailable();

            boolean isReservableTime = raw.getReservationStatus() == ReservationStatus.PENDING.getCode()
                    && isReservationTimeAvailable(raw.getAssetId(), raw.getStartAt(), raw.getEndAt(), slotsByAssetId);

            return GetAppliedReservationResponseDto.fromRaw(raw, isReservableTime && isAssetAvailable);
        });
        return PageResponseDto.from(page);
    }

    // 주별 일정 조회
    @Override
    @Transactional(readOnly = true)
    public WeekReservationListResponseDto getWeeklyReservations(final Long userId, final LocalDate date) { // 해당 주의기준날짜
        userReader.findById(userId);

        LocalDate startDate = date.with(DayOfWeek.MONDAY);
        LocalDate endDate = startDate.plusDays(6);

        Instant start = startDate.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();

        List<Reservation> reservations = reservationReader.getReservationsByUserAndWeek(userId, start, end);

        Map<LocalDate, List<Reservation>> byDate = reservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStartAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDate()));

        // 비어있을 수 있음
        List<WeekReservationResponseDto> reservationList = new ArrayList<>();

        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {

            List<Reservation> dailyReservations = byDate.getOrDefault(cursor, List.of());

            WeekReservationResponseDto dto = WeekReservationResponseDto.builder()
                    .date(cursor)
                    .reservations(dailyReservations.stream()
                            .map(WeekReservationDailyResponseDto::fromEntity)
                            .toList())
                    .build();

            reservationList.add(dto);

            cursor = cursor.plusDays(1);
        }

        WeekReservationListResponseDto weekReservationListResponseDto = WeekReservationListResponseDto.builder()
                .reservations(reservationList)
                .build();

        return weekReservationListResponseDto;
    }

    // 월별 일정 조회
    @Override
    @Transactional(readOnly = true)
    public MonthReservationListResponseDto getMonthlyReservations(
            final Long userId, final YearMonth yearMonth) { // 일까지 포함 X이므로 달까지 포함하는 자료형 사용
        userReader.findById(userId);

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        Instant from = start.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();
        Instant to = end.plusDays(1).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();
        // 비어있을 수 있음
        List<Reservation> reservations = reservationReader.getReservationsByUserAndYearMonth(userId, from, to);

        // 예약을 LocalDate 기준으로 그룹핑
        Map<LocalDate, List<Reservation>> byDate = reservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStartAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDate()));

        // 월의 모든 날짜 dto 생성
        List<MonthReservationResponseDto> dailyDtos = new ArrayList<>();

        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            List<Reservation> dailyReservations = byDate.getOrDefault(cursor, List.of());

            MonthReservationResponseDto dto = MonthReservationResponseDto.builder()
                    .date(cursor)
                    .reservations(dailyReservations.stream()
                            .map(MonthReservationDailyResponseDto::fromEntity) // 필요하면 inside DTO
                            .toList())
                    .build();

            dailyDtos.add(dto);

            cursor = cursor.plusDays(1);
        }

        return MonthReservationListResponseDto.builder().reservations(dailyDtos).build();
    }

    // 자원의 (예약 가능 시간대 포함) 모든 시간대 목록 조회
    @Override
    @Transactional(readOnly = true)
    public AssetTimeResponseDto getAssetTimes(final Long userId, final Long assetId, final LocalDate date) {

        userReader.findById(userId);

        assetQueryService.getAssetById(assetId);

        // 해당 자원의 해당 날짜 예약 목록
        ZoneId zone = ZoneId.of("Asia/Seoul");

        DateRange range = dayToInstant(date, zone);

        List<RawReservationSlotResponseDto> occupiedSlots =
                reservationSlotJpaRepository.findAllByAssetAndDate(assetId, range.getStartDay(), range.getEndDay());

        List<TimeSlot> timeSlots = AvailableTimeSlotCalculator.calculateAvailableSlots(occupiedSlots, date, zone);

        List<TimeSlotDto> timeSlotDtos =
                timeSlots.stream().map(slot -> TimeSlotDto.create(slot, zone)).toList();

        return AssetTimeResponseDto.create(assetId, timeSlotDtos);
    }

    private DateRange dayToInstant(final LocalDate date, final ZoneId zone) {
        Instant start = date.atStartOfDay(zone).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zone).toInstant();

        return DateRange.create(start, end);
    }

    boolean isReservableForDay(final LocalDate date, final List<Reservation> reservations) {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        Instant dayStart = date.atStartOfDay(zone).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();

        Instant cursor = dayStart;

        List<Reservation> sortedReservations = reservations.stream()
                .filter(reservation -> reservation.getEndAt().isAfter(dayStart)
                        && reservation.getStartAt().isBefore(dayEnd))
                .sorted(Comparator.comparing(Reservation::getStartAt))
                .toList();

        for (Reservation reservation : sortedReservations) {
            Instant reservedStart = reservation.getStartAt().isBefore(dayStart) ? dayStart : reservation.getStartAt();
            Instant reservedEnd = reservation.getEndAt().isAfter(dayEnd) ? dayEnd : reservation.getEndAt();

            if (cursor.isBefore(reservedStart)) {
                return true;
            }

            if (cursor.isBefore(reservedEnd)) {
                cursor = reservedEnd;
            }
        }

        return cursor.isBefore(dayEnd);
    }

    // test 가능하도록 package private 허용
    // 자원에 대한 예약 가능의 유무 -  비즈니스 책임이므로 command service로
    boolean isReservationTimeAvailable(
            final Long assetId,
            final Instant startAt, // 신청한 시작 시간
            final Instant endAt, // 신청한 종료 시간
            final Map<Long, List<RawReservationSlotResponseDto>> slotsByAssetId) {

        List<RawReservationSlotResponseDto> assetSlots = slotsByAssetId.getOrDefault(assetId, List.of());

        return assetSlots.stream()
                // 자원에 이미 있는 slot들과 희망하는 예약의 startAt, endAt이 겹치지 않아야함
                .noneMatch(slot ->

                        // startAt <= slot.startAt < endAt
                        !slot.startAt().isBefore(startAt) && slot.startAt().isBefore(endAt));
    }

    private Map<Long, List<RawReservationSlotResponseDto>> getReservationSlotsInBulk(
            final List<RawAppliedReservationResponseDto> reservations) {

        if (reservations.isEmpty()) {
            return Map.of();
        }

        List<Long> assetIds = reservations.stream()
                .map(RawAppliedReservationResponseDto::getAssetId)
                .distinct()
                .toList();

        Instant minStartAt = reservations.stream()
                .map(RawAppliedReservationResponseDto::getStartAt)
                .min(Instant::compareTo)
                .orElseThrow();

        Instant maxEndAt = reservations.stream()
                .map(RawAppliedReservationResponseDto::getEndAt)
                .max(Instant::compareTo)
                .orElseThrow();

        List<RawReservationSlotResponseDto> slots =
                reservationSlotJpaRepository.findAllForReservability(assetIds, minStartAt, maxEndAt);

        return slots.stream().collect(Collectors.groupingBy(RawReservationSlotResponseDto::assetId));
    }

    private DateRange resolveSearchDateRange(GetAppliedReservationSearchCondition condition) {

        ZoneId zone = ZoneId.of("Asia/Seoul");

        LocalDate start = condition.getStartDate();
        LocalDate end = condition.getEndDate();

        // 1. 기본값 (둘 다 null이면 오늘부터 한 달)
        if (start == null && end == null) {
            start = LocalDate.now(zone);
            end = start.plusMonths(1);
        }

        // 2. 순서 검증
        if (start != null && end != null && start.isAfter(end)) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_TIME_INVALID);
        }

        // 3. 최대 조회 기간 제한 (90일)
        if (start != null && end != null && ChronoUnit.DAYS.between(start, end) > 90) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_SEARCH_PERIOD_EXCEEDED);
        }

        // 4. Instant 변환
        Instant startInstant = start != null ? start.atStartOfDay(zone).toInstant() : null;

        Instant endInstant = end != null ? end.plusDays(1).atStartOfDay(zone).toInstant() : null;

        return DateRange.create(startInstant, endInstant);
    }

    private ReservationStatus parseStatus(String value) {
        if (value == null) return null;

        try {
            return ReservationStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_STATUS_INVALID);
        }
    }
}
