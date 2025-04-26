package com.example.picket.domain.booking.service;

import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.ticket.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class BookingFacade {
    private final BookingService bookingService;
    private final RedissonClient redissonClient;
    private final String KEY_PREFIX = "BOOKING-LOCK:SHOW-DATE:";

    public Order booking(Long showId, Long showDateId, Long userId, List<Long> seatIds, String paymentKey, String orderId, Number amount) throws InterruptedException {
        String lockKey = KEY_PREFIX + showDateId;
        RLock lock = redissonClient.getFairLock(lockKey);

        if (lock.tryLock(10, TimeUnit.SECONDS)) {
            try {
                return bookingService.booking(showId, showDateId, userId, seatIds, paymentKey, orderId, amount);
            } finally {
                lock.unlock();
            }
        } else {
            throw new IllegalStateException("락 획득 실패: " + lockKey);
        }
    }

    public List<Ticket> cancelBooking(Long showId, Long showDateId, Long userId, List<Long> ticketIds) throws InterruptedException {
        String lockKey = KEY_PREFIX + showDateId;
        RLock lock = redissonClient.getFairLock(lockKey);

        if (lock.tryLock(10, TimeUnit.SECONDS)) {
            try {
                return bookingService.cancelBooking(showId, showDateId, userId, ticketIds);
            } finally {
                lock.unlock();
            }
        } else {
            throw new IllegalStateException("락 획득 실패: " + lockKey);
        }
    }
}
