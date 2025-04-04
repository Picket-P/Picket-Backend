package com.example.picket.domain.comment.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.comment.dto.request.CommentRequest;
import com.example.picket.domain.comment.dto.response.CommentResponse;
import com.example.picket.domain.comment.dto.response.PageCommentResponse;
import com.example.picket.domain.comment.service.CommentCommandService;
import com.example.picket.domain.comment.service.CommentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class CommentController {

    private final CommentCommandService commentCommandService;
    private final CommentQueryService  commentQueryService;

    @PostMapping("/shows/{showId}/comments")
    public ResponseEntity<CommentResponse> createComment(@Auth AuthUser auth,
                                                         @PathVariable Long showId,
                                                         @RequestBody CommentRequest commentRequest) {
        return ResponseEntity.ok(commentCommandService.createComment(auth.getId(), showId, commentRequest));
    }

    @PatchMapping("/shows/{showId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(@Auth AuthUser auth,
                                                         @PathVariable Long showId,
                                                         @PathVariable Long commentId,
                                                         @RequestBody CommentRequest commentRequest) {
        return ResponseEntity.ok(commentCommandService.updateComment(auth.getId(), showId, commentId, commentRequest));
    }

    @DeleteMapping("/shows/{showId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@Auth AuthUser auth, @PathVariable Long showId, @PathVariable Long commentId) {
        commentCommandService.deleteComment(auth.getId(), showId, commentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/shows/{showId}/comments")
    public ResponseEntity<PageCommentResponse> getComments(@PathVariable Long showId, @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(commentQueryService.getComments(showId, pageable));
    }

}
