package com.example.picket.domain.ticket.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.example.picket.common.enums.SeatStatus;
import com.example.picket.common.enums.TicketStatus;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.service.SeatQueryService;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.service.ShowDateQueryService;
import com.example.picket.domain.show.service.ShowQueryService;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.ticket.repository.TicketRepository;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserQueryService;

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
    private final ShowQueryService showQueryService;

//    @Transactional
//    public Ticket createTicket(Long userId, UserRole userRole, Long seatId) {
//
//        Seat foundSeat = getSeat(seatId);
//        Show foundShow = foundSeat.getShowDate().getShow();
//        User foundUser = getUser(userId);
//        BigDecimal foundPrice = foundSeat.getPrice();
//        ShowDate foundShowDate = foundSeat.getShowDate();
//
//        validateTicketCreationTime(foundShow); // 예매 시간 검증
//
//        validateAvailableTicketCount(foundUser, foundShow); // 예매 가능 티켓 개수 검증
//
//        validateSeat(foundSeat); // 좌석 검증
//
//        //foundShowDate.updateCountOnBooking(); //showDate 필드 업데이트
//
//        Ticket ticket = Ticket.toEntity(foundUser, foundShow, foundSeat, foundPrice, TicketStatus.TICKET_CREATED);
//
//        foundSeat.updateSeatStatus(SeatStatus.RESERVED); // 좌석 상태 업데이트
//
//        ticketRepository.save(ticket);
//
//        return ticket;
//
//    }

    @Transactional
    public Ticket deleteTicket(Long ticketId, Long userId) {

        Ticket ticket = ticketRepository.findByTicketId(ticketId).orElseThrow(
                () -> new CustomException(NOT_FOUND, "존재하지 않는 Ticket입니다.")
        );

        ShowDate foundshowDate = getShowDate(ticket.getShow());
        Seat foundSeat = getSeat(ticket.getSeat().getId());

        validateTicketDeletionTime(foundshowDate); // 취소 시간 검증

        validateTicketStatus(ticket); // 티켓 상태 검증

        validateUserInfo(userId, ticket); // 티켓의 유저 정보 검증

        ticket.updateTicketStatus(TicketStatus.TICKET_CANCELED);

        //foundshowDate.updateCountOnCancellation(); // showDate 필드 업데이트

        foundSeat.updateSeatStatus(SeatStatus.AVAILABLE); // 좌석 상태 업데이트

        // TODO : 환불 처리 로직 구현 ?
        // 환불이 성공적으로 끝났다면, TicketStatus와 deletedAt 업데이트

        ticket.updateTicketStatus(TicketStatus.TICKET_EXPIRED); // 티켓 상태 업데이트

        ticket.updateDeletedAt(LocalDateTime.now()); // 티켓 소프트 딜리트

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
        if (seat.getSeatStatus() == SeatStatus.RESERVED) {
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

    private void validateTicketDeletionTime(ShowDate showDate) {
        if (LocalDate.now().isAfter(showDate.getDate())) {
            throw new CustomException(FORBIDDEN, "공연 시작 날짜 이전에만 취소 가능합니다.");
        }
    }

    private void validateUserInfo(Long userId, Ticket ticket) {
        if (ticket.getUser().getId() != userId) {
            throw new CustomException(FORBIDDEN, "예매자 본인만 취소할 수 있습니다.");
        }
    }

    private void validateTicketStatus(Ticket ticket) {
        if (ticket.getStatus() == TicketStatus.TICKET_EXPIRED) {
            throw new CustomException(CONFLICT, "이미 취소된 티켓입니다.");
        }
    }

    private void validateAvailableTicketCount(User user, Show show) {
        int reservedTicketCount = ticketRepository.countTicketByUserAndShowWithTicketStatus(user, show, TicketStatus.TICKET_CREATED);
        if (reservedTicketCount >= show.getTicketsLimitPerUser()) {
            throw new CustomException(CONFLICT, "예매 가능한 티켓 수를 초과합니다.");
        }
    }


    @Transactional
    public Ticket createTicket(User user, Show show, Seat seat, TicketStatus ticketStatus) {
        Ticket ticket = Ticket.toEntity(user, show, seat, seat.getPrice(), ticketStatus);
        seat.updateSeatStatus(SeatStatus.RESERVED);
        ticketRepository.save(ticket);
        return ticket;
    }


}
