package com.example.picket.domain.booking.service;

import com.example.picket.common.enums.TicketStatus;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.order.service.OrderCommandService;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.service.SeatQueryService;
import com.example.picket.domain.seat_holding.service.SeatHoldingService;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.service.ShowDateQueryService;
import com.example.picket.domain.show.service.ShowQueryService;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.ticket.service.TicketCommandService;
import com.example.picket.domain.ticket.service.TicketQueryService;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final RedissonClient redissonClient;
    private final String KEY_PREFIX = "UPDATE-COUNT-LOCK:SHOW-DATE:";

    private final SeatHoldingService seatHoldingService;

    private final SeatQueryService seatQueryService;
    private final ShowQueryService showQueryService;
    private final UserQueryService userQueryService;
    private final ShowDateQueryService showDateQueryService;

    private final OrderCommandService orderCommandService;
    private final TicketCommandService ticketCommandService;
    private final TicketQueryService ticketQueryService;

    @Transactional
    public Order booking(Long showId, Long showDateId, Long userId, List<Long> seatIds) throws InterruptedException {

        Show foundShow = showQueryService.getShow(showId);
        checkBookingTime(foundShow);

        User foundUser = userQueryService.getUser(userId);
        ShowDate foundShowDate = showDateQueryService.getShowDate(showDateId);

        seatHoldingService.seatHoldingCheck(userId, seatIds);

        ticketQueryService.checkTicketLimit(foundUser, foundShow);

        List<Ticket> tickets = createTickets(foundUser, foundShow, seatIds);

        Order order = orderCommandService.createOrder(foundUser, tickets);

        String lockKey = KEY_PREFIX + showDateId;
        RLock lock = redissonClient.getFairLock(lockKey);

        if (lock.tryLock(10, TimeUnit.SECONDS)) {
            try {
                foundShowDate.updateCountOnBooking(seatIds.size());
            } finally {
                lock.unlock();
            }
        } else {
            throw new IllegalStateException("락 획득 실패: " + lockKey);
        }

        seatHoldingService.seatHoldingUnLock(seatIds);

        return order;
    }

    private void checkBookingTime(Show show) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(show.getReservationStart())) {
            throw new CustomException(BAD_REQUEST, "예매 시작 시간 전입니다.");
        }

        if (now.isAfter(show.getReservationEnd())) {
            throw new CustomException(BAD_REQUEST, "예매 종료 시간 이후 입니다.");
        }
    }

    private void checkCancelBookingTime(ShowDate showDate) {
        if (LocalDate.now().isAfter(showDate.getDate())) {
            throw new CustomException(FORBIDDEN, "공연 시작 날짜 이전에만 취소 가능합니다.");
        }
    }

    private List<Ticket> createTickets(User user, Show show, List<Long> seatIds) {
        return seatIds.stream()
                .map(seatId -> {
                    Seat seat = seatQueryService.getSeat(seatId);
                    return ticketCommandService.createTicket(user, show, seat, TicketStatus.TICKET_CREATED);
                })
                .toList();
    }

    public List<Ticket> cancelBooking(Long showId, Long showDateId, Long userId, List<Long> ticketIds) throws InterruptedException {

        ShowDate foundShowDate = showDateQueryService.getShowDate(showDateId);
        checkCancelBookingTime(foundShowDate);

        User foundUser = userQueryService.getUser(userId);
        List<Ticket> canceledTickets = ticketCommandService.deleteTicket(foundUser, ticketIds);

        String lockKey = KEY_PREFIX + showDateId;
        RLock lock = redissonClient.getFairLock(lockKey);

        if (lock.tryLock(10, TimeUnit.SECONDS)) {
            try {
                foundShowDate.updateCountOnBooking(ticketIds.size());
            } finally {
                lock.unlock();
            }
        } else {
            throw new IllegalStateException("락 획득 실패: " + lockKey);
        }

        return canceledTickets;

    }
}
