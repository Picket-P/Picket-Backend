package com.example.picket.domain.ranking.scheduler;

import com.example.picket.common.enums.ShowStatus;
import com.example.picket.domain.ranking.entity.HotShow;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.ShowRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotShowRankingScheduler {

    private final StringRedisTemplate redisTemplate;
    private final ShowRepository showRepository;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private static final String HOT_RANKING_KEY = "ranking:hot_shows";

    @Scheduled(cron = "0 0 * * * ?") // 매시간
    @Transactional(readOnly = true)
    public void updateHotShowRanking() {
        RLock lock = redissonClient.getLock("lock:hot_show_ranking");
        try {
            if (lock.tryLock(0, 60, TimeUnit.SECONDS)) {
                log.info("인기 공연 랭킹 업데이트 시작");
                List<Show> topShows = showRepository.findTop10ByStatusNotAndOrderByViewCountDesc(ShowStatus.FINISHED);
                List<HotShow> hotShows = topShows.stream()
                        .map(show -> HotShow.toEntity(
                                show.getId(),
                                show.getTitle(),
                                show.getViewCount(),
                                show.getStatus(),
                                LocalDateTime.now()
                        ))
                        .toList();

                redisTemplate.delete(HOT_RANKING_KEY);
                for (HotShow hotShow : hotShows) {
                    String json = objectMapper.writeValueAsString(hotShow);
                    redisTemplate.opsForList().rightPush(HOT_RANKING_KEY, json);
                }
                redisTemplate.expire(HOT_RANKING_KEY, 1, TimeUnit.HOURS);
                log.info("Redis 인기 공연 캐시 갱신 완료: {}개", hotShows.size());
            } else {
                log.warn("인기 공연 랭킹 락 획득 실패, 스킵");
            }
        } catch (InterruptedException e) {
            log.error("인기 공연 랭킹 락 획득 실패", e);
        } catch (Exception e) {
            log.error("인기 공연 캐시 갱신 실패", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("인기 공연 랭킹 락 해제");
            }
        }
    }
}
