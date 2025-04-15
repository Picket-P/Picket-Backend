package com.example.picket.domain.booking.service;

import com.example.picket.common.enums.SeatStatus;
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
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

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

    @Transactional
    public Order booking(Long showId, Long showDateId, Long userId, List<Long> seatIds) throws InterruptedException {

        Show foundShow = showQueryService.getShow(showId);
        checkBookingTime(foundShow);

        seatHoldingService.seatHoldingCheck(userId, seatIds);

        User foundUser = userQueryService.getUser(userId);
        ShowDate foundShowDate = showDateQueryService.getShowDate(showDateId);

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

    private List<Ticket> createTickets(User user, Show show, List<Long> seatIds) {
        return seatIds.stream()
                .map(seatId -> {
                    Seat seat = seatQueryService.getSeat(seatId);
                    seat.updateSeatStatus(SeatStatus.RESERVED);
                    return ticketCommandService.createTicketVer2(user, show, seat, TicketStatus.TICKET_CREATED);
                })
                .toList();
    }
}
