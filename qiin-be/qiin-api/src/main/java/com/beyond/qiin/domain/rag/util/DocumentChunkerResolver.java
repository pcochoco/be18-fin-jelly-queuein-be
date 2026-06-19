package com.beyond.qiin.domain.rag.util;

import com.beyond.qiin.domain.rag.enums.ChunkingStrategy;
import com.beyond.qiin.domain.rag.exception.RagErrorCode;
import com.beyond.qiin.domain.rag.exception.RagException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

//TODO : 생략 가능한지
@Component
@RequiredArgsConstructor
public class DocumentChunkerResolver { 

    private final List<DocumentChunker> chunkers;

    public DocumentChunker resolve(ChunkingStrategy strategy) {
        return chunkers.stream()
                .filter(chunker -> chunker.supports() == strategy)
                .findFirst()
                .orElseThrow(() -> new RagException(
                        RagErrorCode.RAG_CHUNKING_STRATEGY_NOT_SUPPORTED, "지원하지 않는 청킹 전략입니다: " + strategy));
    }
}
