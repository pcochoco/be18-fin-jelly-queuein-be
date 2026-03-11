package com.beyond.qiin.domain.booking.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beyond.qiin.common.dto.PageResponseDto;
import com.beyond.qiin.domain.booking.controller.ReservationController;
import com.beyond.qiin.domain.booking.dto.reservation.request.ConfirmReservationRequestDto;
import com.beyond.qiin.domain.booking.dto.reservation.request.CreateReservationRequestDto;
import com.beyond.qiin.domain.booking.dto.reservation.request.UpdateReservationRequestDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.ReservationDetailResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.ReservationResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.applied_reservation.GetAppliedReservationResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.attendant.AttendantResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.response.user_reservation.GetUserReservationResponseDto;
import com.beyond.qiin.domain.booking.service.command.ReservationCommandService;
import com.beyond.qiin.domain.booking.service.query.ReservationQueryService;
import com.beyond.qiin.security.CustomUserDetails;
import com.beyond.qiin.security.config.SecurityConfig;
import com.beyond.qiin.security.jwt.JwtFilter;
import com.beyond.qiin.security.jwt.JwtTokenProvider;
import com.beyond.qiin.security.jwt.RedisTokenRepository;
import com.beyond.qiin.security.resolver.ArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = ReservationController.class,
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class)
        })
@AutoConfigureMockMvc(addFilters = false)
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationCommandService reservationCommandService;

    @MockBean
    private ReservationQueryService reservationQueryService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RedisTokenRepository redisTokenRepository;

    @MockBean
    private ArgumentResolver argumentResolver;

    @Test
    @WithMockUser(
            username = "testUser",
            roles = {"GENERAL"})
    void applyReservation_success() throws Exception {
        Instant start = Instant.parse("2025-12-12T10:00:00Z");
        Instant end = Instant.parse("2025-12-12T11:00:00Z");

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        CustomUserDetails mockUser = new CustomUserDetails(1L, "tester", authorities);

        CreateReservationRequestDto request = CreateReservationRequestDto.builder()
                .startAt(start)
                .endAt(end)
                .description("팀 회의")
                .attendantIds(List.of(2L, 3L))
                .build();

        AttendantResponseDto at1 = AttendantResponseDto.builder()
                .attendantId(2L)
                .attendantName("홍길동")
                .build();

        AttendantResponseDto at2 = AttendantResponseDto.builder()
                .attendantId(3L)
                .attendantName("김철수")
                .build();

        ReservationResponseDto response = ReservationResponseDto.builder()
                .reservationId(100L)
                .assetName("회의실 A")
                .applicantName("사용자1")
                .startAt(start)
                .endAt(end)
                .actualStartAt(null)
                .actualEndAt(null)
                .description("팀 회의")
                .reason(null)
                .version(1L)
                .isApproved(false)
                .status("WAITING")
                .createdAt(Instant.now())
                .createdBy(1L)
                .updatedAt(Instant.now())
                .updatedBy(1L)
                .attendants(List.of(at1, at2))
                .build();

        when(jwtTokenProvider.getUserId(any())).thenReturn(1L);
        when(reservationCommandService.applyReservation(any(), any(), any())).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/reservations/1/apply") // assetId = 1
                                .with(authentication(new UsernamePasswordAuthenticationToken(
                                        mockUser, null, mockUser.getAuthorities())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationId").value(100L))
                .andExpect(jsonPath("$.applicantName").value("사용자1"))
                .andExpect(jsonPath("$.assetName").value("회의실 A"))
                .andExpect(jsonPath("$.description").value("팀 회의"))
                .andExpect(jsonPath("$.status").value("WAITING"))
                // attendants 검증
                .andExpect(jsonPath("$.attendants[0].attendantId").value(2L))
                .andExpect(jsonPath("$.attendants[0].attendantName").value("홍길동"))
                .andExpect(jsonPath("$.attendants[1].attendantId").value(3L))
                .andExpect(jsonPath("$.attendants[1].attendantName").value("김철수"));
    }

    @Test
    @WithMockUser(
            username = "testUser",
            roles = {"GENERAL"})
    void getReservation_success() throws Exception {

        Long reservationId = 30L;

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_GENERAL"));

        CustomUserDetails mockUser = new CustomUserDetails(1L, "tester", authorities);

        ReservationDetailResponseDto detailDto = ReservationDetailResponseDto.builder()
                .reservationId(reservationId)
                .assetName("회의실 B")
                .applicantName("홍길동")
                .description("팀 브리핑")
                .reservationStatus("APPROVED")
                .startAt(Instant.parse("2025-12-12T09:00:00Z"))
                .endAt(Instant.parse("2025-12-12T10:00:00Z"))
                .attendants(List.of()) // null 방지
                .build();

        // jwt mocking
        when(jwtTokenProvider.getUserId(any())).thenReturn(1L);

        // query mocking
        when(reservationQueryService.getReservation(any(), any())).thenReturn(detailDto);

        mockMvc.perform(get("/api/v1/reservations/" + reservationId)
                        .header("accessToken", "mock-token")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(reservationId))
                .andExpect(jsonPath("$.assetName").value("회의실 B"))
                .andExpect(jsonPath("$.applicantName").value("홍길동"))
                .andExpect(jsonPath("$.description").value("팀 브리핑"))
                .andExpect(jsonPath("$.reservationStatus").value("APPROVED"));
    }

    @Test
    @WithMockUser(
            username = "adminUser",
            roles = {"ADMIN"})
    void approveReservation_success() throws Exception {

        Long reservationId = 50L;

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

        CustomUserDetails mockUser = new CustomUserDetails(1L, "adminUser", authorities);

        // 요청 DTO
        ConfirmReservationRequestDto requestDto = ConfirmReservationRequestDto.builder()
                .version(1L)
                .reason("승인합니다")
                .build();

        // 응답 DTO
        ReservationResponseDto responseDto = ReservationResponseDto.builder()
                .reservationId(reservationId)
                .assetName("세미나실 A")
                .applicantName("홍길동")
                .description("세미나 준비")
                .status("APPROVED")
                .isApproved(true)
                .startAt(Instant.parse("2025-12-12T09:00:00Z"))
                .endAt(Instant.parse("2025-12-12T10:00:00Z"))
                .attendants(List.of())
                .build();

        // Mock 설정
        when(jwtTokenProvider.getUserId(any())).thenReturn(1L);
        when(reservationCommandService.approveReservation(any(), any(), any())).thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/reservations/" + reservationId + "/approve")
                        .header("accessToken", "mock-token")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(reservationId))
                .andExpect(jsonPath("$.assetName").value("세미나실 A"))
                .andExpect(jsonPath("$.applicantName").value("홍길동"))
                .andExpect(jsonPath("$.description").value("세미나 준비"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.isApproved").value(true));
    }

    @Test
    @WithMockUser(
            username = "adminUser",
            roles = {"ADMIN"})
    void rejectReservation_success() throws Exception {

        Long reservationId = 70L;

        // 사용자 권한 설정
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

        CustomUserDetails mockUser = new CustomUserDetails(1L, "adminUser", authorities);

        // 요청 DTO
        ConfirmReservationRequestDto requestDto = ConfirmReservationRequestDto.builder()
                .version(1L)
                .reason("사유: 일정 충돌")
                .build();

        // 응답 DTO
        ReservationResponseDto responseDto = ReservationResponseDto.builder()
                .reservationId(reservationId)
                .assetName("회의실 C")
                .applicantName("이영희")
                .description("업무 공유 미팅")
                .status("REJECTED")
                .reason("사유: 일정 충돌")
                .isApproved(false)
                .startAt(Instant.parse("2025-12-12T15:00:00Z"))
                .endAt(Instant.parse("2025-12-12T16:00:00Z"))
                .attendants(List.of())
                .build();

        // Mock 동작 설정
        when(jwtTokenProvider.getUserId(any())).thenReturn(1L);
        when(reservationCommandService.rejectReservation(any(), any(), any())).thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/reservations/" + reservationId + "/reject")
                        .header("accessToken", "mock-token")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(reservationId))
                .andExpect(jsonPath("$.assetName").value("회의실 C"))
                .andExpect(jsonPath("$.applicantName").value("이영희"))
                .andExpect(jsonPath("$.description").value("업무 공유 미팅"))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.reason").value("사유: 일정 충돌"))
                .andExpect(jsonPath("$.isApproved").value(false));
    }

    @Test
    @WithMockUser(
            username = "testUser",
            roles = {"GENERAL"})
    void cancelReservation_success() throws Exception {

        Long reservationId = 77L;

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_GENERAL"));

        CustomUserDetails mockUser = new CustomUserDetails(1L, "tester", authorities);

        // 응답 DTO
        ReservationResponseDto responseDto = ReservationResponseDto.builder()
                .reservationId(reservationId)
                .assetName("회의실 C")
                .applicantName("tester")
                .description("취소된 예약")
                .status("CANCELED")
                .startAt(Instant.parse("2025-12-12T11:00:00Z"))
                .endAt(Instant.parse("2025-12-12T12:00:00Z"))
                .attendants(List.of())
                .build();

        // Mock 설정
        when(jwtTokenProvider.getUserId(any())).thenReturn(1L);
        when(reservationCommandService.cancelReservation(any(), any())).thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/reservations/" + reservationId + "/cancel")
                        .header("accessToken", "mock-token")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(reservationId))
                .andExpect(jsonPath("$.assetName").value("회의실 C"))
                .andExpect(jsonPath("$.applicantName").value("tester"))
                .andExpect(jsonPath("$.description").value("취소된 예약"))
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    @WithMockUser(
            username = "testUser",
            roles = {"GENERAL"})
    void startUsingReservation_success() throws Exception {

        Long reservationId = 55L;

        // 권한 생성
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_GENERAL"));

        CustomUserDetails mockUser = new CustomUserDetails(1L, "tester", authorities);

        // 실제 시작 시간은 now()로 들어간다고 가정 → 테스트에서 명시적으로 고정
        Instant now = Instant.parse("2025-12-12T09:00:00Z");

        // 응답 DTO
        ReservationResponseDto responseDto = ReservationResponseDto.builder()
                .reservationId(reservationId)
                .assetName("회의실 A")
                .applicantName("tester")
                .description("업무 회의")
                .status("USING")
                .actualStartAt(now)
                .startAt(Instant.parse("2025-12-12T09:00:00Z"))
                .endAt(Instant.parse("2025-12-12T10:00:00Z"))
                .attendants(List.of())
                .build();

        // Mock 설정
        when(jwtTokenProvider.getUserId(any())).thenReturn(1L);
        when(reservationCommandService.startUsingReservation(any(), any())).thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/reservations/" + reservationId + "/check-in")
                        .header("accessToken", "mock-token")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(reservationId))
                .andExpect(jsonPath("$.assetName").value("회의실 A"))
                .andExpect(jsonPath("$.applicantName").value("tester"))
                .andExpect(jsonPath("$.status").value("USING"))
                .andExpect(jsonPath("$.actualStartAt").value(now.toString()));
    }

    @Test
    @WithMockUser(
            username = "testUser",
            roles = {"MASTER"})
    void updateReservation_success() throws Exception {

        Long reservationId = 10L;

        Instant newStart = Instant.parse("2025-12-12T13:00:00Z");
        Instant newEnd = Instant.parse("2025-12-12T14:00:00Z");

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("MASTER"));

        CustomUserDetails mockUser = new CustomUserDetails(1L, "testUser", authorities);

        UpdateReservationRequestDto request = UpdateReservationRequestDto.builder()
                .version(1L)
                .description("설명 수정됨")
                .startAt(newStart)
                .endAt(newEnd)
                .attendantIds(List.of(5L, 6L))
                .build();

        ReservationResponseDto response = ReservationResponseDto.builder()
                .reservationId(reservationId)
                .assetName("회의실 A")
                .applicantName("testerUser")
                .description("설명 수정됨")
                .status("PENDING")
                .startAt(newStart)
                .endAt(newEnd)
                .attendants(List.of(
                        AttendantResponseDto.builder()
                                .attendantId(5L)
                                .attendantName("참여자1")
                                .build(),
                        AttendantResponseDto.builder()
                                .attendantId(6L)
                                .attendantName("참여자2")
                                .build()))
                .build();

        when(jwtTokenProvider.getUserId(any())).thenReturn(1L);
        when(reservationCommandService.updateReservation(any(), any(), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/reservations/" + reservationId)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(reservationId))
                .andExpect(jsonPath("$.description").value("설명 수정됨"))
                .andExpect(jsonPath("$.startAt").value(newStart.toString()))
                .andExpect(jsonPath("$.endAt").value(newEnd.toString()))
                .andExpect(jsonPath("$.attendants[0].attendantId").value(5L))
                .andExpect(jsonPath("$.attendants[1].attendantId").value(6L));
    }

    @Test
    @WithMockUser(
            username = "testUser",
            roles = {"MASTER"})
    void deleteReservation_success() throws Exception {

        Long reservationId = 20L;

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("MASTER"));

        CustomUserDetails mockUser = new CustomUserDetails(1L, "tester", authorities);

        // JWT mock
        when(jwtTokenProvider.getUserId(any())).thenReturn(1L);

        // softDeleteReservation은 void → doNothing 사용
        doNothing().when(reservationCommandService).softDeleteReservation(any(), any());

        mockMvc.perform(delete("/api/v1/reservations/" + reservationId)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(
            username = "testUser",
            roles = {"GENERAL"})
    void getUserReservations_success() throws Exception {

        Long userId = 1L;

        // 🔥 검색 조건 필수값(LocalDate)
        String date = "2025-12-12";

        // 응답 DTO 1건
        GetUserReservationResponseDto dto = GetUserReservationResponseDto.builder()
                .reservationId(10L)
                .assetName("회의실 A")
                .isApproved(true)
                .startAt(Instant.parse("2025-12-12T10:00:00Z"))
                .endAt(Instant.parse("2025-12-12T11:00:00Z"))
                .reservationStatus("APPROVED")
                .version(1L)
                .actualStartAt(null)
                .actualEndAt(null)
                .build();

        Page<GetUserReservationResponseDto> pageImpl = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        PageResponseDto<GetUserReservationResponseDto> pageDto = PageResponseDto.from(pageImpl);

        when(jwtTokenProvider.getUserId(any())).thenReturn(userId);
        when(reservationQueryService.getReservationsByUserId(any(), any(), any()))
                .thenReturn(pageDto);

        mockMvc.perform(get("/api/v1/reservations/me")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                new CustomUserDetails(
                                        userId, "testUser", List.of(new SimpleGrantedAuthority("ROLE_GENERAL"))),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_GENERAL")))))
                        .param("date", date)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reservationId").value(10L))
                .andExpect(jsonPath("$.content[0].assetName").value("회의실 A"))
                .andExpect(jsonPath("$.content[0].assetStatus").value("AVAILABLE"))
                .andExpect(jsonPath("$.content[0].reservationStatus").value("APPROVED"))
                .andExpect(jsonPath("$.content[0].isApproved").value(true));
    }

    @Test
    @WithMockUser(
            username = "adminUser",
            roles = {"ADMIN"})
    void getAppliedReservations_success() throws Exception {

        Long userId = 1L;

        // 검색 조건용 파라미터
        String date = "2025-12-12";

        GetAppliedReservationResponseDto dto = GetAppliedReservationResponseDto.builder()
                .assetName("회의실 A")
                .reservationId(100L)
                .applicantName("홍길동")
                .respondentName("관리자")
                .reservationStatus("PENDING")
                .isApproved(false)
                .isReservable(true)
                .reason(null)
                .version(1L)
                .build();

        // PageResponseDto mock 생성
        Page<GetAppliedReservationResponseDto> pageImpl = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        PageResponseDto<GetAppliedReservationResponseDto> pageDto = PageResponseDto.from(pageImpl);

        when(jwtTokenProvider.getUserId(any())).thenReturn(userId);
        when(reservationQueryService.getReservationApplies(any(), any(), any())).thenReturn(pageDto);

        mockMvc.perform(get("/api/v1/reservations/pending")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                new CustomUserDetails(
                                        userId, "adminUser", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("date", date)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reservationId").value(100L))
                .andExpect(jsonPath("$.content[0].assetName").value("회의실 A"))
                .andExpect(jsonPath("$.content[0].applicantName").value("홍길동"));
    }
}
