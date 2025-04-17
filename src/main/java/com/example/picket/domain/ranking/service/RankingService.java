package com.example.picket.domain.ranking.service;

import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.ShowStatus;
import com.example.picket.domain.like.repository.LikeRepository;
import com.example.picket.domain.ranking.entity.HotShow;
import com.example.picket.domain.ranking.entity.LikeShow;
import com.example.picket.domain.ranking.entity.PopularKeyword;
import com.example.picket.domain.ranking.repository.SearchLogRepository;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.ShowRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SearchLogRepository searchLogRepository;
    private final ShowRepository showRepository;
    private final LikeRepository likeRepository;
    private final ObjectMapper objectMapper;
    private static final ShowStatus[] ACTIVE_STATUSES = {
            ShowStatus.RESERVATION_PENDING,
            ShowStatus.RESERVATION_ONGOING,
            ShowStatus.RESERVATION_CLOSED,
            ShowStatus.PERFORMANCE_ONGOING
    };

    @Async
    public CompletableFuture<List<PopularKeyword>> getPopularKeywordsAsync() {
        try {
            List<String> jsonKeywords = redisTemplate.opsForList().range("ranking:popular_keywords", 0, -1);
            if (jsonKeywords != null && !jsonKeywords.isEmpty()) {
                List<PopularKeyword> keywords = jsonKeywords.stream()
                        .map(json -> {
                            try {
                                return objectMapper.readValue(json, PopularKeyword.class);
                            } catch (Exception e) {
                                log.error("검색 키워드 역직렬화 실패: {}", json, e);
                                return null;
                            }
                        })
                        .filter(keyword -> keyword != null)
                        .toList();
                log.info("Redis에서 {}개 검색 키워드 조회 완료", keywords.size());
                return CompletableFuture.completedFuture(keywords);
            }

            log.warn("Redis 데이터 사용 불가, RDS로 폴백");
            List<Object[]> topKeywords = searchLogRepository.findTopKeywordsSince(LocalDateTime.now().minusDays(1));
            List<PopularKeyword> fallbackKeywords = topKeywords.stream()
                    .map(row -> PopularKeyword.toEntity(
                            (Category) row[0],
                            (Long) row[1],
                            LocalDateTime.now()
                    ))
                    .toList();
            log.info("RDS에서 {}개 검색 키워드 조회 완료", fallbackKeywords.size());
            return CompletableFuture.completedFuture(fallbackKeywords);
        } catch (Exception e) {
            log.error("검색 키워드 조회 실패", e);
            return CompletableFuture.completedFuture(List.of());
        }
    }

    @Async
    public CompletableFuture<List<HotShow>> getHotShowsAsync() {
        try {
            List<String> jsonShows = redisTemplate.opsForList().range("ranking:hot_shows", 0, -1);
            if (jsonShows != null && !jsonShows.isEmpty()) {
                List<HotShow> shows = jsonShows.stream()
                        .map(json -> {
                            try {
                                return objectMapper.readValue(json, HotShow.class);
                            } catch (Exception e) {
                                log.error("공연 역직렬화 실패: {}", json, e);
                                return null;
                            }
                        })
                        .filter(show -> show != null)
                        .toList();
                log.info("Redis에서 {}개 인기 공연 조회 완료", shows.size());
                return CompletableFuture.completedFuture(shows);
            }

            log.warn("Redis 데이터 사용 불가, RDS로 폴백");
            List<Show> topShows = showRepository.findTop10ByStatusNotAndOrderByViewCountDesc(ShowStatus.FINISHED);
            List<HotShow> fallbackShows = topShows.stream()
                    .map(show -> HotShow.toEntity(
                            show.getId(),
                            show.getTitle(),
                            show.getViewCount(),
                            show.getStatus(),
                            LocalDateTime.now()
                    ))
                    .toList();
            log.info("RDS에서 {}개 인기 공연 조회 완료", fallbackShows.size());
            return CompletableFuture.completedFuture(fallbackShows);
        } catch (Exception e) {
            log.error("인기 공연 조회 실패", e);
            return CompletableFuture.completedFuture(List.of());
        }
    }

    @Async
    public CompletableFuture<List<LikeShow>> getLikeShowsAsync() {
        try {
            List<String> jsonShows = redisTemplate.opsForList().range("ranking:like_shows", 0, -1);
            if (jsonShows != null && !jsonShows.isEmpty()) {
                List<LikeShow> shows = jsonShows.stream()
                        .map(json -> {
                            try {
                                return objectMapper.readValue(json, LikeShow.class);
                            } catch (Exception e) {
                                log.error("공연 역직렬화 실패: {}", json, e);
                                return null;
                            }
                        })
                        .filter(show -> show != null)
                        .toList();
                log.info("Redis에서 {}개 좋아요 공연 조회 완료", shows.size());
                return CompletableFuture.completedFuture(shows);
            }

            log.warn("Redis 데이터 사용 불가, RDS로 폴백");
            Pageable pageable = PageRequest.of(0, 10);
            List<Object[]> topShows = likeRepository.findTop10ShowsByLikeCountAndStatusIn(ACTIVE_STATUSES, pageable);
            log.debug("RDS 좋아요 공연 쿼리 결과: {}", topShows);
            List<LikeShow> fallbackShows = topShows.stream()
                    .map(row -> LikeShow.toEntity(
                            (Long) row[0],
                            (String) row[1],
                            (Long) row[2],
                            (ShowStatus) row[3],
                            LocalDateTime.now()
                    ))
                    .toList();
            log.info("RDS에서 {}개 좋아요 공연 조회 완료", fallbackShows.size());
            return CompletableFuture.completedFuture(fallbackShows);
        } catch (Exception e) {
            log.error("좋아요 공연 조회 실패", e);
            return CompletableFuture.completedFuture(List.of());
        }
    }
}