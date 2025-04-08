package com.example.picket.domain.ticket.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.example.picket.common.enums.TicketStatus;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.service.SeatQueryService;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.service.ShowDateQueryService;
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
    private final SeatQueryService seatQueryService;
    private final ShowDateQueryService showDateQueryService;

    @Transactional
    public Ticket createTicket(Long userId, UserRole userRole, Long seatId) {

        Seat foundSeat = getSeat(seatId);
        Show foundShow = foundSeat.getShowDate().getShow();

        validateTicketCreationTime(foundShow);

        validateSeat(foundSeat);

        User foundUser = getUser(userId);
        BigDecimal foundPrice = foundSeat.getPrice();
        ShowDate foundShowDate = foundSeat.getShowDate();

        discountShowDateRemainCount(foundShowDate);

        Ticket ticket = Ticket.toEntity(foundUser, foundShow, foundSeat, foundPrice, TicketStatus.TICKET_CREATED);

        ticketRepository.save(ticket);

        return ticket;

    }

    @Transactional
    public Ticket deleteTicket(Long ticketId, Long userId) {

        Ticket ticket = ticketRepository.findByTicketId(ticketId).orElseThrow(
                () -> new CustomException(NOT_FOUND, "존재하지 않는 Ticket입니다.")
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
        return userQueryService.getUser(userId);
    }

    private Seat getSeat(Long seatId) {
        return seatQueryService.getSeat(seatId);
    }

    private ShowDate getShowDate(Show show) {
        return showDateQueryService.getShowDateByShow(show);
    }

    private void validateSeat(Seat seat) {
        if (ticketRepository.existsBySeat(seat)) {
            throw new CustomException(CONFLICT, "이미 예매된 좌석입니다.");
        }
    }

    private void validateTicketCreationTime(Show show) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(show.getReservationStart())) {
            throw new CustomException(BAD_REQUEST, "예매 시작 시간 전입니다.");
        }

        if (now.isAfter(show.getReservationEnd())) {
            throw new CustomException(BAD_REQUEST, "예매 종료 시간 이후 입니다.");
        }
    }

    private void discountShowDateRemainCount(ShowDate showDate) {
        showDate.discountRemainCount();
    }

    private void validateUserInfo(Long userId, Ticket ticket) {
        if (ticket.getUser().getId() != userId) {
            throw new CustomException(FORBIDDEN, "예매자 본인만 취소할 수 있습니다.");
        }
    }


}
