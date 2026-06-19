package com.beyond.qiin.domain.rag.entity;

import com.beyond.qiin.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "document_chunk")
public class DocumentChunk extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private RagDocument document;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    /**
     * REV-005 같은 원본 요구사항 ID
     */
    @Column(name = "source_key", nullable = false, length = 100)
    private String sourceKey;

    /**
     * 행의 각 필드명을 포함해 구성한 최종 청크 텍스트
     */
    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    /**
     * pgvector가 파싱할 수 있는 문자열 형태의 벡터. 예: [0.1, 0.2, ...]
     */
    @ColumnTransformer(write = "?::vector")
    @Column(name = "embedding", nullable = false, columnDefinition = "vector(3072)")
    private String embedding;

    public DocumentChunk(RagDocument document, Integer chunkIndex, String sourceKey, String content, String embedding) {
        this.document = document;
        this.chunkIndex = chunkIndex;
        this.sourceKey = sourceKey;
        this.content = content;
        this.embedding = embedding;
    }
}
