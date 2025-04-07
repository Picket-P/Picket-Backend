package com.example.picket.domain.ticket.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.domain.ticket.dto.response.GetTicketResponse;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketQueryService {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public Page<Ticket> getTickets(Long userId, int size, int page) {

        Sort sortStandard = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page - 1, size, sortStandard);

        return ticketRepository.findByUser(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Ticket getTicket(Long userId, Long ticketId) {

        Ticket ticket = ticketRepository.findByTicketId(ticketId).orElseThrow(
                () -> new CustomException(ErrorCode.TICKET_NOT_FOUND)
        );

        validateUserInfo(userId,ticket);

        return ticket;

    }

    private void validateUserInfo(Long userId, Ticket ticket) {
        if (ticket.getUser().getId() != userId) {
            throw new CustomException(ErrorCode.TICKET_ACCESS_DENIED);
        }
    }
}
