package com.example.picket.domain.comment.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.comment.dto.request.CommentRequest;
import com.example.picket.domain.comment.dto.response.CommentResponse;
import com.example.picket.domain.comment.service.CommentCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class CommentController {

    private final CommentCommandService commentCommandService;

    @PostMapping("/shows/{showId}/comments")
    public ResponseEntity<CommentResponse> createComment(@Auth AuthUser auth, @PathVariable Long showId, @RequestBody CommentRequest commentRequest) {
        return ResponseEntity.ok(commentCommandService.createComment(auth.getId(), showId, commentRequest));
    }

    @PatchMapping("/shows/{showId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(@Auth AuthUser auth, @PathVariable Long showId, @PathVariable Long commentId, @RequestBody CommentRequest commentRequest) {
        return ResponseEntity.ok(commentCommandService.updateComment(auth.getId(), showId, commentId, commentRequest));
    }


}
