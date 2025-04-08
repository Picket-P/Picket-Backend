package com.example.picket.domain.show.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.annotation.AuthPermission;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.show.dto.request.ShowCreateRequest;
import com.example.picket.domain.show.dto.request.ShowUpdateRequest;
import com.example.picket.domain.show.dto.response.ShowDateResponse;
import com.example.picket.domain.show.dto.response.ShowResponse;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.service.ShowCommandService;
import com.example.picket.domain.show.service.ShowDateQueryService;
import com.example.picket.domain.show.service.ShowQueryService;
import com.example.picket.domain.show.service.ShowResponseMapper;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ShowController {

    private final ShowCommandService showCommandService;
    private final ShowQueryService showQueryService;
    private final ShowDateQueryService showDateQueryService;

    private final ShowResponseMapper showResponseMapper;

    // 공연 생성
    @PostMapping("/admin/shows")
    @AuthPermission(role = UserRole.ADMIN)
    public ResponseEntity<ShowResponse> createShow(@Auth AuthUser user, @Valid @RequestBody ShowCreateRequest request) {
        Show show = showCommandService.createShow(user, request);
        List<ShowDate> dates = showDateQueryService.getShowDatesByShowId(show.getId());
        return ResponseEntity.ok(ShowResponse.toDto(show, dates.stream().map(ShowDateResponse::toDto).toList()));
    }

    // 공연 목록 조회 API (카테고리, 정렬 지원)
    @GetMapping("/shows")
    public ResponseEntity<List<ShowResponse>> getShows(
            @RequestParam(required = false) Category category,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order
    ) {
        List<Show> shows = showQueryService.getShows(category, sortBy, order);

        List<ShowResponse> response = shows.stream()
                .map(show -> {
                    List<ShowDate> showDates = showDateQueryService.getShowDatesByShowId(show.getId());
                    return showResponseMapper.toDto(show, showDates);
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    // 공연 단건 조회
    @GetMapping("/shows/{showId}")
    public ResponseEntity<ShowResponse> getShowDetail(@PathVariable Long showId) {
        Show show = showQueryService.getShow(showId);
        List<ShowDate> showDates = showDateQueryService.getShowDatesByShowId(showId);
        ShowResponse response = showResponseMapper.toDto(show, showDates);
        return ResponseEntity.ok(response);
    }

    // 공연 수정 API (부분 수정 지원)
    @PatchMapping("/shows/{showId}")
    public ResponseEntity<ShowResponse> updateShow(
            @Auth AuthUser authUser,
            @PathVariable Long showId,
            @Valid @RequestBody ShowUpdateRequest request
    ) {
        Show show = showCommandService.updateShow(authUser, showId, request);
        List<ShowDate> showDates = showDateQueryService.getShowDatesByShowId(showId);

        ShowResponse response = showResponseMapper.toDto(show, showDates);
        return ResponseEntity.ok(response);
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