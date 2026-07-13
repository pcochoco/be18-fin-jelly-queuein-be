package com.beyond.qiin.domain.inventory.service.query;

import com.beyond.qiin.common.dto.PageResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.request.search_condition.ReservableAssetSearchCondition;
import com.beyond.qiin.domain.booking.dto.reservation.response.reservable_asset.ReservableAssetResponseDto;
import com.beyond.qiin.domain.inventory.dto.asset.request.search_condition.AssetSearchCondition;
import com.beyond.qiin.domain.inventory.dto.asset.response.*;
import com.beyond.qiin.domain.inventory.entity.Asset;
import com.beyond.qiin.domain.inventory.enums.AssetStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface AssetQueryService {

    // 0계층 드롭다운
    List<RootAssetResponseDto> getRootAssetIds();

    // 1계층 드롭다운
    List<OneDepthAssetResponseDto> getOneDepthAssetList(final Long rootAssetId);

    // 자식 자원들 조회
    PageResponseDto<DescendantAssetResponseDto> getDescendantAssetList(
            final AssetSearchCondition condition, final Pageable pageable);

    //    부분 트리 방식임
    TreeAssetResponseDto getAssetTree(final Long assetId);
    //        List<TreeAssetResponseDto> getFullAssetTree();

    // 전체 트리 조회
    List<TreeAssetResponseDto> getFullAssetTree();

    // 자원 상세 조회
    AssetDetailResponseDto getAssetDetail(final Long assetId);

    //
    Asset getAssetById(final Long assetId);

    // 자원 상태에 따른 사용 가능 여부 반환
    boolean isAvailable(final Long assetId);

    // 챗봇 용으로 추가하는 메소드들

    // 카테고리에 속한 자원 목록 조회
    List<Asset> findAssetsByCategory(final Long categoryId);

    // 사용 가능한 자원 목록 조회
    List<Asset> findAvailableAssets(final Long categoryId, final String keyword);

    // 예약 가능 시간대 있는자원 목록 조회
    PageResponseDto<ReservableAssetResponseDto> getReservableAssets(
            final Long userId, final ReservableAssetSearchCondition condition, final Pageable pageable);

    // 이름으로 자원 id 찾기
    Long findIdByName(String name);

    // 부모 경로 찾기
    List<String> findParentPath(Long assetId);

    // 자원 상태 조회
    AssetStatus findStatus(Long assetId);
}
