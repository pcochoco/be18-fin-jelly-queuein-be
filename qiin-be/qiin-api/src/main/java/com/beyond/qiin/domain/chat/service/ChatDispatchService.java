package com.beyond.qiin.domain.chat.service;

import com.beyond.qiin.domain.chat.dto.IntentResultDto;
import com.beyond.qiin.domain.inventory.service.query.AssetQueryService;
import com.beyond.qiin.domain.inventory.service.query.CategoryQueryService;
import com.beyond.qiin.domain.rag.service.RagRetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// intent 에 대해 db 호출 + llm summary 활용 응답 생성
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatDispatchService {

    private final AssetQueryService assetQueryService;
    private final CategoryQueryService categoryQueryService;
    private final LlmSummaryService llmSummaryService;
    private final RagRetrievalService ragRetrievalService;

    // 분류된 intent를 보고 알맞은 handler로 보내 실행 - 사용자 메시지를 ai에 전달해 intent로 요약했음
    public String dispatch(IntentResultDto intentResult) {
        return switch (intentResult.intent()) { // record이므로 getIntent()

                // db 조회 시
            case "LIST_ASSETS_BY_CATEGORY" -> handleListAssetsByCategory(intentResult);
            case "GET_ASSET_DETAIL" -> handleGetAssetDetail(intentResult);
            case "LIST_AVAILABLE_ASSETS" -> handleListAvailableAssets(intentResult);
            case "GET_ASSET_LOCATION" -> handleGetAssetLocation(intentResult);
            case "GET_ASSET_STATUS" -> handleGetAssetStatus(intentResult);
            case "LIST_ALL_CATEGORIES" -> handleListAllCategories();

                // TODO : rag 활용 시 chat dispatch service에서 handle method 추가 or not
            case "RAG_SEARCH" -> ragRetrievalService.answer(
                    (String) intentResult.params().get("query"));

            case "UNKNOWN" -> handleUnknown();
            default -> handleUnknown();
        };
    }

    private String handleListAssetsByCategory(IntentResultDto intent) {
        Long categoryId = extractLong(intent.params().get("categoryId"));

        var assets = assetQueryService.findAssetsByCategory(categoryId);

        return llmSummaryService.summarize(
                "다음은 카테고리 %d의 자원 목록입니다. 사용자가 이해하기 쉽게 자연스럽게 요약해주세요.".formatted(categoryId), assets);
    }

    private String handleGetAssetDetail(IntentResultDto intent) {
        Long assetId = extractLong(intent.params().get("assetId"));

        var asset = assetQueryService.getAssetDetail(assetId);

        return llmSummaryService.summarize("아래 자원의 상세 정보를 사용자에게 자연스럽게 설명해주세요.", asset);
    }

    private String handleListAvailableAssets(IntentResultDto intent) {
        Long categoryId = extractLong(intent.params().get("categoryId"));
        String keyword = (String) intent.params().get("keyword");

        var assets = assetQueryService.findAvailableAssets(categoryId, keyword);

        return llmSummaryService.summarize("아래는 예약 가능한 자원 목록입니다. 사용자에게 자연스럽게 설명해주세요.", assets);
    }

    private String handleGetAssetLocation(IntentResultDto intent) {
        Long assetId = extractLong(intent.params().get("assetId"));
        String assetName = (String) intent.params().get("assetName");

        Long id = (assetId != null) ? assetId : assetQueryService.findIdByName(assetName);

        var path = assetQueryService.findParentPath(id);

        return llmSummaryService.summarize("이 자원의 전체 위치 경로를 자연스럽게 설명해주세요.", path);
    }

    private String handleGetAssetStatus(IntentResultDto intent) {
        Long assetId = extractLong(intent.params().get("assetId"));

        var status = assetQueryService.findStatus(assetId);

        return llmSummaryService.summarize("이 자원의 상태를 사용자에게 이해하기 쉽게 설명해주세요.", status);
    }

    private String handleListAllCategories() {
        var categories = categoryQueryService.findAllCategories();

        return llmSummaryService.summarize("아래는 전체 카테고리 목록입니다. 사용자에게 자연스럽게 요약해주세요.", categories);
    }

    private String handleUnknown() {
        return "죄송합니다, 이해하지 못했어요. 다시 질문해 주세요!";
    }

    private Long extractLong(Object value) {
        return value == null ? null : Long.valueOf(value.toString());
    }
}
