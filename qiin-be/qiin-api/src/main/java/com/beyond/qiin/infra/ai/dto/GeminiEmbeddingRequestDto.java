package com.beyond.qiin.infra.ai.dto;

public record GeminiEmbeddingRequestDto(String model, GeminiContent content, EmbedContentConfig embedContentConfig) {

    public record EmbedContentConfig(Integer outputDimensionality) {}
}
