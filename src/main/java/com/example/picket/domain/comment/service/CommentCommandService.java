package com.example.picket.domain.comment.service;

import static com.example.picket.common.exception.ErrorCode.COMMENT_DELETE_NOT_ALLOWED;
import static com.example.picket.common.exception.ErrorCode.COMMENT_NOT_FOUND_IN_SHOW;

import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.comment.dto.request.CommentRequest;
import com.example.picket.domain.comment.entity.Comment;
import com.example.picket.domain.comment.repository.CommentRepository;
import com.example.picket.domain.show.service.ShowQueryService;
import com.example.picket.domain.user.service.UserQueryService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final ShowQueryService showQueryService;
    private final UserQueryService userQueryService;

    @Transactional
    public Comment createComment(Long userId, Long showId, CommentRequest commentRequest) {

        return commentRepository.save(Comment.toEntity(commentRequest.getContent()
                , showQueryService.findById(showId)
                , userQueryService.getReferenceById(userId))
        );
    }

    @Transactional
    public Comment updateComment(Long userId, Long showId, Long commentId, CommentRequest commentRequest) {

        Comment comment = commentRepository.findByIdAndShowIdAndUserId(commentId, showId, userId)
                .orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND_IN_SHOW));
        comment.updateContent(commentRequest.getContent());
        return comment;
    }

    @Transactional
    public void deleteComment(Long userId, Long showId, Long commentId) {
        Comment comment = commentRepository.findByIdAndShowId(commentId, showId)
                .orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND_IN_SHOW));
        validateDeletePermission(userId, comment);
        comment.updateDeletedAt(LocalDateTime.now());
    }

    private void validateDeletePermission(Long userId, Comment comment) {
        if (!userId.equals(comment.getUser().getId()) && !userId.equals(comment.getShow().getDirectorId())) {
            throw new CustomException(COMMENT_DELETE_NOT_ALLOWED);
        }
    }
}
