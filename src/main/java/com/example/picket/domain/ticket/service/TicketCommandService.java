package com.example.picket.domain.ticket.service;

import com.example.picket.common.enums.TicketStatus;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.repository.SeatRepository;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.ticket.repository.TicketRepository;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserQueryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketCommandService {

    private final TicketRepository ticketRepository;
    private final UserQueryService userQueryService;
    private final SeatRepository seatRepository;
    private final ShowDateRepository showDateRepository;

    @Transactional
    public Ticket createTicket(Long userId, UserRole userRole, Long seatId) {

        Seat foundSeat = getSeat(seatId);
        Show foundShow = foundSeat.getShow();

        validateTicketCreationTime(foundShow);

        validateSeat(foundSeat);

        User foundUser = getUser(userId);
        BigDecimal foundPrice = foundSeat.getPrice();
        ShowDate foundShowDate = getShowDate(foundShow);

        discountShowDateRemainCount(foundShowDate);

        Ticket ticket = Ticket.toEntity(foundUser, foundShow, foundSeat, foundPrice, TicketStatus.TICKET_CREATED);

        ticketRepository.save(ticket);

        return ticket;

    }

    @Transactional
    public Ticket deleteTicket(Long ticketId, Long userId) {

        Ticket ticket = ticketRepository.findByTicketId(ticketId).orElseThrow(
                () -> new CustomException(ErrorCode.TICKET_NOT_FOUND)
        );

        validateUserInfo(userId, ticket);

        ShowDate showDate = getShowDate(ticket.getShow());

        if (LocalDate.now().isBefore(showDate.getDate())) {
            ticket.updateTicketStatus(TicketStatus.TICKET_CANCELED);
            // TODO : 환불 처리 로직 구현 ?
            // 환불이 성공적으로 끝났다면, TicketStatus와 deletedAt 업데이트
            ticket.updateTicketStatus(TicketStatus.TICKET_EXPIRED);
            ticket.updateDeletedAt(LocalDateTime.now());
        }

        return ticket;
    }

    private User getUser(Long userId) {
        return userQueryService.findById(userId);
    }

    private Seat getSeat(Long seatId) {
        return seatRepository.findById(seatId).orElseThrow(
                () -> new CustomException(ErrorCode.SEAT_NOT_FOUND));
    }

    private ShowDate getShowDate(Show show) {
        return showDateRepository.findByShow(show).orElseThrow(
                () -> new CustomException(ErrorCode.SHOW_DATE_NOT_FOUND));
    }

    private void validateSeat(Seat seat) {
        if (ticketRepository.existsBySeat(seat)) {
            throw new CustomException(ErrorCode.SEAT_ALREADY_RESERVED);
        }
    }

    private void validateTicketCreationTime(Show show) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(show.getReservationStart())) {
            throw new CustomException(ErrorCode.SHOW_RESERVATION_TIME_INVALID_BEFORE_SHOW);
        }

        if (now.isAfter(show.getReservationEnd())) {
            throw new CustomException(ErrorCode.SHOW_RESERVATION_TIME_INVALID_AFTER_SHOW);
        }
    }

    private void discountShowDateRemainCount(ShowDate showDate) {
        showDate.discountRemainCount();
    }

    private void validateUserInfo(Long userId, Ticket ticket) {
        if (ticket.getUser().getId() != userId) {
            throw new CustomException(ErrorCode.TICKET_CANCEL_FORBIDDEN);
        }
    }


}
