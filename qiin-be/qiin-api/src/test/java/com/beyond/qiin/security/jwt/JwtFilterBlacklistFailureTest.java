package com.beyond.qiin.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

class JwtFilterBlacklistFailureTest {

    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final RedisTokenRepository redisTokenRepository = mock(RedisTokenRepository.class);
    private final AuthenticationEntryPoint authenticationEntryPoint = mock(AuthenticationEntryPoint.class);
    private final JwtFilter jwtFilter = new JwtFilter(jwtTokenProvider, redisTokenRepository, authenticationEntryPoint);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("blacklist에 없는 정상 토큰은 인증되고 다음 filter로 전달된다")
    void allowsTokenWhenBlacklistDoesNotContainIt() throws Exception {
        String token = "valid-token";
        Claims claims = mock(Claims.class);
        when(redisTokenRepository.isBlacklisted(token)).thenReturn(false);
        when(jwtTokenProvider.getClaims(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("1");
        when(claims.get("role", String.class)).thenReturn("ROLE_USER");
        when(claims.get("email", String.class)).thenReturn("user@example.com");
        when(claims.get("permissions", List.class)).thenReturn(List.of("reservation:read"));

        MockHttpServletRequest request = bearerRequest(token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        var filterChain = mock(jakarta.servlet.FilterChain.class);

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("blacklist에 등록된 토큰은 기존 401 entry point로 거부된다")
    void rejectsBlacklistedTokenWithExistingUnauthorizedFlow() throws Exception {
        String token = "blacklisted-token";
        when(redisTokenRepository.isBlacklisted(token)).thenReturn(true);
        MockHttpServletRequest request = bearerRequest(token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        var filterChain = mock(jakarta.servlet.FilterChain.class);

        jwtFilter.doFilter(request, response, filterChain);

        verify(authenticationEntryPoint)
                .commence(
                        org.mockito.ArgumentMatchers.same(request),
                        org.mockito.ArgumentMatchers.same(response),
                        org.mockito.ArgumentMatchers.any());
        verify(filterChain, never()).doFilter(request, response);
        verify(jwtTokenProvider, never()).getClaims(token);
    }

    @Test
    @DisplayName("blacklist Redis timeout 또는 connection failure는 즉시 503으로 인증을 차단한다")
    void rejectsWhenBlacklistCannotBeChecked() throws Exception {
        String token = "unverified-token";
        when(redisTokenRepository.isBlacklisted(token))
                .thenThrow(new BlacklistCheckUnavailableException(new RuntimeException("Redis command timeout")));
        MockHttpServletRequest request = bearerRequest(token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        var filterChain = mock(jakarta.servlet.FilterChain.class);

        long startedAt = System.nanoTime();
        jwtFilter.doFilter(request, response, filterChain);
        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getContentAsString()).contains("SERVICE_UNAVAILABLE");
        assertThat(elapsedMillis).isLessThan(100L);
        verify(filterChain, never()).doFilter(request, response);
        verify(authenticationEntryPoint, never())
                .commence(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any());
    }

    private MockHttpServletRequest bearerRequest(final String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/reservations");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
