package com.example.picket.domain.like.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.like.dto.response.LikeResponse;
import com.example.picket.domain.like.service.LikeCommandService;
import com.example.picket.domain.like.service.LikeQueryService;
import com.example.picket.domain.show.entity.Show;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "좋아요 관리 API", description = "사용자가 좋아요한 공연 목록조회, 좋아요 추가, 취소 기능 API입니다.")
public class LikeController {

    private final LikeQueryService likeQueryService;
    private final LikeCommandService likeCommandService;

    @Operation(summary = "좋아요한 공연 목록 조회", description = "사용자가 좋아요한 공연 목록조회를 할 수 있습니다.")
    @GetMapping("/likes")
    public ResponseEntity<Page<LikeResponse>> getLikes(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @Auth AuthUser authUser) {
        List<Show> shows = likeQueryService.getLikes(authUser.getId(), page, size);
        Pageable pageable = PageRequest.of(page, size);
        List<LikeResponse> likeResponses = shows.stream().map(
                show -> LikeResponse.toDto(
                        show.getId(),
                        show.getTitle(),
                        show.getCategory(),
                        show.getDescription()
                )
        ).toList();
        Page<LikeResponse> responses = new PageImpl<>(likeResponses, pageable, likeResponses.size());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "좋아요 추가", description = "특정 공연에 좋아요를 추가할 수 있습니다.")
    @PostMapping("/shows/{showId}/likes")
    public ResponseEntity<Void> createLike(@Auth AuthUser authUser, @PathVariable Long showId) {
        likeCommandService.createLike(authUser.getId(), showId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "좋아요 삭제", description = "특정 공연에 좋아요를 삭제할 수 있습니다.")
    @DeleteMapping("/shows/{showId}/likes/{likeId}")
    public ResponseEntity<Void> createLike(@Auth AuthUser authUser, @PathVariable Long showId,
                                           @PathVariable Long likeId) {
        likeCommandService.deleteLike(authUser.getId(), showId, likeId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
