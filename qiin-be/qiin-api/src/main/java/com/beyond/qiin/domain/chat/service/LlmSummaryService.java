package com.beyond.qiin.domain.chat.service;

import com.beyond.qiin.infra.ai.service.ChatClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// db 조회 결과를 자연어로 처리
@Service
@RequiredArgsConstructor
public class LlmSummaryService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public String summarize(String instruction, Object data) {
        return chatClient.sendMessage(
                """
                %s

                데이터:
                %s
                """
                        .formatted(instruction, toJson(data)));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
