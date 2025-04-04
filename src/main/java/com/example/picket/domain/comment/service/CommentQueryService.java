package com.example.picket.domain.comment.service;

import com.example.picket.common.enums.TicketStatus;
import com.example.picket.domain.comment.dto.response.PageCommentResponse;
import com.example.picket.domain.comment.entity.Comment;
import com.example.picket.domain.comment.repository.CommentRepository;
import com.example.picket.domain.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public PageCommentResponse getComments(Long showId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByShowId(showId, pageable);

        return PageCommentResponse.from(comments, comment ->
                hasValidTicket(comment.getUser().getId(), showId)
        );
    }

    // todo : 추후 ticketService로 이동
    private Boolean hasValidTicket(Long userId, Long showId) {
        return ticketRepository.existsByUserIdAndShowIdAndStatusNot(userId, showId, TicketStatus.TICKET_CANCELED);
    }
}
