package com.beyond.qiin.domain.rag.entity;

import com.beyond.qiin.common.BaseEntity;
import com.beyond.qiin.domain.rag.enums.ChunkingStrategy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "rag_document")
public class RagDocument extends BaseEntity {

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "source_url", nullable = false, length = 1000)
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "chunking_strategy", nullable = false, length = 30)
    private ChunkingStrategy chunkingStrategy;

    public RagDocument(String title, String sourceUrl, ChunkingStrategy chunkingStrategy) {
        this.title = title;
        this.sourceUrl = sourceUrl;
        this.chunkingStrategy = chunkingStrategy;
    }
}
