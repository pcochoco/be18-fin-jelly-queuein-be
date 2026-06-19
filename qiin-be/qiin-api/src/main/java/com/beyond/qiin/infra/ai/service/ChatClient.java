package com.beyond.qiin.infra.ai.service;

import com.beyond.qiin.domain.chat.dto.IntentResultDto;
import com.beyond.qiin.domain.chat.exception.ChatErrorCode;
import com.beyond.qiin.domain.chat.exception.ChatException;
import com.beyond.qiin.infra.ai.dto.GeminiContent;
import com.beyond.qiin.infra.ai.dto.GeminiGenerateRequestDto;
import com.beyond.qiin.infra.ai.dto.GeminiGenerateResponseDto;
import com.beyond.qiin.infra.ai.dto.GeminiGenerationConfig;
import com.beyond.qiin.infra.ai.dto.GeminiPart;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class ChatClient {
    private static final String CHAT_SYSTEM_PROMPT = "당신은 QueueIn 예약 시스템 비서입니다.";

    private static final String INTENT_SYSTEM_PROMPT =
            """
            당신은 QueueIn 예약 시스템의 챗봇 분석 엔진입니다.
            사용자의 자연어 입력을 읽고, 아래 8개의 Intent 중 하나로 반드시 분류하여 JSON 형태로 출력하세요.

            반드시 아래 JSON 스키마를 사용하세요:

            {
              "intent": "INTENT_NAME",
              "params": { ... }
            }

            ========================================
            [가능한 Intent 목록과 규칙]
            ========================================

            1) LIST_ASSETS_BY_CATEGORY
            - 사용자가 "카테고리 X"라고 말하며 해당 카테고리의 자원 목록을 요청할 때 사용합니다.
            - params: { "categoryId": number }

            2) GET_ASSET_DETAIL
            - 특정 자원의 상세 정보를 요청할 때 사용합니다.
            - params: { "assetId": number, "assetName": string }
            - assetId 또는 assetName 중 하나만 있어도 됩니다.

            3) LIST_AVAILABLE_ASSETS
            - 사용자가 “예약 가능한”, “사용 가능한”, “빌릴 수 있는” 등의 표현을 사용하여 자원을 찾을 때 선택합니다.
            - params: { "categoryId": number (optional), "keyword": string (optional) }

            4) GET_ASSET_LOCATION
            - 사용자가 특정 자원의 위치를 물어볼 때 사용합니다.
            - params: { "assetId": number, "assetName": string }

            5) GET_ASSET_STATUS
            - 사용자가 특정 자원의 현재 상태를 물어볼 때 사용합니다.
            - params: { "assetId": number, "assetName": string }

            6) LIST_ALL_CATEGORIES
            - 사용자가 전체 카테고리 목록을 요청할 때 사용합니다.
            - params: {}

            7) RAG_SEARCH
            - 사용자가 QueueIn 요구사항 정의서, 요구사항, 정책, 기능 명세, 문서 내용에 대해 질문할 때 사용합니다.
            - params: { "query": string }
            - query에는 사용자의 질문 원문을 그대로 넣으십시오.

            8) UNKNOWN
            - 위 Intent 목록 중 어느 것에도 명확하게 속하지 않으면 사용합니다.
            - params: {}

            ========================================
            [출력 규칙]
            ========================================
            - JSON 외 텍스트는 절대 출력하지 마십시오.
            - intent는 반드시 위 목록 중 정확히 하나를 선택하십시오.
            - 숫자는 숫자로, 문자열은 문자열로 출력하십시오.
            - assetId, categoryId는 숫자로만 출력하십시오.
            - 이름 기반 요청은 params.assetName에 그대로 넣으십시오.
            - 해석이 불가능하면 반드시 UNKNOWN을 선택하십시오.
            """;

    // RAG 답변용
    private static final String RAG_SYSTEM_PROMPT =
            """
            당신은 QueueIn 예약 시스템의 문서 기반 안내 비서입니다.

            반드시 제공된 [문서 내용]만 근거로 답변하세요.
            문서에 없는 내용은 추측하지 말고 "제공된 문서에서 확인할 수 없습니다."라고 답하세요.

            답변 규칙:
            - 사용자가 이해하기 쉽게 자연스럽게 설명합니다.
            - 문서 내용과 관련 없는 일반 지식은 사용하지 않습니다.
            - 확실하지 않은 내용은 단정하지 않습니다.
            - 필요한 경우 핵심 내용을 짧게 정리합니다.
            """;

    private final RestClient geminiRestClient;

    private final ObjectMapper objectMapper;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.temperature}")
    private Double temperature;

    public String sendMessage(String userMessage) {
        GeminiGenerateRequestDto request = createChatRequest(userMessage);
        GeminiGenerateResponseDto response = callGemini(request);

        return extractContent(response);
    }

    public String answerWithRag(String userQuestion, String context) {
        GeminiGenerateRequestDto request = createRagRequest(userQuestion, context);
        GeminiGenerateResponseDto response = callGemini(request);

        return extractContent(response);
    }

    public IntentResultDto extractIntent(String userMessage) {
        GeminiGenerateRequestDto request = createIntentRequest(userMessage);
        GeminiGenerateResponseDto response = callGemini(request);

        String raw = extractContent(response);
        String json = cleanJson(raw);

        return parseIntent(json);
    }

    private GeminiGenerateRequestDto createChatRequest(String userMessage) {
        return createRequest(CHAT_SYSTEM_PROMPT, temperature, userMessage);
    }

    private GeminiGenerateRequestDto createIntentRequest(String userMessage) {
        return createRequest(INTENT_SYSTEM_PROMPT, 0.0, userMessage);
    }

    private GeminiGenerateRequestDto createRagRequest(String userQuestion, String context) {
        return createRequest(RAG_SYSTEM_PROMPT, 0.0, "[문서 내용]\n" + context + "\n\n[사용자 질문]\n" + userQuestion);
    }

    private GeminiGenerateRequestDto createRequest(String systemPrompt, Double temperature, String userMessage) {
        return new GeminiGenerateRequestDto(
                GeminiContent.system(systemPrompt),
                List.of(GeminiContent.user(userMessage)),
                new GeminiGenerationConfig(temperature));
    }

    private GeminiGenerateResponseDto callGemini(GeminiGenerateRequestDto request) {
        try {
            return geminiRestClient
                    .post()
                    .uri("/models/{model}:generateContent", model)
                    .body(request)
                    .retrieve()
                    .body(GeminiGenerateResponseDto.class);
        } catch (RestClientResponseException e) {
            if (HttpStatus.TOO_MANY_REQUESTS.value() == e.getStatusCode().value()) {
                throw new ChatException(ChatErrorCode.LLM_RATE_LIMIT_EXCEEDED);
            }
            throw new ChatException(ChatErrorCode.LLM_REQUEST_FAILED, e.getMessage());
        }
    }

    private String extractContent(GeminiGenerateResponseDto response) {
        if (response == null
                || response.candidates() == null
                || response.candidates().isEmpty()
                || response.candidates().get(0).content() == null
                || response.candidates().get(0).content().parts() == null
                || response.candidates().get(0).content().parts().isEmpty()) {
            throw new ChatException(ChatErrorCode.LLM_RESPONSE_EMPTY);
        }

        return response.candidates().get(0).content().parts().stream()
                .map(GeminiPart::text)
                .filter(text -> text != null && !text.isBlank())
                .reduce("", String::concat);
    }

    // json 문자열을 java 객체로
    private IntentResultDto parseIntent(String json) {
        try {
            return objectMapper.readValue(json, IntentResultDto.class);
        } catch (Exception e) {
            throw new ChatException(ChatErrorCode.LLM_INTENT_PARSE_FAILED, "Intent JSON 파싱 실패: " + json);
        }
    }

    // 답변을 순수 json 문자열로
    private String cleanJson(String raw) {
        if (raw == null) {
            throw new ChatException(ChatErrorCode.LLM_RESPONSE_EMPTY);
        }

        String cleaned = raw.replace("```json", "").replace("```", "").trim();

        int start = cleaned.indexOf("{");
        int end = cleaned.lastIndexOf("}");

        if (start == -1 || end == -1) {
            throw new ChatException(ChatErrorCode.LLM_INTENT_PARSE_FAILED, "LLM 응답에서 JSON 블록을 찾을 수 없습니다: " + cleaned);
        }

        return cleaned.substring(start, end + 1);
    }
}
