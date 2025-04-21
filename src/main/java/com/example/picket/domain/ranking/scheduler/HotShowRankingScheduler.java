package com.example.picket.domain.ranking.scheduler;

import com.example.picket.common.enums.ShowStatus;
import com.example.picket.domain.ranking.entity.HotShow;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.ShowRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
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
    private final ObjectMapper objectMapper;
    private static final String HOT_RANKING_KEY = "ranking:hot_shows";

    @Scheduled(cron = "0 0 * * * ?") // 매시간
    @SchedulerLock(name = "hot_show_ranking", lockAtMostFor = "PT5M", lockAtLeastFor = "PT10S")
    @Transactional(readOnly = true)
    public void updateHotShowRanking() {
        try {
            log.info("인기 공연 랭킹 업데이트 시작");
            List<Show> topShows = showRepository.findTop10ByStatusNotAndOrderByViewCountDesc(ShowStatus.FINISHED);
            log.debug("RDS에서 조회된 공연 데이터: {}", topShows);
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
        } catch (Exception e) {
            log.error("인기 공연 캐시 갱신 실패", e);
        }
    }
}
