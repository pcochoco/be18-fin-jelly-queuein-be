package com.beyond.qiin.domain.rag.dto;

public record RequirementRow(
        String businessGroup,
        String role,
        String requirementId,
        String requirementName,
        String functionalRequirement,
        String finalRequirement,
        String screenRequirement,
        String technicalRequirement) {

    /**
     * 최종 요구사항이 작성되어 있으면 이를 사용하고,
     * 없으면 기존 기능 요구사항을 사용한다.
     */
    public String resolvedRequirement() {
        if (finalRequirement != null && !finalRequirement.isBlank()) {
            return finalRequirement.trim();
        }

        return functionalRequirement == null ? "" : functionalRequirement.trim();
    }
}
