package com.beyond.qiin.domain.rag.util;

import com.beyond.qiin.domain.rag.dto.ChunkData;
import com.beyond.qiin.domain.rag.dto.ParsedDocument;
import com.beyond.qiin.domain.rag.enums.ChunkingStrategy;
import java.util.List;

public interface DocumentChunker {

    ChunkingStrategy supports();

    List<ChunkData> chunk(ParsedDocument document);
}
