package com.beyond.qiin.domain.booking.service.command;

import com.beyond.qiin.domain.accounting.service.command.UsageHistoryCommandService;
import com.beyond.qiin.domain.booking.dto.reservation.request.ConfirmReservationRequestDto;
import com.beyond.qiin.domain.booking.dto.reservation.request.CreateReservationRequestDto;
import com.beyond.qiin.domain.booking.dto.reservation.request.UpdateReservationRequestDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.ReservationResponseDto;
import com.beyond.qiin.domain.booking.entity.Attendant;
import com.beyond.qiin.domain.booking.entity.Reservation;
import com.beyond.qiin.domain.booking.enums.ReservationStatus;
import com.beyond.qiin.domain.booking.event.ReservationEventPublisher;
import com.beyond.qiin.domain.booking.exception.ReservationErrorCode;
import com.beyond.qiin.domain.booking.exception.ReservationException;
import com.beyond.qiin.domain.booking.repository.AttendantJpaRepository;
import com.beyond.qiin.domain.booking.repository.ReservationSlotJpaRepository;
import com.beyond.qiin.domain.booking.support.AttendantWriter;
import com.beyond.qiin.domain.booking.support.ReservationReader;
import com.beyond.qiin.domain.booking.support.ReservationSlotManager;
import com.beyond.qiin.domain.booking.support.ReservationWriter;
import com.beyond.qiin.domain.iam.entity.User;
import com.beyond.qiin.domain.iam.support.user.UserReader;
import com.beyond.qiin.domain.inventory.entity.Asset;
import com.beyond.qiin.domain.inventory.service.command.AssetCommandService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationCommandServiceImpl implements ReservationCommandService {

    private final UserReader userReader;
    private final ReservationReader reservationReader;
    private final ReservationWriter reservationWriter;
    private final AttendantWriter attendantWriter;
    private final AssetCommandService assetCommandService;
    private final ReservationEventPublisher reservationEventPublisher;
    private final AttendantJpaRepository attendantJpaRepository;
    private final UsageHistoryCommandService usageHistoryCommandService;
    private final ReservationSlotManager reservationSlotManager;
    private final ReservationSlotJpaRepository reservationSlotJpaRepository;

    // TODO : 선착순, 승인 예약 중복 처리
    // TODO : entity 생성은 entity 안에서
    // 승인 예약
    @Override
    @Transactional
    @CacheEvict(cacheNames = "appliedReservations", key = "@appliedReservationCachePolicy.defaultKey()")
    public ReservationResponseDto applyReservation(
            final Long userId, final Long assetId, final CreateReservationRequestDto createReservationRequestDto) {

        Asset asset = assetCommandService.getAssetById(assetId);

        assetCommandService.isAvailable(assetId); // 자원 자체가 지금 사용 가능한가에 대한 확인

        User applicant = userReader.findById(userId);

        userReader.validateAllExist(createReservationRequestDto.getAttendantIds());

        List<User> attendantUsers = userReader.findAllByIds(createReservationRequestDto.getAttendantIds());

        Reservation reservation = Reservation.create(
                createReservationRequestDto, applicant, asset, ReservationStatus.PENDING, Instant.now());

        reservation.setIsApplied(true);

        List<Attendant> attendants = attendantUsers.stream()
                .map(user -> Attendant.create(user, reservation))
                .collect(Collectors.toList());

        reservation.addAttendants(attendants);

        reservationWriter.save(reservation);

        attendantWriter.saveAll(attendants);

        return ReservationResponseDto.fromEntity(reservation);
    }

    @Override
    @Transactional
    public ReservationResponseDto instantConfirmReservation(
            final Long userId, final Long assetId, final CreateReservationRequestDto createReservationRequestDto) {

        // 자원
        Asset asset = assetCommandService.getAssetById(assetId);

        assetCommandService.isAvailable(assetId); // 자원 상태 사용 가능

        // 사용자
        User applicant = userReader.findById(userId);

        userReader.validateAllExist(createReservationRequestDto.getAttendantIds()); // 참여자 목록의 사용자들이 모두 존재하는지에 대한 확인

        List<User> attendantUsers = userReader.findAllByIds(createReservationRequestDto.getAttendantIds());

        // 예약 생성 - 선착순 자원은 자동 승인 (미래 시간인지 확인)
        Reservation reservation = Reservation.create(
                createReservationRequestDto, applicant, asset, ReservationStatus.APPROVED, Instant.now());
        reservation.setIsApproved(true); // 승인됨

        // 참여자 추가
        List<Attendant> attendants = attendantUsers.stream()
                .map(user -> Attendant.create(user, reservation))
                .collect(Collectors.toList());

        reservation.addAttendants(attendants);

        reservationWriter.save(reservation);

        attendantWriter.saveAll(attendants);

        List<Long> attendantUserIds = attendants.stream()
                .map(a -> a.getUser().getId())
                .toList(); // 각 attendantUserId 에 대해 넣지 못하는 문제 userId를 인자로 지정하게 해줘야하나

        // reservation slot 생성 - reservation에 startAt, endAt, 있으므로 따로 reservation과의 연관관계 메서드 적용 x
        reservationSlotManager.createSlots(reservation, asset);

        reservationEventPublisher.publishEventCreated(reservation, attendantUserIds);

        return ReservationResponseDto.fromEntity(reservation);
    }

    // key 의 appliedReservations prefix 바탕으로 해당 시 삭제 및 default key를 통해 생성
    @CacheEvict(cacheNames = "appliedReservations", key = "@appliedReservationCachePolicy.defaultKey()")
    @Override
    @Transactional
    public ReservationResponseDto approveReservation(
            final Long userId,
            final Long reservationId,
            final ConfirmReservationRequestDto confirmReservationRequestDto) {

        User respondent = userReader.findById(userId);

        Reservation reservation = reservationReader.getReservationById(reservationId);

        Asset asset = assetCommandService.getAssetById(reservation.getAsset().getId());

        // 신청자는 자신의 예약을 승인할 수 없음
        if (reservation.getApplicant().getId().equals(userId)) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_NOT_APPROVABLE);
        }

        // reservation slot 생성 - reservation에 startAt, endAt, 있으므로 따로 reservation과의 연관관계 메서드 적용 x
        reservationSlotManager.createSlots(reservation, asset);

        reservation.approve(respondent, confirmReservationRequestDto.getReason(), Instant.now()); // status approved

        reservationWriter.save(reservation);

        List<Long> attendantUserIds = reservation.getAttendants().stream()
                .map(a -> a.getUser().getId())
                .filter(id -> !id.equals(userId)) // 예약 신청자 본인 : 초대되었다는 알림 제외
                .toList();

        reservationEventPublisher.publishEventCreated(reservation, attendantUserIds);

        return ReservationResponseDto.fromEntity(reservation);
    }

    @CacheEvict(cacheNames = "appliedReservations", key = "@appliedReservationCachePolicy.defaultKey()")
    @Override
    @Transactional
    public ReservationResponseDto rejectReservation(
            final Long userId,
            final Long reservationId,
            final ConfirmReservationRequestDto confirmReservationRequestDto) {

        User respondent = userReader.findById(userId);

        Reservation reservation = reservationReader.getReservationById(reservationId);

        // 승인자는 자신의 예약 승인 불가
        if (reservation.getApplicant().getId().equals(userId)) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_NOT_APPROVABLE);
        }

        reservation.reject(respondent, confirmReservationRequestDto.getReason()); // status rejected, reason 추가
        reservationSlotManager.deleteSlots(reservationId);

        reservationWriter.save(reservation);

        reservationEventPublisher.publishEventCreated(reservation, null);

        return ReservationResponseDto.fromEntity(reservation);
    }

    @Override
    @Transactional
    public ReservationResponseDto startUsingReservation(final Long userId, final Long reservationId) {

        // 예약자 본인에 대한 확인
        userReader.findById(userId);

        Reservation reservation = reservationReader.getReservationById(reservationId);

        // 지금 사용 불가한 자원이면 제외
        assetCommandService.isAvailable(reservation.getAsset().getId());

        validateReservationStart(reservation.getStartAt(), reservation.getEndAt());

        reservation.start(); // status using, 실제 시작 시간 추가

        reservationWriter.save(reservation);

        return ReservationResponseDto.fromEntity(reservation);
    }

    // 실제 종료 시간 추가
    @Override
    @Transactional
    public ReservationResponseDto endUsingReservation(final Long userId, final Long reservationId) {
        // 예약자 본인에 대한 확인
        userReader.findById(userId);
        Reservation reservation = reservationReader.getReservationById(reservationId);
        reservation.end(); // status complete, 실제 종료 시간 추가
        reservationWriter.save(reservation);

        Asset asset = reservation.getAsset();

        usageHistoryCommandService.createUsageHistory(asset, reservation);

        return ReservationResponseDto.fromEntity(reservation);
    }

    // 예약 취소
    @Override
    @Transactional
    public ReservationResponseDto cancelReservation(final Long userId, final Long reservationId) {

        // 예약자 본인에 대한 확인
        userReader.findById(userId);
        Reservation reservation = reservationReader.getReservationById(reservationId);
        //        validateReservationCanceling(reservation); // 30분 전인 경우 허용
        reservation.cancel();
        reservationSlotManager.deleteSlots(reservationId);

        reservationWriter.save(reservation);

        return ReservationResponseDto.fromEntity(reservation);
    }

    // 예약 정보 수정
    @Override
    @Transactional
    public ReservationResponseDto updateReservation(
            final Long userId,
            final Long reservationId,
            final UpdateReservationRequestDto updateReservationRequestDto) {

        // 예약자 본인에 대한 확인
        userReader.findById(userId);
        Reservation reservation = reservationReader.getReservationById(reservationId);

        if (updateReservationRequestDto.getDescription() != null) {
            reservation.changeDescription(updateReservationRequestDto.getDescription());
        }

        if (updateReservationRequestDto.getStartAt() != null && updateReservationRequestDto.getEndAt() != null) {
            ReservationStatus statusBeforeChange = reservation.getStatus();
            reservationSlotManager.deleteSlots(reservationId);
            reservation.changeSchedule(
                    updateReservationRequestDto.getStartAt(), updateReservationRequestDto.getEndAt());
            if (statusBeforeChange == ReservationStatus.APPROVED) {
                reservationSlotManager.createSlots(reservation, reservation.getAsset());
            }
        }

        // 수정 시 참여자들을 무조건 받는 구조 : id 없는 경우 -> 빈 배열일 때도 이전 추가된 참여들을 위해 삭제해야함
        // 기존 참여자들 삭제
        for (Attendant a : new ArrayList<>(reservation.getAttendants())) {
            reservation.removeAttendant(a); // 양방향 끊기
            attendantJpaRepository.delete(a); // DB에서 삭제
        }

        //
        if (!updateReservationRequestDto.getAttendantIds().isEmpty()) {
            // 추가할 참여자들에 대해 검증
            userReader.validateAllExist(updateReservationRequestDto.getAttendantIds());
            List<User> newAttendants = userReader.findAllByIds(updateReservationRequestDto.getAttendantIds());

            // 예약의 참여자들 변경
            List<Attendant> attendants = reservation.changeAttendants(newAttendants);
            attendantWriter.saveAll(attendants);
        }

        reservationWriter.save(reservation);

        return ReservationResponseDto.fromEntity(reservation);
    }

    // 예약에 대해  soft delete
    @Override
    @Transactional
    public void softDeleteReservation(final Long userId, final Long reservationId) {
        userReader.findById(userId);
        Reservation reservation = reservationReader.getReservationById(reservationId);
        if (reservation.getStatus() == ReservationStatus.USING) {
            throw new ReservationException(ReservationErrorCode.USING_RESERVATION_NOT_DELETED);
        }
        reservation.softDeleteAll(userId); // 예약, 참여자 둘다 soft delete 처리

        // reservation slot 삭제
        reservationSlotManager.deleteSlots(reservationId);

        reservationWriter.save(reservation);
    }

    // 하드 딜리트
    public void hardDeleteReservation(final Long reservationId) {
        reservationSlotManager.deleteSlots(reservationId);
        reservationWriter.hardDelete(reservationId);
    }

    private void validateReservationCanceling(final Reservation reservation) {
        if (!isReservationCancelAvailable(reservation))
            // ddd -> 검증 / service 의 행동 결정(메시지 던짐)
            throw new ReservationException(ReservationErrorCode.RESERVATION_CANCEL_NOT_ALLOWED);
    }

    private boolean isReservationCancelAvailable(final Reservation reservation) {
        Instant now = Instant.now();
        Instant deadline = reservation.getStartAt().minus(30, ChronoUnit.MINUTES);

        if (now.isBefore(deadline)) {
            return true; // 취소 가능
        }
        return false;
    }

    private void validateReservationStart(final Instant startAt, final Instant endAt) {
        // 시작 시간부터 사용 가능
        Instant now = Instant.now();

        if (now.isBefore(startAt)) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_TIME_NOT_YET);
        }

        // 끝나는 시간 이후면 사용 불가
        if (now.isAfter(endAt)) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_TIME_OVER);
        }
    }
}
