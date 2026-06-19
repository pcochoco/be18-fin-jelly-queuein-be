package com.beyond.qiin.domain.rag.exception;

import com.beyond.qiin.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum RagErrorCode implements ErrorCode {
    RAG_CHUNK_EMPTY(HttpStatus.BAD_REQUEST, "RAG_CHUNK_EMPTY", "저장할 문서 청크가 없습니다."),
    RAG_CHUNKING_STRATEGY_NOT_SUPPORTED(
            HttpStatus.BAD_REQUEST, "RAG_CHUNKING_STRATEGY_NOT_SUPPORTED", "지원하지 않는 문서 청킹 전략입니다."),
    RAG_EMBEDDING_RESPONSE_EMPTY(HttpStatus.BAD_GATEWAY, "RAG_EMBEDDING_RESPONSE_EMPTY", "AI 임베딩 응답이 비어 있습니다."),
    RAG_EMBEDDING_RATE_LIMIT_EXCEEDED(
            HttpStatus.TOO_MANY_REQUESTS, "RAG_EMBEDDING_RATE_LIMIT_EXCEEDED", "AI 임베딩 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."),
    RAG_EMBEDDING_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "RAG_EMBEDDING_REQUEST_FAILED", "AI 임베딩 요청에 실패했습니다.");

    private final HttpStatus status;
    private final String error;
    private final String message;

    RagErrorCode(final HttpStatus status, final String error, final String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
