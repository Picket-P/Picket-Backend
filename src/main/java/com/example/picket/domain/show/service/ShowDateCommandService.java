package com.example.picket.domain.show.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import com.example.picket.domain.show.repository.jdbc.ShowDateJdbcRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class ShowDateCommandService {

    private final ShowDateRepository showDateRepository;
    private final ShowDateJdbcRepository showDateJdbcRepository;
    private final RedissonClient redissonClient;
    private final String KEY_PREFIX = "UPDATE-COUNT-LOCK:SHOW-DATE:";

    public void createShowDate(ShowDate showDate) {
        showDateRepository.save(showDate);
    }

    public void createShowDatesJdbc(List<ShowDate> showDates) {
        showDateJdbcRepository.saveAllJdbc(showDates);
    }

    public void showDateUpdate(Long showDateId, int count) throws InterruptedException {
        String lockKey = KEY_PREFIX + showDateId;
        RLock lock = redissonClient.getFairLock(lockKey);

        if (lock.tryLock(10, TimeUnit.SECONDS)) {
            try {
                ShowDate foundShowDate = showDateRepository.findById(showDateId)
                        .orElseThrow(() -> new CustomException(NOT_FOUND, "공연 날짜를 찾을 수 없습니다."));
                foundShowDate.updateCountOnBooking(count);
            } finally {
                lock.unlock();
            }
        } else {
            throw new IllegalStateException("락 획득 실패: " + lockKey);
        }
    }

}

