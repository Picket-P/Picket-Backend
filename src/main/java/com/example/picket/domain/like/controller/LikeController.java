package com.example.picket.domain.like.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.like.dto.response.LikeResponse;
import com.example.picket.domain.like.service.LikeCommandService;
import com.example.picket.domain.like.service.LikeQueryService;
import com.example.picket.domain.show.entity.Show;
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
public class LikeController {

    private final LikeQueryService likeQueryService;
    private final LikeCommandService likeCommandService;

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

    @PostMapping("/shows/{showId}/likes")
    public ResponseEntity<Void> createLike(@Auth AuthUser authUser, @PathVariable Long showId) {
        likeCommandService.createLike(authUser.getId(), showId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/shows/{showId}/likes/{likeId}")
    public ResponseEntity<Void> createLike(@Auth AuthUser authUser, @PathVariable Long showId,
                                           @PathVariable Long likeId) {
        likeCommandService.deleteLike(authUser.getId(), showId, likeId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
