package com.beyond.qiin.domain.rag.service;

import com.beyond.qiin.domain.rag.entity.DocumentChunk;
import com.beyond.qiin.domain.rag.repository.DocumentChunkRepository;
import com.beyond.qiin.infra.ai.service.ChatClient;
import com.beyond.qiin.infra.ai.service.EmbeddingClient;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, transactionManager = "ragTransactionManager")
public class RagRetrievalService {

    private final EmbeddingClient embeddingClient;
    private final ChatClient chatClient;
    private final DocumentChunkRepository documentChunkRepository;

    public String answer(String userMessage) {
        String context = searchContext(userMessage);

        return chatClient.answerWithRag(userMessage, context);
    }

    public String searchContext(String userMessage) {

        // 1. 질문 임베딩 생성
        String embedding = embeddingClient.embed(userMessage);

        // 2. pgvector 유사도 검색
        List<DocumentChunk> chunks = documentChunkRepository.findSimilarChunks(embedding, 5);

        // 3. LLM에 넘길 context 문자열 생성
        return chunks.stream().map(DocumentChunk::getContent).collect(Collectors.joining("\n\n"));
    }
}
