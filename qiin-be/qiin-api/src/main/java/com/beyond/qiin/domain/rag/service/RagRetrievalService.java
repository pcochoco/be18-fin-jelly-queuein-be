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
//embedding model을 이용해 질문에 대한 답변을 생성하고, 관련 문서 청크를 검색하는 서비스
//chat dispatch service 의 RAG_SEARCH case 에서 답변 생성
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, transactionManager = "ragTransactionManager")
public class RagRetrievalService {

    private final EmbeddingClient embeddingClient;
    private final ChatClient chatClient;
    private final DocumentChunkRepository documentChunkRepository;

    public String answer(String userMessage) {
        String context = searchContext(userMessage);

        // LLM에 질문과 context를 넘겨 답변 생성
        return chatClient.answerWithRag(userMessage, context);
    }

    public String searchContext(String userMessage) {

        // 질문에 대한 임베딩 생성
        String embedding = embeddingClient.embed(userMessage);

        // pgvector 기반으로 질문에 대한 유사도 검색
        List<DocumentChunk> chunks = documentChunkRepository.findSimilarChunks(embedding, 5);

        // LLM에 넘길 context 문자열 생성
        return chunks.stream().map(DocumentChunk::getContent).collect(Collectors.joining("\n\n"));
    }
}
