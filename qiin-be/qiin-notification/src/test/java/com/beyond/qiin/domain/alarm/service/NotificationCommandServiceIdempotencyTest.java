package com.beyond.qiin.domain.alarm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.beyond.qiin.domain.alarm.repository.NotificationJpaRepository;
import com.beyond.qiin.infra.kafka.reservation.event.ReservationEventPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class NotificationCommandServiceIdempotencyTest {

    @Autowired
    private NotificationJpaRepository notificationJpaRepository;

    private SseService sseService;
    private NotificationCommandServiceImpl notificationCommandService;

    @BeforeEach
    void setUp() {
        sseService = mock(SseService.class);
        notificationCommandService =
                new NotificationCommandServiceImpl(notificationJpaRepository, sseService, new ObjectMapper());
    }

    @Test
    void sameOutboxEventForSameReceiverIsStoredOnlyOnce() {
        UUID outboxId = UUID.randomUUID();
        ReservationEventPayload payload = ReservationEventPayload.builder()
                .outboxId(outboxId)
                .eventType("CREATED")
                .reservationId(1L)
                .assetId(2L)
                .applicantId(3L)
                .respondentId(4L)
                .startAt("2026-08-18T10:00:00")
                .endAt("2026-08-18T11:00:00")
                .status("CREATED")
                .attendantUserIds(List.of())
                .build();

        notificationCommandService.notifyEvent(payload);

        // 2번째 notifyEvent 호출
        // 내부에서 sendNotification 시 db에서 동일한 notification 있는 경우 skip하는 구조
        assertThatCode(() -> notificationCommandService.notifyEvent(payload)).doesNotThrowAnyException();
        assertThat(notificationJpaRepository.count()).isEqualTo(1);
        verify(sseService, times(1)).send(org.mockito.ArgumentMatchers.eq(3L), org.mockito.ArgumentMatchers.any());
    }
}
