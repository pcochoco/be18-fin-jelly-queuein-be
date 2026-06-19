package com.beyond.qiin.domain.rag.service;

import com.beyond.qiin.domain.rag.dto.ChunkData;
import com.beyond.qiin.domain.rag.dto.ParsedDocument;
import com.beyond.qiin.domain.rag.entity.DocumentChunk;
import com.beyond.qiin.domain.rag.entity.RagDocument;
import com.beyond.qiin.domain.rag.enums.ChunkingStrategy;
import com.beyond.qiin.domain.rag.exception.RagErrorCode;
import com.beyond.qiin.domain.rag.exception.RagException;
import com.beyond.qiin.domain.rag.repository.DocumentChunkRepository;
import com.beyond.qiin.domain.rag.repository.RagDocumentRepository;
import com.beyond.qiin.domain.rag.util.DocumentChunker;
import com.beyond.qiin.domain.rag.util.DocumentChunkerResolver;
import com.beyond.qiin.infra.ai.service.EmbeddingClient;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//embedding model을 이용해 문서를 벡터화하고, chunking 전략에 따라 문서를 분할하여 DB에 저장하는 서비스
@Service
@RequiredArgsConstructor
public class RagIngestionService {

    private final DocumentChunkerResolver chunkerResolver;
    private final EmbeddingClient embeddingClient;
    private final RagDocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;

    @Transactional(transactionManager = "ragTransactionManager")
    public Long ingest(ParsedDocument parsedDocument, ChunkingStrategy strategy) {

        DocumentChunker chunker = chunkerResolver.resolve(strategy);
        
        List<ChunkData> chunkDataList = chunker.chunk(parsedDocument);

        if (chunkDataList.isEmpty()) {
            throw new RagException(RagErrorCode.RAG_CHUNK_EMPTY);
        }

        replaceExistingDocument(parsedDocument.sourceUrl(), strategy);

        RagDocument document =
                documentRepository.save(new RagDocument(parsedDocument.title(), parsedDocument.sourceUrl(), strategy));

        List<DocumentChunk> chunks = new ArrayList<>();

        for (ChunkData chunkData : chunkDataList) {
            //embedding model 호출해서 벡터화
            String embedding = embeddingClient.embed(chunkData.content());

            //chunk entity 생성
            chunks.add(new DocumentChunk(
                    document, chunkData.chunkIndex(), chunkData.sourceKey(), chunkData.content(), embedding));
        }

        chunkRepository.saveAll(chunks);

        return document.getId();
    }

    //delete old version of document if exists
    private void replaceExistingDocument(String sourceUrl, ChunkingStrategy strategy) {
        List<RagDocument> existingDocuments =
                documentRepository.findAllBySourceUrlAndChunkingStrategy(sourceUrl, strategy);

        if (existingDocuments.isEmpty()) {
            return;
        }

        //delete all chunks of according documents
        chunkRepository.deleteAllByDocumentInBatch(existingDocuments);

        //delete documents 
        documentRepository.deleteAllInBatch(existingDocuments);
    }
}
