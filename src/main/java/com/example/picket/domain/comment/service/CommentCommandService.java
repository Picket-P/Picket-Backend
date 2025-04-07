package com.example.picket.domain.comment.service;

import com.example.picket.common.enums.TicketStatus;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.comment.dto.request.CommentRequest;
import com.example.picket.domain.comment.dto.response.CommentResponse;
import com.example.picket.domain.comment.entity.Comment;
import com.example.picket.domain.comment.repository.CommentRepository;
import com.example.picket.domain.show.repository.ShowRepository;
import com.example.picket.domain.ticket.repository.TicketRepository;
import com.example.picket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.picket.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final ShowRepository showRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse createComment(Long userId, Long showId, CommentRequest commentRequest) {

        Comment comment = Comment
                .builder()
                .content(commentRequest.getContent())
                .show(showRepository.findById(showId).orElseThrow(()-> new CustomException(SHOW_NOT_FOUND)))
                .user(userRepository.getReferenceById(userId))
                .build();

        commentRepository.save(comment);


        return CommentResponse.from(comment, hasValidTicket(userId, showId));
    }

    @Transactional
    public CommentResponse updateComment(Long userId, Long showId, Long commentId, CommentRequest commentRequest) {

        Comment comment = commentRepository.findByIdAndShowIdAndUserId(commentId, showId, userId)
                .orElseThrow(()-> new CustomException(COMMENT_NOT_FOUND_IN_SHOW));
        comment.updateContent(commentRequest.getContent());
        return CommentResponse.from(comment, hasValidTicket(userId, showId));
    }

    @Transactional
    public void deleteComment(Long userId, Long showId, Long commentId) {
        Comment comment = commentRepository.findByIdAndShowId(commentId, showId)
                .orElseThrow(()-> new CustomException(COMMENT_NOT_FOUND_IN_SHOW));
        validateDeletePermission(userId, comment);

        comment.deleteComment();
    }

    private void validateDeletePermission(Long userId, Comment comment) {
        if(!userId.equals(comment.getUser().getId()) || !userId.equals(comment.getShow().getId())) {
            throw new CustomException(COMMENT_DELETE_NOT_ALLOWED);
        }
    }

    // todo : 추후 ticketService로 이동
    private Boolean hasValidTicket(Long userId, Long showId) {
        return ticketRepository.existsByUserIdAndShowIdAndStatusNot(userId, showId, TicketStatus.TICKET_CANCELED);
    }

}
