package com.beyond.qiin.domain.alarm.controller;

import com.beyond.qiin.domain.alarm.service.SseService;
import com.beyond.qiin.security.jwt.JwtTokenProvider;
import com.beyond.qiin.security.resolver.CurrentUserId;
import com.beyond.qiin.security.resolver.SseUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SseController {
    private final JwtTokenProvider jwtTokenProvider;
    private final SseService sseService;

    // SSE 구독 - authorization header 사용 x(preauthorize : 권한 필요하지 않으므로 생략)
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@SseUserId Long userId) {

        return sseService.connect(userId);
    }

    // SSE 구독 해제 - authorization header 사용 x(preauthorize : 권한 필요하지 않으므로 생략)
    @DeleteMapping("/unsubscribe")
    public void unsubscribe(@CurrentUserId Long userId) {

        sseService.disconnect(userId);
    }
}
