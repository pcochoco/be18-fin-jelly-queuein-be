package com.beyond.qiin.domain.chat.service;

import com.beyond.qiin.domain.chat.dto.IntentResultDto;
import com.beyond.qiin.infra.ai.service.ChatClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// 사용자가 보낸 메시지를 chatbot client로 요약해 intent를 추출
@Service
@RequiredArgsConstructor
public class IntentService {
    private final ChatClient chatClient;

    public IntentResultDto analyze(String userMessage) {
        return chatClient.extractIntent(userMessage);
    }
}
