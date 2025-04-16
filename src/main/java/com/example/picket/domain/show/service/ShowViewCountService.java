package com.example.picket.domain.show.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.show.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowViewCountService {

    private static final String VIEW_KEY_FORMAT = "view:show:%d:user:%d";

    private final ShowRepository showRepository;
    private final StringRedisTemplate redisTemplate;

    @Async
    @Transactional
    public void incrementViewCount(AuthUser authUser, Long showId) {
        try {
            if (authUser == null) {
                return;
            }

            String key = String.format(VIEW_KEY_FORMAT, showId, authUser.getId());
            String hasViewed = redisTemplate.opsForValue().get(key);

            if (hasViewed == null) {
                redisTemplate.opsForValue().set(key, "viewed", getSecondsUntilMidnight());
                showRepository.incrementViewCount(showId);
            }

        } catch (Exception e) {
            throw new CustomException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // 자정까지 남은 시간 계산 (TTL 설정용)
    private Duration getSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        return Duration.between(now, midnight);
    }

}
