package com.beyond.qiin.infra.ai.dto;

import java.util.List;

public record GeminiGenerateResponseDto(List<Candidate> candidates) {

    public record Candidate(GeminiContent content) {}
}
