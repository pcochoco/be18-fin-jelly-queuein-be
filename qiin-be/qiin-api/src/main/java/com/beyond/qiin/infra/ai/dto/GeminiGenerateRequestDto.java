package com.beyond.qiin.infra.ai.dto;

import java.util.List;

public record GeminiGenerateRequestDto(
        GeminiContent systemInstruction, List<GeminiContent> contents, GeminiGenerationConfig generationConfig) {}
