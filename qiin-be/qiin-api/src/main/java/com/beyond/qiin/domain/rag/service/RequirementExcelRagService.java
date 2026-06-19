package com.beyond.qiin.domain.rag.service;

import com.beyond.qiin.domain.rag.dto.ParsedDocument;
import com.beyond.qiin.domain.rag.enums.ChunkingStrategy;
import com.beyond.qiin.domain.rag.source.ExcelRequirementDocumentReader;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequirementExcelRagService {

    //TODO : 고정되어있는 상수임에 따라 여러 문서 지원 시 수정 필요 
    private static final String DOCUMENT_TITLE = "QueueIn 요구사항 정의서";
    private static final Path EXCEL_PATH = Path.of("docs", "QueueIn_요구사항정의서_테이블.xlsx");

    private final ExcelRequirementDocumentReader documentReader;
    private final RagIngestionService ingestionService;

    public Long ingestRequirements() {
        ParsedDocument document = documentReader.read(DOCUMENT_TITLE, EXCEL_PATH.toString(), EXCEL_PATH);

        return ingestionService.ingest(document, ChunkingStrategy.REQUIREMENT_ROW);
    }
}
