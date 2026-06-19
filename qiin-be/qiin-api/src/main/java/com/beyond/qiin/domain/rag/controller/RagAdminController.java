package com.beyond.qiin.domain.rag.controller;

import com.beyond.qiin.domain.rag.service.RequirementExcelRagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/rag")
public class RagAdminController {

    private final RequirementExcelRagService requirementExcelRagService;

    @PostMapping("/requirements/ingest")
    public ResponseEntity<Long> ingestRequirements() {
        Long documentId = requirementExcelRagService.ingestRequirements();

        return ResponseEntity.ok(documentId);
    }
}
