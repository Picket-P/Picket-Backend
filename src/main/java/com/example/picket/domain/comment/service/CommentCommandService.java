package com.example.picket.domain.comment.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.comment.dto.request.CommentRequest;
import com.example.picket.domain.comment.entity.Comment;
import com.example.picket.domain.comment.repository.CommentRepository;
import com.example.picket.domain.show.service.ShowQueryService;
import com.example.picket.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final ShowQueryService showQueryService;
    private final UserQueryService userQueryService;

    @Transactional
    public Comment createComment(Long userId, Long showId, CommentRequest commentRequest) {

        return commentRepository.save(Comment.create(commentRequest.getContent()
                , showQueryService.getShow(showId)
                , userQueryService.getUser(userId))
        );
    }

    @Transactional
    public Comment updateComment(Long userId, Long showId, Long commentId, CommentRequest commentRequest) {

        Comment comment = commentRepository.findByIdAndShowIdAndUserId(commentId, showId, userId)
                .orElseThrow(() -> new CustomException(NOT_FOUND, "해당 공연에 해당 사용자의 댓글이 없습니다."));
        comment.updateContent(commentRequest.getContent());
        return comment;
    }

    @Transactional
    public void deleteComment(Long userId, Long showId, Long commentId) {
        Comment comment = commentRepository.findByIdAndShowId(commentId, showId)
                .orElseThrow(() -> new CustomException(NOT_FOUND, "해당 공연에 해당 사용자의 댓글이 없습니다."));
        validateDeletePermission(userId, comment);
        comment.updateDeletedAt(LocalDateTime.now());
    }

    private void validateDeletePermission(Long userId, Comment comment) {
        if (!userId.equals(comment.getUser().getId()) && !userId.equals(comment.getShow().getDirectorId())) {
            throw new CustomException(FORBIDDEN, "해당 댓글의 삭제 권한이 없습니다.");
        }
    }
}
