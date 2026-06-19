package com.beyond.qiin.domain.rag.dto;

import java.util.List;

public record ParsedDocument(String title, String sourceUrl, List<RequirementRow> rows) {

    public ParsedDocument {
        rows = rows == null ? List.of() : List.copyOf(rows);
    }
}
