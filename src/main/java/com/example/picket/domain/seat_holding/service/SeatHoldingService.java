package com.example.picket.domain.seat_holding.service;

import com.example.picket.common.enums.SeatStatus;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.seat.service.SeatQueryService;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.service.ShowQueryService;
import com.example.picket.domain.ticket.service.TicketQueryService;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@RequiredArgsConstructor
public class SeatHoldingService {

    private final RedissonClient redissonClient;
    private final SeatQueryService seatQueryService;
    private final ShowQueryService showQueryService;

    private final String KEY_PREFIX = "seat-holding-lock:seat:";
    private final Duration HOLDING_DURATION = Duration.ofMinutes(30);

    @Transactional
    public void seatHoldingLock(Long userId, Long showId, List<Long> seatIds) {

        checkSeatLimit(showId, seatIds);

        List<Long> successfullyLockedSeats = new ArrayList<>();

        for (Long seatId : seatIds) {
            RBucket<String> seatLock = redissonClient.getBucket(KEY_PREFIX + seatId);

            boolean success = seatLock.setIfAbsent(userId.toString(), HOLDING_DURATION);

            if (!success) {
                rollback(successfullyLockedSeats);
                throw new CustomException(CONFLICT, "이미 선점된 좌석입니다.");
            }

            successfullyLockedSeats.add(seatId);
            seatQueryService.getSeat(seatId).updateSeatStatus(SeatStatus.OCCUPIED);
        }
    }

    @Transactional
    public void seatHoldingUnLock(List<Long> seatIds) {
        for (Long seatId : seatIds) {
            RBucket<String> seatLock = redissonClient.getBucket(KEY_PREFIX + seatId);
            seatLock.delete();
        }
    }

    @Transactional
    public void seatHoldingCheck(Long userId, List<Long> seatIds) {
        for (Long seatId : seatIds) {
            RBucket<String> seatLock = redissonClient.getBucket(KEY_PREFIX + seatId);
            String strUserId = seatLock.get();

            if (strUserId == null || !strUserId.equals(userId.toString())) {
                throw new CustomException(FORBIDDEN, "좌석 선점이 만료되었습니다. 좌석을 선점한 사용자가 아닙니다.");
            }
        }
    }

    private void rollback(List<Long> seatIds) {
        for (Long seatId : seatIds) {
            RBucket<String> seatLock = redissonClient.getBucket(KEY_PREFIX + seatId);
            seatLock.delete();

            seatQueryService.getSeat(seatId).updateSeatStatus(SeatStatus.AVAILABLE);
        }
    }

    private void checkSeatLimit(Long showId, List<Long> seatIds) {
        Show foundShow = showQueryService.getShow(showId);
        if (foundShow.getTicketsLimitPerUser() < seatIds.size()) {
            throw new CustomException(CONFLICT, "선택 가능한 좌석 개수를 초과하였습니다.");
        }
    }
}
