package com.example.picket.domain.comment.controller;

import com.example.picket.domain.comment.dto.request.CommentRequest;
import com.example.picket.domain.comment.dto.response.CommentResponse;
import com.example.picket.domain.comment.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/shows/{showId}/comments")
    public ResponseEntity<CommentResponse> createComment(HttpSession session, @PathVariable Long showId, @RequestBody CommentRequest commentRequest) {
        Long userId = session.getAttribute("userId")==null?null:(Long)session.getAttribute("userId");
        return ResponseEntity.ok(commentService.createComment(userId, showId, commentRequest));
    }
}
