package com.example.picket.domain.ranking.scheduler;

import com.example.picket.common.enums.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularKeywordScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    private static final String SEARCH_KEYWORD_KEY = "search:keywords";
    private static final String POPULAR_KEYWORD_RANKING_KEY = "ranking:popular_keywords";

    // 카테고리 검색 시 호출
    public void incrementSearchKeyword(Category category) {
        if (category != null) {
            redisTemplate.opsForZSet().incrementScore(SEARCH_KEYWORD_KEY, category.name(), 1);
        }
    }

    @Scheduled(cron = "0 0/30 * * * ?") // 30분마다
    public void updatePopularKeywords() {
        RLock lock = redissonClient.getLock("lock:popular_keywords");
        try {
            if (lock.tryLock(0, 60, TimeUnit.SECONDS)) {
                Set<ZSetOperations.TypedTuple<String>> topKeywords = redisTemplate.opsForZSet()
                        .reverseRangeWithScores(SEARCH_KEYWORD_KEY, 0, 9);
                redisTemplate.delete(POPULAR_KEYWORD_RANKING_KEY);
                redisTemplate.opsForList().rightPushAll(
                        POPULAR_KEYWORD_RANKING_KEY,
                        topKeywords.stream()
                                .map(tuple -> tuple.getValue() + ":" + tuple.getScore())
                                .toList()
                );
                redisTemplate.opsForList().trim(POPULAR_KEYWORD_RANKING_KEY, -10, -1);
                redisTemplate.expire(POPULAR_KEYWORD_RANKING_KEY, 24, TimeUnit.HOURS); // TTL 설정
            }
        } catch (InterruptedException e) {
            log.error("Failed to acquire lock for popular keywords", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 초기화
    public void resetSearchKeywords() {
        RLock lock = redissonClient.getLock("lock:reset_keywords");
        try {
            if (lock.tryLock(0, 60, TimeUnit.SECONDS)) {
                redisTemplate.delete(SEARCH_KEYWORD_KEY);
            }
        } catch (InterruptedException e) {
            log.error("키워드 초기화 중 다른 작업과 충돌하여 락 획득에 실패했습니다.", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
