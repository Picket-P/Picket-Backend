package com.example.picket.domain.comment.service;

import com.example.picket.common.enums.TicketStatus;
import com.example.picket.domain.comment.entity.Comment;
import com.example.picket.domain.comment.repository.CommentRepository;
import com.example.picket.domain.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public Page<Comment> getComments(Long showId, Pageable pageable) {
        return commentRepository.findByShowId(showId, pageable);
    }

    // todo : 추후 ticketService로 이동
    public List<Long> hasValidTicket(List<Long> userIds, Long showId) {
        return ticketRepository.findUserIdsWithValidTicket(userIds, showId, TicketStatus.TICKET_CANCELED);
    }
}
