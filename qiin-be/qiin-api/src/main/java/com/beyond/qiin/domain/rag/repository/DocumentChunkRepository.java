package com.beyond.qiin.domain.rag.repository;

import com.beyond.qiin.domain.rag.entity.DocumentChunk;
import com.beyond.qiin.domain.rag.entity.RagDocument;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    @Query(
            value =
                    """
        SELECT *
        FROM document_chunk
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """,
            nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(@Param("embedding") String embedding, @Param("limit") int limit);

    List<DocumentChunk> findAllByDocumentIdOrderByChunkIndexAsc(Long documentId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM DocumentChunk chunk WHERE chunk.document IN :documents")
    int deleteAllByDocumentInBatch(@Param("documents") Collection<RagDocument> documents);
}
