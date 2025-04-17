package com.example.picket.domain.ranking.scheduler;

import com.example.picket.common.enums.Category;
import com.example.picket.domain.ranking.entity.PopularKeyword;
import com.example.picket.domain.ranking.entity.SearchLog;
import com.example.picket.domain.ranking.repository.SearchLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularKeywordScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    private final SearchLogRepository searchLogRepository;
    private final ObjectMapper objectMapper;
    private static final String SEARCH_KEYWORD_KEY = "search:keywords";
    private static final String POPULAR_KEYWORD_RANKING_KEY = "ranking:popular_keywords";

    // 카테고리 검색 시 호출
    @Transactional
    public void incrementSearchKeyword(Category category) {
        if (category != null) {
            log.info("검색 키워드 증가: {}", category.name());
            redisTemplate.opsForZSet().incrementScore(SEARCH_KEYWORD_KEY, category.name(), 1);
            searchLogRepository.save(SearchLog.toEntity(category));
        } else {
            log.warn("카테고리가 null입니다.");
        }
    }

    @Scheduled(cron = "0 0 * * * ?") // 매시간마다
    @Transactional(readOnly = true)
    public void updatePopularKeywords() {
        RLock lock = redissonClient.getLock("lock:popular_keywords");
        try {
            if (lock.tryLock(0, 60, TimeUnit.SECONDS)) {
                log.info("검색 키워드 랭킹 업데이트 시작");
                LocalDateTime now = LocalDateTime.now();
                Set<ZSetOperations.TypedTuple<String>> topKeywords = redisTemplate.opsForZSet()
                        .reverseRangeWithScores(SEARCH_KEYWORD_KEY, 0, 9);

                List<PopularKeyword> keywordList = topKeywords.stream()
                        .map(tuple -> PopularKeyword.toEntity(
                                Category.valueOf(tuple.getValue()),
                                tuple.getScore().longValue(),
                                now
                        ))
                        .toList();

                List<String> jsonKeywords = keywordList.stream()
                        .map(keyword -> {
                            try {
                                return objectMapper.writeValueAsString(keyword);
                            } catch (Exception e) {
                                log.error("검색 키워드 직렬화 실패: {}", keyword, e);
                                return null;
                            }
                        })
                        .filter(str -> str != null)
                        .toList();

                redisTemplate.delete(POPULAR_KEYWORD_RANKING_KEY);
                if (!jsonKeywords.isEmpty()) {
                    redisTemplate.opsForList().rightPushAll(POPULAR_KEYWORD_RANKING_KEY, jsonKeywords);
                    redisTemplate.opsForList().trim(POPULAR_KEYWORD_RANKING_KEY, -10, -1);
                    redisTemplate.expire(POPULAR_KEYWORD_RANKING_KEY, 1, TimeUnit.HOURS);
                    log.info("Redis 검색 키워드 캐시 갱신 완료: {}개", jsonKeywords.size());
                } else {
                    log.warn("저장할 검색 키워드 없음");
                }
            } else {
                log.warn("검색 키워드 랭킹 락 획득 실패, 스킵");
            }
        } catch (InterruptedException e) {
            log.error("검색 키워드 랭킹 락 획득 실패", e);
        } catch (Exception e) {
            log.error("검색 키워드 캐시 갱신 실패", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("검색 키워드 랭킹 락 해제");
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 초기화
    public void resetSearchKeywords() {
        RLock lock = redissonClient.getLock("lock:reset_keywords");
        try {
            if (lock.tryLock(0, 60, TimeUnit.SECONDS)) {
                log.info("검색 키워드 초기화 시작");
                redisTemplate.delete(SEARCH_KEYWORD_KEY);
                log.info("검색 키워드 초기화 완료");
            } else {
                log.warn("검색 키워드 초기화 락 획득 실패, 스킵");
            }
        } catch (InterruptedException e) {
            log.error("검색 키워드 초기화 락 획득 실패", e);
        } catch (Exception e) {
            log.error("검색 키워드 초기화 실패", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("검색 키워드 초기화 락 해제");
            }
        }
    }
}