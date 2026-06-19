package com.beyond.qiin.domain.chat.exception;

import com.beyond.qiin.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ChatErrorCode implements ErrorCode {
    LLM_RESPONSE_EMPTY(HttpStatus.BAD_GATEWAY, "LLM_RESPONSE_EMPTY", "AI 응답이 비어 있습니다."),
    LLM_INTENT_PARSE_FAILED(HttpStatus.BAD_GATEWAY, "LLM_INTENT_PARSE_FAILED", "AI 의도 분석 응답을 해석하지 못했습니다."),
    LLM_RATE_LIMIT_EXCEEDED(
            HttpStatus.TOO_MANY_REQUESTS, "LLM_RATE_LIMIT_EXCEEDED", "AI 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."),
    LLM_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "LLM_REQUEST_FAILED", "AI 요청에 실패했습니다.");

    private final HttpStatus status;
    private final String error;
    private final String message;

    ChatErrorCode(final HttpStatus status, final String error, final String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
