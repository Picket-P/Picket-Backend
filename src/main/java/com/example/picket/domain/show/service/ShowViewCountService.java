package com.example.picket.domain.show.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ShowViewCountService {

    private static final String VIEW_KEY_FORMAT = "view:show:%d:user:%d";

    private final ShowRepository showRepository;
    private final StringRedisTemplate redisTemplate;

    @Async
    @Transactional
    public CompletableFuture<Integer> incrementViewCount(AuthUser authUser, Long showId) {
        if (authUser == null) {
            return CompletableFuture.completedFuture(null);
        }

        String key = String.format(VIEW_KEY_FORMAT, showId, authUser.getId());

        try {
            String hasViewed = redisTemplate.opsForValue().get(key);

            if (hasViewed == null) {
                Show show = showRepository.findById(showId)
                    .orElseThrow(() -> new CustomException(NOT_FOUND, "해당 공연을 찾을 수 없습니다."));

                show.incrementViewCount();
                redisTemplate.opsForValue().set(key, "viewed", getSecondsUntilMidnight());

                return CompletableFuture.completedFuture(show.getViewCount());
            }

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            rollBackRedis(key);
            throw new CustomException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void rollBackRedis(String key) {
        boolean hasKey = redisTemplate.opsForValue().get(key) != null;
        if (hasKey) {
            redisTemplate.delete(key);
        }
    }

    // 자정까지 남은 시간 계산 (TTL 설정용)
    private Duration getSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        return Duration.between(now, midnight);
    }

}
