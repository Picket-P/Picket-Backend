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

import static com.example.picket.common.exception.ErrorCode.SHOW_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ShowRepository showRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse createComment(Long userId, Long showId, CommentRequest commentRequest) {

        //todo : userId 추후 수정 필요
        Comment comment = Comment
                .builder()
                .content(commentRequest.getContent())
                .show(showRepository.findById(showId).orElseThrow(()-> new CustomException(SHOW_NOT_FOUND)))
                .user(userRepository.getReferenceById(1L))
                .build();

        commentRepository.save(comment);


        return CommentResponse.from(comment, hasValidTicket(1L, showId));
    }

    private Boolean hasValidTicket(Long userId, Long showId) {
        return ticketRepository.existsByUserIdAndShowIdAndStatusNot(userId, showId, TicketStatus.TICKET_CANCELED);
    }

}
