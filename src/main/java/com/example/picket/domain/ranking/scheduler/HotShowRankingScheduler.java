package com.example.picket.domain.ranking.scheduler;

import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.ShowRepository;
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
public class HotShowRankingScheduler {

    private final ShowRepository showRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    private static final String HOT_RANKING_KEY = "ranking:hot_shows";

    @Scheduled(cron = "0 0/30 * * * ?") // 30분마다
    @Transactional(readOnly = true)
    public void updateHotShowRanking() {
        RLock lock = redissonClient.getLock("lock:hot_show_ranking");
        try {
            if (lock.tryLock(0, 60, TimeUnit.SECONDS)) {
                List<Show> topShows = showRepository.findTop10ByOrderByViewCountDesc();
                redisTemplate.delete(HOT_RANKING_KEY);
                topShows.forEach(show -> redisTemplate.opsForZSet().add(
                        HOT_RANKING_KEY,
                        show.getId() + ":" + show.getTitle(),
                        show.getViewCount()
                ));
                redisTemplate.expire(HOT_RANKING_KEY, 24, TimeUnit.HOURS); // TTL 설정
            }
        } catch (InterruptedException e) {
            log.error("HOT 쇼 랭킹을 위한 락 획득에 실패했습니다.", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
