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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@Tag(name = "댓글 관리 API", description = "댓글 다건조회, 생성, 수정, 삭제 기능 API입니다.")
public class CommentController {

    private final CommentCommandService commentCommandService;
    private final CommentQueryService commentQueryService;
    private final TicketQueryService ticketQueryService;

    @Operation(summary = "댓글 생성", description = "댓글을 생성할 수 있습니다.")
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

    @Operation(summary = "댓글 수정", description = "댓글을 수정할 수 있습니다.")
    @PutMapping("/shows/{showId}/comments/{commentId}")
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

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제할 수 있습니다.")
    @DeleteMapping("/shows/{showId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@Auth AuthUser auth, @PathVariable Long showId,
                                              @PathVariable Long commentId) {
        commentCommandService.deleteComment(auth.getId(), showId, commentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 다건 조회", description = "댓글을 다건 조회할 수 있습니다.")
    @GetMapping("/shows/{showId}/comments")
    public ResponseEntity<PageCommentResponse> getComments(@PathVariable Long showId
            , @ParameterObject @PageableDefault(size = 10, page = 0, sort = "createdAt,desc") Pageable pageable) {
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
