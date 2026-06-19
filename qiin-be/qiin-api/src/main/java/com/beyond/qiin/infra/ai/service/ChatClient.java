package com.beyond.qiin.infra.ai.service;

import com.beyond.qiin.domain.chat.dto.IntentResultDto;
import com.beyond.qiin.infra.ai.dto.OpenAiMessage;
import com.beyond.qiin.infra.ai.dto.OpenAiRequestDto;
import com.beyond.qiin.infra.ai.dto.OpenAiResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

//일반 답 생성, intent 추출용
@Component
@RequiredArgsConstructor
public class ChatbotClient {
    private static final String CHAT_SYSTEM_PROMPT =
            "당신은 QueueIn 예약 시스템 비서입니다.";

    private static final String INTENT_SYSTEM_PROMPT =
            """
            당신은 QueueIn 예약 시스템의 챗봇 분석 엔진입니다.
            사용자의 자연어 입력을 읽고, 아래 7개의 Intent 중 하나로 반드시 분류하여 JSON 형태로 출력하세요.

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

            7) UNKNOWN
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

    private final RestClient openAiRestClient;

    private final ObjectMapper objectMapper;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.temperature}")
    private Double temperature;

    public String sendMessage(String userMessage) {
        OpenAiRequestDto request = createChatRequest(userMessage);
        OpenAiResponseDto response = callOpenAi(request);

        return extractContent(response);
    }

    public String answerWithRag(String userQuestion, String context) {
        OpenAiRequestDto request = createRagRequest(userQuestion, context);
        OpenAiResponseDto response = callOpenAi(request);

        return extractContent(response);
    }

    public IntentResultDto extractIntent(String userMessage) {
        OpenAiRequestDto request = createIntentRequest(userMessage);
        OpenAiResponseDto response = callOpenAi(request);

        String raw = extractContent(response);
        String json = cleanJson(raw);

        return parseIntent(json);
    }

    //챗봇용 요청
    private OpenAiRequestDto createChatRequest(String userMessage) {
        return createRequest(
                temperature,
                List.of(
                        new OpenAiMessage("system", CHAT_SYSTEM_PROMPT),
                        new OpenAiMessage("user", userMessage)
                )
        );
    }

    //intent 분석용 요청 생성
    private OpenAiRequestDto createIntentRequest(String userMessage) {
        return createRequest(
                0.0,
                List.of(
                        new OpenAiMessage("system", INTENT_SYSTEM_PROMPT),
                        new OpenAiMessage("user", userMessage)
                )
        );
    }

    //dto 생성 담당
    private OpenAiRequestDto createRequest(
            Double temperature,
            List<OpenAiMessage> messages
    ) {
        OpenAiRequestDto request = new OpenAiRequestDto();
        request.setModel(model);
        request.setTemperature(temperature);
        request.setMessages(messages);
        return request;
    }



    private OpenAiResponseDto callOpenAi(OpenAiRequestDto request) {
        return openAiRestClient
                .post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(OpenAiResponseDto.class);
    }

    //응답 dto에서 content 문자열을 꺼냄
    private String extractContent(OpenAiResponseDto response) {
        if (response == null
                || response.getChoices() == null
                || response.getChoices().length == 0
                || response.getChoices()[0].getMessage() == null
                || response.getChoices()[0].getMessage().getContent() == null) {
            throw new RuntimeException("OpenAI 응답이 비어있습니다.");
        }

        return response.getChoices()[0].getMessage().getContent();
    }

    //json 문자열을 java 객체로
    private IntentResultDto parseIntent(String json) {
        try {
            return objectMapper.readValue(json, IntentResultDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Intent JSON 파싱 실패: " + json, e);
        }
    }

    //답변을 순수 json 문자열로
    private String cleanJson(String raw) {
        if (raw == null) {
            throw new RuntimeException("LLM 응답이 비어있습니다.");
        }

        String cleaned = raw
                .replace("```json", "")
                .replace("```", "")
                .trim();

        int start = cleaned.indexOf("{");
        int end = cleaned.lastIndexOf("}");

        if (start == -1 || end == -1) {
            throw new RuntimeException("LLM 응답에서 JSON 블록을 찾을 수 없습니다: " + cleaned);
        }

        return cleaned.substring(start, end + 1);
    }
}
