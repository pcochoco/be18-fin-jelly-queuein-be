package com.beyond.qiin.domain.rag.util;

import com.beyond.qiin.domain.rag.dto.ChunkData;
import com.beyond.qiin.domain.rag.dto.ParsedDocument;
import com.beyond.qiin.domain.rag.dto.RequirementRow;
import com.beyond.qiin.domain.rag.enums.ChunkingStrategy;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
//TODO : 생략 가능한지
@Component
public class RequirementRowChunker implements DocumentChunker { //TODO : Impl

    @Override
    public ChunkingStrategy supports() {
        return ChunkingStrategy.REQUIREMENT_ROW;
    }

    @Override
    public List<ChunkData> chunk(ParsedDocument document) {
        List<ChunkData> chunks = new ArrayList<>();

        for (RequirementRow row : document.rows()) {
            if (isEmptyRow(row)) {
                continue;
            }

            String content = createContent(document.title(), row);

            chunks.add(new ChunkData(chunks.size(), row.requirementId(), content));
        }

        return chunks;
    }

    private String createContent(String documentTitle, RequirementRow row) {
        StringBuilder builder = new StringBuilder();

        append(builder, "문서", documentTitle);
        append(builder, "업무 그룹", row.businessGroup());
        append(builder, "대상 권한", row.role());
        append(builder, "요구사항 ID", row.requirementId());
        append(builder, "요구사항명", row.requirementName());

        // 기존 요구사항과 최종 요구사항을 모두 넣지 않고
        // 현재 적용되는 최종 내용만 저장한다.
        append(builder, "최종 기능 요구사항", row.resolvedRequirement());

        append(builder, "화면 요구사항", row.screenRequirement());
        append(builder, "기술 요구사항", row.technicalRequirement());

        return builder.toString().trim();
    }

    private void append(StringBuilder builder, String fieldName, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        builder.append(fieldName).append(": ").append(value.trim()).append(System.lineSeparator());
    }

    private boolean isEmptyRow(RequirementRow row) {
        return isBlank(row.requirementId()) && isBlank(row.requirementName()) && isBlank(row.resolvedRequirement());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
