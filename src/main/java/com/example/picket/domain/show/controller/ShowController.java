package com.example.picket.domain.show.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.annotation.AuthPermission;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.show.dto.ShowCreateRequest;
import com.example.picket.domain.show.dto.ShowDateResponse;
import com.example.picket.domain.show.dto.ShowResponse;
import com.example.picket.domain.show.dto.ShowUpdateRequest;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import com.example.picket.domain.show.service.ShowCommandService;
import com.example.picket.domain.show.service.ShowQueryService;
import com.example.picket.domain.show.service.ShowResponseMapper;
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
    private final ShowResponseMapper showResponseMapper;
    private final ShowDateRepository showDateRepository;

    // 공연 생성
    @PostMapping("/admin/shows")
    public ResponseEntity<ShowResponse> createShow(@Auth AuthUser user, @RequestBody ShowCreateRequest request) {
        Show show = showCommandService.createShow(user, request);
        List<ShowDate> dates = showDateRepository.findAllByShowId(show.getId());
        return ResponseEntity.ok(ShowResponse.from(show, dates.stream().map(ShowDateResponse::from).toList()));
    }

    // 공연 목록 조회 API (카테고리, 정렬 지원)
    @GetMapping("/shows")
    public ResponseEntity<List<ShowResponse>> getShows(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order
    ) {
        List<Show> shows = showQueryService.getShows(category, sortBy, order);

        List<ShowResponse> response = shows.stream()
                .map(show -> {
                    List<ShowDate> showDates = showQueryService.getShowDatesByShowId(show.getId());
                    return showResponseMapper.toDto(show, showDates);
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    // 공연 단건 조회
    @GetMapping("/shows/{showId}")
    public ResponseEntity<ShowResponse> getShowDetail(@PathVariable Long showId) {
        Show show = showQueryService.getShowDetails(showId);
        List<ShowDate> showDates = showQueryService.getShowDatesByShowId(showId);
        ShowResponse response = showResponseMapper.toDto(show, showDates);
        return ResponseEntity.ok(response);
    }

    // 공연 수정 API (부분 수정 지원)
    @PatchMapping("/shows/{showId}")
    public ResponseEntity<ShowResponse> updateShow(
            @Auth AuthUser authUser,
            @PathVariable Long showId,
            @RequestBody ShowUpdateRequest request
    ) {
        Show show = showCommandService.updateShow(authUser, showId, request);
        List<ShowDate> showDates = showDateRepository.findAllByShowId(showId);

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