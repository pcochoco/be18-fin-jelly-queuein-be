package com.beyond.qiin.domain.inventory.controller;

import com.beyond.qiin.common.dto.PageResponseDto;
import com.beyond.qiin.domain.booking.dto.reservation.request.search_condition.ReservableAssetSearchCondition;
import com.beyond.qiin.domain.booking.dto.reservation.response.reservable_asset.ReservableAssetResponseDto;
import com.beyond.qiin.domain.inventory.dto.asset.request.CreateAssetRequestDto;
import com.beyond.qiin.domain.inventory.dto.asset.request.MoveAssetRequestDto;
import com.beyond.qiin.domain.inventory.dto.asset.request.UpdateAssetRequestDto;
import com.beyond.qiin.domain.inventory.dto.asset.request.search_condition.AssetSearchCondition;
import com.beyond.qiin.domain.inventory.dto.asset.response.*;
import com.beyond.qiin.domain.inventory.service.command.AssetCommandService;
import com.beyond.qiin.domain.inventory.service.query.AssetQueryService;
import com.beyond.qiin.security.jwt.JwtTokenProvider;
import com.beyond.qiin.security.resolver.AccessToken;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetCommandService assetCommandService;

    private final AssetQueryService assetQueryService;

    private final JwtTokenProvider jwtTokenProvider;

    // 자원 생성
    @PreAuthorize("hasAnyAuthority('MASTER', 'ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<CreateAssetResponseDto> createAsset(
            @Valid @RequestBody CreateAssetRequestDto createAssetRequestDto) {

        CreateAssetResponseDto createAssetResponseDto = assetCommandService.createAsset(createAssetRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(createAssetResponseDto);
    }

    // 자원 수정
    @PreAuthorize("hasAnyAuthority('MASTER', 'ADMIN', 'MANAGER')")
    @PatchMapping("/{assetId}")
    public ResponseEntity<UpdateAssetResponseDto> updateAsset(
            @PathVariable Long assetId, @Valid @RequestBody UpdateAssetRequestDto updateAssetRequestDto) {

        UpdateAssetResponseDto updateAssetResponseDto = assetCommandService.updateAsset(updateAssetRequestDto, assetId);

        return ResponseEntity.status(HttpStatus.OK).body(updateAssetResponseDto);
    }

    // 자원 삭제
    @PreAuthorize("hasAnyAuthority('MASTER', 'ADMIN', 'MANAGER')")
    @DeleteMapping("/{assetId}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long assetId, @AccessToken final String accessToken) {

        final Long userId = jwtTokenProvider.getUserId(accessToken);

        assetCommandService.softDeleteAsset(assetId, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    // 자원 이동
    @PreAuthorize("hasAnyAuthority('MASTER', 'ADMIN', 'MANAGER')")
    @PatchMapping("/{assetId}/move")
    public ResponseEntity<Void> moveAsset(
            @PathVariable Long assetId, @Valid @RequestBody MoveAssetRequestDto moveAssetRequestDto) {

        assetCommandService.moveAsset(assetId, moveAssetRequestDto.getParentName());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    // 0계층 조회
    @PreAuthorize("hasAnyAuthority('MASTER', 'ADMIN', 'MANAGER', 'GENERAL')")
    @GetMapping("/roots")
    public ResponseEntity<List<RootAssetResponseDto>> getRootAssets() {

        List<RootAssetResponseDto> rootAssetList = assetQueryService.getRootAssetIds();

        return ResponseEntity.status(HttpStatus.OK).body(rootAssetList);
    }

    // 1계층 조회
    @PreAuthorize("hasAnyAuthority('MASTER', 'ADMIN', 'MANAGER', 'GENERAL')")
    @GetMapping("/{rootId}/one-depth")
    public ResponseEntity<List<OneDepthAssetResponseDto>> getOneDepthAssets(@PathVariable Long rootId) {

        List<OneDepthAssetResponseDto> oneDepthAssetList = assetQueryService.getOneDepthAssetList(rootId);

        return ResponseEntity.status(HttpStatus.OK).body(oneDepthAssetList);
    }

    // 예약 가능한 자원들 조회
    @PreAuthorize("hasAnyAuthority('MASTER', 'ADMIN', 'MANAGER', 'GENERAL')")
    @GetMapping("/descendants")
    public ResponseEntity<PageResponseDto<DescendantAssetResponseDto>> getDescendantAssets(
            @Valid @ModelAttribute AssetSearchCondition condition, Pageable pageable) {
        PageResponseDto<DescendantAssetResponseDto> descendantAssetList =
                assetQueryService.getDescendantAssetList(condition, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(descendantAssetList);
    }

    // 실제 예약 시간 점유되지 않은 자원 조회
    // 예약 가능 자원 목록 조회
    @PreAuthorize("hasAnyAuthority('MASTER', 'ADMIN','GENERAL', 'MANAGER')")
    @GetMapping("/reservable-assets")
    public ResponseEntity<PageResponseDto<ReservableAssetResponseDto>> getReservableAssets(
            @AccessToken final String accessToken,
            @Valid @ModelAttribute ReservableAssetSearchCondition condition,
            Pageable pageable) {

        final Long userId = jwtTokenProvider.getUserId(accessToken);

        PageResponseDto<ReservableAssetResponseDto> page =
                assetQueryService.getReservableAssets(userId, condition, pageable);
        return ResponseEntity.ok(page);
    }

    // 자원 계층 조회
    @PreAuthorize("hasAnyAuthority('MASTER', 'ADMIN', 'MANAGER', 'GENERAL')")
    @GetMapping("/tree")
    public ResponseEntity<List<TreeAssetResponseDto>> getTreeAssets() {

        List<TreeAssetResponseDto> tree = assetQueryService.getFullAssetTree();

        return ResponseEntity.status(HttpStatus.OK).body(tree);
    }

    // 자원 상세 조회
    @PreAuthorize("hasAnyAuthority('MASTER', 'ADMIN', 'MANAGER', 'GENERAL')")
    @GetMapping("/{assetId}")
    public ResponseEntity<AssetDetailResponseDto> getAssetDetail(@PathVariable Long assetId) {

        AssetDetailResponseDto assetDetail = assetQueryService.getAssetDetail(assetId);

        return ResponseEntity.status(HttpStatus.OK).body(assetDetail);
    }
}
