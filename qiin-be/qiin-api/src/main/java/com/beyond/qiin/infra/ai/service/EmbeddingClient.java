package com.beyond.qiin.infra.ai.service;

import com.beyond.qiin.domain.rag.exception.RagErrorCode;
import com.beyond.qiin.domain.rag.exception.RagException;
import com.beyond.qiin.infra.ai.dto.GeminiContent;
import com.beyond.qiin.infra.ai.dto.GeminiEmbeddingRequestDto;
import com.beyond.qiin.infra.ai.dto.GeminiEmbeddingResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class EmbeddingClient {

    private static final int EMBEDDING_DIMENSION = 3072;

    private final RestClient geminiRestClient;

    @Value("${gemini.embedding-model}")
    private String embeddingModel;

    public String embed(String text) {
        GeminiEmbeddingRequestDto request = new GeminiEmbeddingRequestDto(
                "models/" + embeddingModel,
                GeminiContent.user(text),
                new GeminiEmbeddingRequestDto.EmbedContentConfig(EMBEDDING_DIMENSION));

        GeminiEmbeddingResponseDto response = callGemini(request);

        if (response == null
                || response.embedding() == null
                || response.embedding().values() == null) {
            throw new RagException(RagErrorCode.RAG_EMBEDDING_RESPONSE_EMPTY);
        }

        List<Double> embedding = response.embedding().values();

        return embedding.toString();
    }

    private GeminiEmbeddingResponseDto callGemini(GeminiEmbeddingRequestDto request) {
        try {
            return geminiRestClient
                    .post()
                    .uri("/models/{model}:embedContent", embeddingModel)
                    .body(request)
                    .retrieve()
                    .body(GeminiEmbeddingResponseDto.class);
        } catch (RestClientResponseException e) {
            if (HttpStatus.TOO_MANY_REQUESTS.value() == e.getStatusCode().value()) {
                throw new RagException(RagErrorCode.RAG_EMBEDDING_RATE_LIMIT_EXCEEDED);
            }
            throw new RagException(RagErrorCode.RAG_EMBEDDING_REQUEST_FAILED, e.getMessage());
        }
    }
}
