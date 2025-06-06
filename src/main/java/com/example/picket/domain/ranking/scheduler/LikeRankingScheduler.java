package com.example.picket.domain.ranking.scheduler;

import com.example.picket.common.enums.ShowStatus;
import com.example.picket.domain.like.repository.LikeRepository;
import com.example.picket.domain.ranking.entity.LikeShow;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeRankingScheduler {

    private final StringRedisTemplate redisTemplate;
    private final LikeRepository likeRepository;
    private final ObjectMapper objectMapper;
    private static final String LIKE_SHOW_RANKING_KEY = "ranking:like_shows";
    private static final ShowStatus[] ACTIVE_STATUSES = {
            ShowStatus.RESERVATION_PENDING,
            ShowStatus.RESERVATION_ONGOING,
            ShowStatus.RESERVATION_CLOSED,
            ShowStatus.PERFORMANCE_ONGOING
    };

    @Scheduled(cron = "0 0 * * * ?") // 매시간
    @SchedulerLock(name = "like_show_ranking", lockAtMostFor = "PT5M", lockAtLeastFor = "PT10S")
    @Transactional
    public void updateLikeRanking() {
        try {
            log.info("좋아요 공연 랭킹 업데이트 시작");
            Pageable pageable = PageRequest.of(0, 10);
            List<Object[]> topShows = likeRepository.findTop10ShowsByLikeCountAndStatusIn(ACTIVE_STATUSES, pageable);
            log.info("상위 좋아요 공연 {}개 조회", topShows.size());
            log.debug("좋아요 공연 쿼리 결과: {}, 상태 필터: {}", topShows, Arrays.toString(ACTIVE_STATUSES));

            List<LikeShow> showList = topShows.stream()
                    .map(row -> LikeShow.create(
                            (Long) row[0],
                            (String) row[1],
                            (Long) row[2],
                            (ShowStatus) row[3],
                            LocalDateTime.now()
                    ))
                    .toList();

            List<String> jsonShows = showList.stream()
                    .map(show -> {
                        try {
                            String json = objectMapper.writeValueAsString(show);
                            log.debug("직렬화된 좋아요 공연 JSON: {}", json);
                            return json;
                        } catch (Exception e) {
                            log.error("좋아요 공연 직렬화 실패: {}", show, e);
                            return null;
                        }
                    })
                    .filter(str -> str != null)
                    .toList();

            redisTemplate.delete(LIKE_SHOW_RANKING_KEY);
            log.debug("Redis 키 삭제: {}", LIKE_SHOW_RANKING_KEY);
            if (!jsonShows.isEmpty()) {
                Long added = redisTemplate.opsForList().rightPushAll(LIKE_SHOW_RANKING_KEY, jsonShows);
                log.debug("Redis에 추가된 항목 수: {}", added);
                redisTemplate.opsForList().trim(LIKE_SHOW_RANKING_KEY, -10, -1);
                boolean expireSet = redisTemplate.expire(LIKE_SHOW_RANKING_KEY, 1, TimeUnit.HOURS);
                log.debug("TTL 설정 성공: {}", expireSet);
                List<String> verify = redisTemplate.opsForList().range(LIKE_SHOW_RANKING_KEY, 0, -1);
                log.debug("Redis 저장 데이터: {}", verify);
                log.info("Redis 좋아요 공연 캐시 갱신 완료: {}개", jsonShows.size());
            } else {
                log.warn("저장할 좋아요 공연 없음: 쿼리 결과가 비어있거나 활성 공연 없음");
            }
        } catch (Exception e) {
            log.error("좋아요 공연 캐시 갱신 실패", e);
        }
    }
}