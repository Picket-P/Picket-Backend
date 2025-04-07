package com.example.picket.domain.comment.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.comment.dto.request.CommentRequest;
import com.example.picket.domain.comment.dto.response.CommentResponse;
import com.example.picket.domain.comment.dto.response.PageCommentResponse;
import com.example.picket.domain.comment.entity.Comment;
import com.example.picket.domain.comment.service.CommentCommandService;
import com.example.picket.domain.comment.service.CommentQueryService;
import com.example.picket.domain.ticket.service.TicketQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class CommentController {

    private final CommentCommandService commentCommandService;
    private final CommentQueryService  commentQueryService;
    private final TicketQueryService ticketQueryService;

    @PostMapping("/shows/{showId}/comments")
    public ResponseEntity<CommentResponse> createComment(@Auth AuthUser auth,
                                                         @PathVariable Long showId,
                                                         @RequestBody CommentRequest commentRequest) {

        Comment comment = commentCommandService.createComment(auth.getId(), showId, commentRequest);
        boolean hasTicket = ticketQueryService
                .hasValidTicket(List.of(comment.getUser().getId()), showId)
                .contains(comment.getUser().getId());

        return ResponseEntity.ok(CommentResponse.toDto(comment, hasTicket));
    }

    @PatchMapping("/shows/{showId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(@Auth AuthUser auth,
                                                         @PathVariable Long showId,
                                                         @PathVariable Long commentId,
                                                         @RequestBody CommentRequest commentRequest) {

        Comment comment = commentCommandService.updateComment(auth.getId(), showId, commentId, commentRequest);
        boolean hasTicket = ticketQueryService
                .hasValidTicket(List.of(comment.getUser().getId()), showId)
                .contains(comment.getUser().getId());

        return ResponseEntity.ok(CommentResponse.toDto(comment, hasTicket));
    }

    @DeleteMapping("/shows/{showId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@Auth AuthUser auth, @PathVariable Long showId, @PathVariable Long commentId) {
        commentCommandService.deleteComment(auth.getId(), showId, commentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/shows/{showId}/comments")
    public ResponseEntity<PageCommentResponse> getComments(@PathVariable Long showId, @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<Comment> comments = commentQueryService.getComments(showId, pageable);

        List<Long> userIds = comments.getContent().stream()
                .map(comment -> comment.getUser().getId())
                .distinct()
                .toList();

        List<Long> validTicketUserIds = ticketQueryService.hasValidTicket(userIds, showId);

        return ResponseEntity.ok(PageCommentResponse.toDto(comments, comment ->
                validTicketUserIds.contains(comment.getUser().getId())));
    }

}
