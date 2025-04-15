package com.example.picket.domain.ticket.service;

import com.example.picket.common.enums.TicketStatus;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.ticket.repository.TicketRepository;
import java.util.List;

import com.example.picket.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.http.HttpStatus.*;

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
                () -> new CustomException(NOT_FOUND, "존재하지 않는 Ticket입니다.")
        );

        validateUserInfo(userId, ticket);

        return ticket;

    }

    @Transactional(readOnly = true)
    public List<Long> hasValidTicket(List<Long> userIds, Long showId) {
        return ticketRepository.findUserIdsWithValidTicket(userIds, showId, TicketStatus.TICKET_CANCELED);
    }

    private void validateUserInfo(Long userId, Ticket ticket) {
        if (ticket.getUser().getId() != userId) {
            throw new CustomException(FORBIDDEN, "본인이 예매한 티켓만 조회할 수 있습니다.");
        }
    }

   @Transactional(readOnly = true)
   public void checkTicketLimit (User user, Show show) {
       int reservedTicketCount = ticketRepository.countTicketByUserAndShowWithTicketStatus(user, show, TicketStatus.TICKET_CREATED);
       if (reservedTicketCount >= show.getTicketsLimitPerUser()) {
           throw new CustomException(CONFLICT, "예매 가능한 티켓 수를 초과합니다.");
       }
   }
}
