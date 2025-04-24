package com.example.picket.domain.ranking.service;

import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.ShowStatus;
import com.example.picket.domain.like.repository.LikeRepository;
import com.example.picket.domain.ranking.dto.response.HotShowResponse;
import com.example.picket.domain.ranking.dto.response.LikeShowResponse;
import com.example.picket.domain.ranking.dto.response.PopularKeywordResponse;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final StringRedisTemplate redisTemplate;
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

    public List<PopularKeywordResponse> getPopularKeywords() {
        try {
            List<String> jsonKeywords = redisTemplate.opsForList().range("ranking:popular_keywords", 0, -1);
            if (jsonKeywords != null && !jsonKeywords.isEmpty()) {
                List<PopularKeywordResponse> keywords = jsonKeywords.stream()
                        .map(json -> {
                            try {
                                PopularKeyword keyword = objectMapper.readValue(json, PopularKeyword.class);
                                return PopularKeywordResponse.of(keyword);
                            } catch (Exception e) {
                                log.error("검색 키워드 역직렬화 실패: {}", json, e);
                                return null;
                            }
                        })
                        .filter(keyword -> keyword != null)
                        .toList();
                log.info("Redis에서 {}개 검색 키워드 조회 완료", keywords.size());
                return keywords;
            }

            log.warn("Redis 데이터 사용 불가, RDS로 폴백");
            List<Object[]> topKeywords = searchLogRepository.findTopKeywordsSince(LocalDateTime.now().minusDays(1));
            List<PopularKeywordResponse> fallbackKeywords = topKeywords.stream()
                    .map(row -> PopularKeyword.create(
                            (Category) row[0],
                            (Long) row[1],
                            LocalDateTime.now()
                    ))
                    .map(PopularKeywordResponse::of)
                    .toList();
            log.info("RDS에서 {}개 검색 키워드 조회 완료", fallbackKeywords.size());
            return fallbackKeywords;
        } catch (Exception e) {
            log.error("검색 키워드 조회 실패", e);
            return List.of();
        }
    }

    public List<HotShowResponse> getHotShows() {
        try {
            List<String> jsonShows = redisTemplate.opsForList().range("ranking:hot_shows", 0, -1);
            if (jsonShows != null && !jsonShows.isEmpty()) {
                List<HotShowResponse> shows = jsonShows.stream()
                        .map(json -> {
                            try {
                                HotShow show = objectMapper.readValue(json, HotShow.class);
                                return HotShowResponse.of(show);
                            } catch (Exception e) {
                                log.error("공연 역직렬화 실패: {}", json, e);
                                return null;
                            }
                        })
                        .filter(show -> show != null)
                        .toList();
                log.info("Redis에서 {}개 인기 공연 조회 완료", shows.size());
                return shows;
            }

            log.warn("Redis 데이터 사용 불가, RDS로 폴백");
            List<Show> topShows = showRepository.findTop10ByStatusNotAndOrderByViewCountDesc(ShowStatus.FINISHED);
            log.debug("RDS에서 조회된 공연 데이터: {}", topShows);
            if (topShows.isEmpty()) {
                log.warn("RDS에서 조건에 맞는 공연 데이터가 없습니다. 상태: NOT FINISHED, 정렬: viewCount DESC");
            }
            List<HotShowResponse> fallbackShows = topShows.stream()
                    .map(show -> HotShow.create(
                            show.getId(),
                            show.getTitle(),
                            show.getViewCount(),
                            show.getStatus(),
                            LocalDateTime.now()
                    ))
                    .map(HotShowResponse::of)
                    .toList();
            log.info("RDS에서 {}개 인기 공연 조회 완료", fallbackShows.size());
            return fallbackShows;
        } catch (Exception e) {
            log.error("인기 공연 조회 실패", e);
            return List.of();
        }
    }

    public List<LikeShowResponse> getLikeShows() {
        try {
            List<String> jsonShows = redisTemplate.opsForList().range("ranking:like_shows", 0, -1);
            if (jsonShows != null && !jsonShows.isEmpty()) {
                List<LikeShowResponse> shows = jsonShows.stream()
                        .map(json -> {
                            try {
                                LikeShow show = objectMapper.readValue(json, LikeShow.class);
                                return LikeShowResponse.of(show);
                            } catch (Exception e) {
                                log.error("공연 역직렬화 실패: {}", json, e);
                                return null;
                            }
                        })
                        .filter(show -> show != null)
                        .toList();
                log.info("Redis에서 {}개 좋아요 공연 조회 완료", shows.size());
                return shows;
            }

            log.warn("Redis 데이터 사용 불가, RDS로 폴백");
            Pageable pageable = PageRequest.of(0, 10);
            List<Object[]> topShows = likeRepository.findTop10ShowsByLikeCountAndStatusIn(ACTIVE_STATUSES, pageable);
            log.debug("RDS 좋아요 공연 쿼리 결과: {}", topShows);
            List<LikeShowResponse> fallbackShows = topShows.stream()
                    .map(row -> LikeShow.create(
                            (Long) row[0],
                            (String) row[1],
                            (Long) row[2],
                            (ShowStatus) row[3],
                            LocalDateTime.now()
                    ))
                    .map(LikeShowResponse::of)
                    .toList();
            log.info("RDS에서 {}개 좋아요 공연 조회 완료", fallbackShows.size());
            return fallbackShows;
        } catch (Exception e) {
            log.error("좋아요 공연 조회 실패", e);
            return List.of();
        }
    }
}