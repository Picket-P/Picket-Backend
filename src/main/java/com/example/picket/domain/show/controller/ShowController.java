package com.example.picket.domain.show.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.annotation.AuthPermission;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.show.dto.request.ShowCreateRequest;
import com.example.picket.domain.show.dto.request.ShowUpdateRequest;
import com.example.picket.domain.show.dto.response.ShowDateDetailResponse;
import com.example.picket.domain.show.dto.response.ShowDetailResponse;
import com.example.picket.domain.show.dto.response.ShowResponse;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.service.ShowCommandService;
import com.example.picket.domain.show.service.ShowDateQueryService;
import com.example.picket.domain.show.service.ShowQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ShowController {

    private final ShowCommandService showCommandService;
    private final ShowQueryService showQueryService;
    private final ShowDateQueryService showDateQueryService;

    // 공연 생성
    @PostMapping("/admin/shows")
    @AuthPermission(role = UserRole.ADMIN)
    public ResponseEntity<ShowDetailResponse> createShow(
        @Auth AuthUser user,
        @Valid @RequestBody ShowCreateRequest request
    ) {
        Show show = showCommandService.createShow(user, request);
        List<ShowDateDetailResponse> showDateDetail = showDateQueryService.getShowDateDetailResponsesByShowId(show.getId());
        return ResponseEntity.ok(ShowDetailResponse.toDto(show, showDateDetail));
    }

    // 공연 목록 조회 API (카테고리, 정렬 지원)
    @GetMapping("/shows")
    public ResponseEntity<List<ShowResponse>> getShows(
        @RequestParam(required = false) Category category,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String order
    ) {
        List<ShowResponse> response = showQueryService.getShowsQueryDsl(category, sortBy, order);
        return ResponseEntity.ok(response);
    }

    // 공연 단건 조회
    @GetMapping("/shows/{showId}")
    public ResponseEntity<ShowDetailResponse> getShowDetail(@PathVariable Long showId) {
        ShowDetailResponse response = showQueryService.getShowQueryDsl(showId);
        return ResponseEntity.ok(response);
}

    // 공연 수정 API (부분 수정 지원)
    @PutMapping("/shows/{showId}")
    public ResponseEntity<ShowDetailResponse> updateShow(
        @Auth AuthUser authUser,
        @PathVariable Long showId,
        @Valid @RequestBody ShowUpdateRequest request
    ) {
        Show show = showCommandService.updateShow(authUser, showId, request);
        List<ShowDateDetailResponse> showDateDetail = showDateQueryService.getShowDateDetailResponsesByShowId(show.getId());
        return ResponseEntity.ok(ShowDetailResponse.toDto(show, showDateDetail));
    }

    // 공연 삭제 API (소프트 삭제 방식)
    @DeleteMapping("/shows/{showId}")
    public ResponseEntity<Void> deleteShow(
        @Auth AuthUser authUser,
        @PathVariable Long showId
    ) {
        showCommandService.deleteShow(authUser, showId);
        return ResponseEntity.noContent().build();
    }

}