package com.example.picket.domain.ticket.service;

import com.example.picket.common.enums.TicketStatus;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.repository.SeatRepository;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import com.example.picket.domain.show.repository.ShowRepository;
import com.example.picket.domain.ticket.dto.response.CreateTicketResponse;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.ticket.repository.TicketRepository;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final ShowDateRepository showDateRepository;

    @Transactional
    public CreateTicketResponse createTicket(Long userId, Long seatId) {

        // TODO : 예매 가능한 UserRole -> USER만

        Seat foundSeat = getSeat(seatId);
        Show foundShow = foundSeat.getShow();
        validateTicketCreationTime(foundShow);

        validateSeat(foundSeat);

        User foundUser = getUser(userId);
        BigDecimal foundPrice = foundSeat.getPrice();
        ShowDate foundShowDate = getShowDate(foundShow);

        discountShowDateRemainCount(foundShowDate);

        Ticket ticket = Ticket.builder()
                .user(foundUser)
                .show(foundShow)
                .seat(foundSeat)
                .price(foundPrice)
                .status(TicketStatus.TICKET_CREATED)
                .build();

        ticketRepository.save(ticket);

        return CreateTicketResponse.from(ticket);

    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
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
        if(ticketRepository.existsBySeat(seat)){
            throw new CustomException(ErrorCode.ALREADY_RESERVED_SEAT);
        }
    }

    private void validateTicketCreationTime(Show show) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(show.getReservationStart())) {
            throw new CustomException(ErrorCode.BEFORE_SHOW_RESERVATION_TIME);
        }

        if (now.isAfter(show.getReservationEnd())) {
            throw new CustomException(ErrorCode.AFTER_SHOW_RESERVATION_TIME);
        }
    }

    private void discountShowDateRemainCount(ShowDate showDate) {
        showDate.discountRemainCount();
    }
}
