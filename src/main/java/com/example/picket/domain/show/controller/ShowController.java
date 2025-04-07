package com.example.picket.domain.show.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.annotation.AuthPermission;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.show.dto.ShowCreateRequest;
import com.example.picket.domain.show.dto.ShowResponse;
import com.example.picket.domain.show.dto.ShowUpdateRequest;
import com.example.picket.domain.show.service.ShowCommandService;
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

    // 공연 생성
    @PostMapping("/admin/shows")
    @AuthPermission(role = UserRole.ADMIN)
    public ResponseEntity<ShowResponse> createShow(
            @Auth AuthUser authUser,
            @RequestBody @Valid ShowCreateRequest request
    ) {
        ShowResponse response = showCommandService.createShow(authUser, request);
        return ResponseEntity.ok(response);
    }

    // 공연 단건 조회
    @GetMapping("/shows/{showId}")
    public ResponseEntity<ShowResponse> getShowDetails(@PathVariable Long showId) {
        return ResponseEntity.ok(showQueryService.getShowDetails(showId));
    }

    // 공연 목록 조회
    @GetMapping("/shows")
    public List<ShowResponse> getShows(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order
    ) {
        return showQueryService.getShows(category, sortBy, order);
    }

    // 공연 수정
    @PatchMapping("/shows/{showId}")
    public ResponseEntity<ShowResponse> updateShow(
            @Auth AuthUser authUser,
            @PathVariable Long showId,
            @RequestBody ShowUpdateRequest request
    ) {
        ShowResponse updated = showCommandService.updateShow(authUser, showId, request);
        return ResponseEntity.ok(updated);
    }

    // 공연 삭제
    @DeleteMapping("/shows/{showId}")
    public ResponseEntity<Void> deleteShow(
            @Auth AuthUser authUser,
            @PathVariable Long showId
    ) {
        showCommandService.deleteShow(authUser, showId);
        return ResponseEntity.noContent().build();
    }
}