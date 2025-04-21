package com.example.picket.domain.ranking.controller;

import com.example.picket.domain.ranking.dto.response.HotShowResponse;
import com.example.picket.domain.ranking.dto.response.LikeShowResponse;
import com.example.picket.domain.ranking.dto.response.PopularKeywordResponse;
import com.example.picket.domain.ranking.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v2/rankings")
@RequiredArgsConstructor
@Slf4j
public class RankingController {

    private final RankingService rankingService;
    private final StringRedisTemplate redisTemplate;

    @Operation(summary = "인기 검색어 랭킹", description = "인기 검색어 랭킹을 조회할 수 있습니다.")
    @GetMapping("/popular-keywords")
    public CompletableFuture<ResponseEntity<List<PopularKeywordResponse>>> getPopularKeywords() {
        log.info("인기 키워드 조회 요청 수신");
        return rankingService.getPopularKeywordsAsync()
                .thenApply(keywords -> {
                    List<PopularKeywordResponse> responses = keywords.stream()
                            .map(PopularKeywordResponse::toDto)
                            .toList();
                    log.info("{}개 키워드 반환", responses.size());
                    return ResponseEntity.ok(responses);
                });
    }

    @Operation(summary = "조회수 랭킹", description = "조회수 랭킹을 조회할 수 있습니다.")
    @GetMapping("/hot-shows")
    public CompletableFuture<ResponseEntity<List<HotShowResponse>>> getHotShows() {
        log.info("인기 공연 조회 요청 수신");
        return rankingService.getHotShowsAsync()
                .thenApply(shows -> {
                    List<HotShowResponse> responses = shows.stream()
                            .map(HotShowResponse::toDto)
                            .toList();
                    log.info("{}개 인기 공연 반환", responses.size());
                    return ResponseEntity.ok(responses);
                });
    }

    @Operation(summary = "좋아요 랭킹", description = "좋아요 랭킹을 조회할 수 있습니다.")
    @GetMapping("/like-shows")
    public CompletableFuture<ResponseEntity<List<LikeShowResponse>>> getLikeShows() {
        log.info("좋아요 공연 조회 요청 수신");
        return rankingService.getLikeShowsAsync()
                .thenApply(shows -> {
                    List<LikeShowResponse> responses = shows.stream()
                            .map(LikeShowResponse::toDto)
                            .toList();
                    log.info("{}개 좋아요 공연 반환", responses.size());
                    return ResponseEntity.ok(responses);
                });
    }
}
