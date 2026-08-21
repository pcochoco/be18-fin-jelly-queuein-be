package com.beyond.qiin.domain.booking.service.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;

import com.beyond.qiin.domain.accounting.service.command.UsageHistoryCommandService;
import com.beyond.qiin.domain.booking.dto.reservation.request.ConfirmReservationRequestDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.ReservationResponseDto;
import com.beyond.qiin.domain.booking.entity.Reservation;
import com.beyond.qiin.domain.booking.enums.ReservationStatus;
import com.beyond.qiin.domain.booking.event.ReservationEventPublisher;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class ApproveReservationTest {
    @Spy
    @InjectMocks
    private ReservationCommandServiceImpl reservationCommandService;

    @Mock
    private UserReader userReader;

    @Mock
    private ReservationReader reservationReader;

    @Mock
    private ReservationWriter reservationWriter;

    @Mock
    private AttendantWriter attendantWriter;

    @Mock
    private AssetCommandService assetCommandService;

    @Mock
    private ReservationEventPublisher reservationEventPublisher;

    @Mock
    private AttendantJpaRepository attendantJpaRepository;

    @Mock
    private UsageHistoryCommandService usageHistoryCommandService;

    @Mock
    private ReservationSlotManager reservationSlotManager;

    @Mock
    private ReservationSlotJpaRepository reservationSlotJpaRepository;

    @Test
    void approveReservation_success() {
        Long userId = 1L;
        Long reservationId = 10L;

        ConfirmReservationRequestDto dto =
                ConfirmReservationRequestDto.builder().reason("승인 이유").build();

        User user = User.builder().userName("tester").build();
        User applicant = User.builder().userName("신청자").build();
        Asset asset = Asset.builder().name("회의실").build();
        Instant startAt = Instant.now().plusSeconds(3600);
        Instant endAt = startAt.plusSeconds(3600);
        Reservation reservation = Mockito.spy(Reservation.builder()
                .asset(asset)
                .applicant(applicant)
                .startAt(startAt)
                .endAt(endAt)
                .build());

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(applicant, "id", 2L);
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        Mockito.when(userReader.findById(userId)).thenReturn(user);
        Mockito.when(reservationReader.getReservationById(reservationId)).thenReturn(reservation);
        Mockito.when(assetCommandService.getAssetById(asset.getId())).thenReturn(asset);

        ReservationResponseDto response = reservationCommandService.approveReservation(userId, reservationId, dto);

        Mockito.verify(reservation).approve(eq(user), eq("승인 이유"), any(Instant.class));
        Mockito.verify(reservationSlotManager).createSlots(reservation, asset);
        Mockito.verify(reservationWriter).save(reservation);
        Mockito.verify(reservationEventPublisher).publishEventCreated(any(Reservation.class), anyList());

        assertNotNull(response);
        assertEquals(ReservationStatus.APPROVED, reservation.getStatus());
    }
}
