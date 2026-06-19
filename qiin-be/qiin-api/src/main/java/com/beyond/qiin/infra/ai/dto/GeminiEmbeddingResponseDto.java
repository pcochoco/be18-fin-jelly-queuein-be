package com.beyond.qiin.infra.ai.dto;

import java.util.List;

public record GeminiEmbeddingResponseDto(ContentEmbedding embedding) {

    public record ContentEmbedding(List<Double> values) {}
}
