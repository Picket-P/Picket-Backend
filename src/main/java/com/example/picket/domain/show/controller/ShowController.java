package com.example.picket.domain.show.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.annotation.AuthPermission;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.dto.PageResponse;
import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.ranking.scheduler.PopularKeywordScheduler;
import com.example.picket.domain.show.dto.request.ShowCreateRequest;
import com.example.picket.domain.show.dto.request.ShowUpdateRequest;
import com.example.picket.domain.show.dto.response.ShowDateDetailResponse;
import com.example.picket.domain.show.dto.response.ShowDetailResponse;
import com.example.picket.domain.show.dto.response.ShowImageResponse;
import com.example.picket.domain.show.dto.response.ShowResponse;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.service.ShowCommandService;
import com.example.picket.domain.show.service.ShowDateQueryService;
import com.example.picket.domain.show.service.ShowQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
@Tag(name = "공연 관리 API", description = "공연 생성, 다건 조회, 단건 조회, 업데이트, 삭제 기능 API입니다.")
public class ShowController {

    private final ShowCommandService showCommandService;
    private final ShowQueryService showQueryService;
    private final ShowDateQueryService showDateQueryService;
    private final PopularKeywordScheduler popularKeywordScheduler;

    // 공연 생성
    @AuthPermission(role = UserRole.DIRECTOR)
    @Operation(summary = "공연 생성", description = "공연을 생성할 수 있습니다.")
    @PostMapping("/admin/shows")
    public ResponseEntity<ShowDetailResponse> createShow(
            @Auth AuthUser user,
            @Valid @RequestBody ShowCreateRequest request
    ) {
        Show show = showCommandService.createShow(user, request);
        List<ShowDateDetailResponse> showDateDetail = showDateQueryService.getShowDateDetailResponsesByShowId(
                show.getId());
        return ResponseEntity.ok(ShowDetailResponse.of(show, showDateDetail));
    }

    // 공연 목록 조회 API (카테고리, 정렬 지원)
    @Operation(summary = "공연 다건 조회", description = "공연을 다건으로 조회할 수 있습니다.")
    @GetMapping("/shows")
    public ResponseEntity<PageResponse<ShowResponse>> getShows(
            @RequestParam(required = false) Category category,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        // 인기 검색어 카운트 증가
        popularKeywordScheduler.incrementSearchKeyword(category);

        Page<ShowResponse> response = showQueryService.getShows(category, sortBy, order, page, size);
        return ResponseEntity.ok(PageResponse.of(response));
    }

    // 공연 단건 조회
    @Operation(summary = "공연 단건 조회", description = "공연을 단건으로 조회할 수 있습니다.")
    @GetMapping("/shows/{showId}")
    public ResponseEntity<ShowDetailResponse> getShowDetail(
            @Auth(isRequire = false) AuthUser authUser,
            @PathVariable Long showId
    ) {
        ShowDetailResponse response = showQueryService.getShow(authUser, showId);
        return ResponseEntity.ok(response);
    }

    // 공연 수정 API (부분 수정 지원)
    @AuthPermission(role = UserRole.DIRECTOR)
    @Operation(summary = "공연 업데이트", description = "공연을 업데이트 할 수 있습니다.")
    @PutMapping("/shows/{showId}")
    public ResponseEntity<ShowDetailResponse> updateShow(
            @Auth AuthUser authUser,
            @PathVariable Long showId,
            @Valid @RequestBody ShowUpdateRequest request
    ) {
        Show show = showCommandService.updateShow(authUser, showId, request);
        List<ShowDateDetailResponse> showDateDetail = showDateQueryService.getShowDateDetailResponsesByShowId(
                show.getId());
        return ResponseEntity.ok(ShowDetailResponse.of(show, showDateDetail));
    }

    // 공연 삭제 API (소프트 삭제 방식)
    @AuthPermission(role = UserRole.DIRECTOR)
    @Operation(summary = "공연 삭제", description = "공연을 삭제할 수 있습니다.")
    @DeleteMapping("/shows/{showId}")
    public ResponseEntity<Void> deleteShow(
            @Auth AuthUser authUser,
            @PathVariable Long showId
    ) {
        showCommandService.deleteShow(authUser, showId);
        return ResponseEntity.noContent().build();
    }

    @AuthPermission(role = UserRole.DIRECTOR)
    @Operation(summary = "공연 포스터 이미지 업로드", description = "공연 포스터 이미지를 AWS S3에 업로드합니다. 이 API는 DIRECTOR 역할의 사용자만 호출할 수 있습니다.")
    @PostMapping(value = "/shows/uploadImage", consumes = "multipart/form-data")
    public ResponseEntity<ShowImageResponse> uploadImage(@Parameter(
            description = "업로드할 이미지 파일",
            required = true,
            name = "multipartFile",
            content = @Content(
                    mediaType = "multipart/form-data",
                    schema = @Schema(
                            type = "string",
                            format = "binary",
                            description = "지원 형식: JPEG, PNG, GIF, WEBP. 최대 크기: 8MB. 예: image.png",
                            example = "image.png"
                    )
            )
    ) @RequestParam("multipartFile") MultipartFile multipartFile) {
        return ResponseEntity.ok(ShowImageResponse.of(showCommandService.uploadImage(multipartFile)));
    }
}