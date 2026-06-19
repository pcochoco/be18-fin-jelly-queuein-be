package com.beyond.qiin.infra.ai.dto;

import java.util.List;

public record GeminiContent(String role, List<GeminiPart> parts) {

    public static GeminiContent user(String text) {
        return new GeminiContent("user", List.of(new GeminiPart(text)));
    }

    public static GeminiContent model(String text) {
        return new GeminiContent("model", List.of(new GeminiPart(text)));
    }

    public static GeminiContent system(String text) {
        return new GeminiContent(null, List.of(new GeminiPart(text)));
    }
}
