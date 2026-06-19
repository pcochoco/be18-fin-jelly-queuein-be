package com.beyond.qiin.domain.rag.repository;

import com.beyond.qiin.domain.rag.entity.RagDocument;
import com.beyond.qiin.domain.rag.enums.ChunkingStrategy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RagDocumentRepository extends JpaRepository<RagDocument, Long> {

    List<RagDocument> findAllBySourceUrlAndChunkingStrategy(String sourceUrl, ChunkingStrategy chunkingStrategy);
}
