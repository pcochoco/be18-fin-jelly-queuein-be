package com.beyond.qiin.domain.chat.service;

import com.beyond.qiin.domain.chat.dto.IntentResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final IntentService intentService;
    private final ChatDispatchService chatDispatchService;

    public String handleUserMessage(String userMessage) {
        // intent 분석
        IntentResultDto intentResult = intentService.analyze(userMessage);

        // intent 실제 실행
        return chatDispatchService.dispatch(intentResult);
    }
}
