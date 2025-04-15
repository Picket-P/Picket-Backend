package com.example.picket.domain.ranking.scheduler;

import com.example.picket.domain.like.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeRankingScheduler {

    private final LikeRepository likeRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    private static final String LIKE_RANKING_KEY = "ranking:like_shows";

    @Scheduled(cron = "0 0/30 * * * ?") // 30분마다
    @Transactional(readOnly = true)
    public void updateLikeRanking() {
        RLock lock = redissonClient.getLock("lock:like_show_ranking");
        try {
            if (lock.tryLock(0, 60, TimeUnit.SECONDS)) {
                List<Object[]> topShows = likeRepository.findTop10ShowsByLikeCount();
                redisTemplate.delete(LIKE_RANKING_KEY);
                topShows.forEach(row -> redisTemplate.opsForZSet().add(
                        LIKE_RANKING_KEY,
                        row[0] + ":" + row[1],
                        ((Long) row[2]).doubleValue()
                ));
                redisTemplate.expire(LIKE_RANKING_KEY, 24, TimeUnit.HOURS); // TTL 설정
            }
        } catch (InterruptedException e) {
            log.error("좋아요 순 공연 랭킹 계산 중 락 획득에 실패했습니다.", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}