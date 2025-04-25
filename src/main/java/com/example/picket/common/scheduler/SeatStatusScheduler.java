package com.example.picket.common.scheduler;

import com.example.picket.common.enums.SeatStatus;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatStatusScheduler {
    private final SeatRepository seatRepository;
    private final StringRedisTemplate redisTemplate;

    @Scheduled(cron = "0 5 * * * *") // 5분마다 실행
    @SchedulerLock(name = "SEATSTATUS-SCHEDULER",
            lockAtLeastFor = "PT1M", lockAtMostFor = "PT5M")
    public void cleanUpOccupiedSeats() {

        log.info("[SEAT CLEANUP] 예약된 좌석 상태 정리 작업 시작");

        List<Seat> occupiedSeats = seatRepository.findAllBySeatStatus(SeatStatus.OCCUPIED);

        log.info("[SEAT CLEANUP] 점유된 좌석 수: {}", occupiedSeats.size());

        List<Seat> updatedSeats = new ArrayList<>();

        for (Seat seat : occupiedSeats) {
            String redisSeatKey = "SEAT-HOLDING-LOCK:SEAT:" + seat.getId();

            Boolean exists = redisTemplate.hasKey(redisSeatKey);
            if (exists == null || !exists) {
                seat.updateSeatStatus(SeatStatus.AVAILABLE);
                updatedSeats.add(seat);
                log.info("[SEAT CLEANUP] Redis 키 없음 → 좌석 상태 AVAILABLE로 변경 (seatId: {})", seat.getId());
            }
        }

        seatRepository.saveAll(updatedSeats);

        log.info("[SEAT CLEANUP] 상태 변경된 좌석 수: {}", updatedSeats.size());
        log.info("[SEAT CLEANUP] 예약된 좌석 상태 정리 작업 종료");
    }
}
