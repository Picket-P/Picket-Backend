package com.example.picket.domain.ticket.service;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.example.picket.common.enums.SeatStatus;
import com.example.picket.common.enums.TicketStatus;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.service.SeatQueryService;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.ticket.repository.TicketRepository;
import com.example.picket.domain.user.entity.User;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketCommandService {

    private final TicketRepository ticketRepository;
    private final SeatQueryService seatQueryService;

    @Transactional
    public List<Ticket> createTicket(User user, Show show, List<Long> seatIds) {
        return seatIds.stream().map(seatId -> {
            Seat foundSeat = seatQueryService.getSeat(seatId);
            foundSeat.updateSeatStatus(SeatStatus.RESERVED);
            Ticket ticket = Ticket.toEntity(user, show, foundSeat, foundSeat.getPrice(), TicketStatus.TICKET_CREATED);
            ticketRepository.save(ticket);
            return ticket;
        }).toList();
    }

    @Transactional
    public List<Ticket> deleteTicket(User user, List<Long> ticketIds) {
        return ticketIds.stream().map(ticketId -> {
            Ticket ticket = ticketRepository.findByTicketId(ticketId).orElseThrow(() -> new CustomException(NOT_FOUND, "존재하지 않는 티켓입니다."));
            validateTicketStatus(ticket); // 티켓 상태 검증
            validateUserInfo(user.getId(), ticket); // 티켓의 유저 정보 검증
            ticket.updateTicketStatus(TicketStatus.TICKET_CANCELED);
            ticket.getSeat().updateSeatStatus(SeatStatus.AVAILABLE);
            return ticket;
        }).toList();

    }

    private void validateUserInfo(Long userId, Ticket ticket) {
        if (ticket.getUser().getId() != userId) {
            throw new CustomException(FORBIDDEN, "예매자 본인만 취소할 수 있습니다.");
        }
    }

    private void validateTicketStatus(Ticket ticket) {
        if (ticket.getStatus() == TicketStatus.TICKET_CANCELED) {
            throw new CustomException(CONFLICT, "이미 취소된 티켓입니다.");
        }
    }
}
