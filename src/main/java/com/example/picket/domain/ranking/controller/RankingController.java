package com.example.picket.domain.ranking.controller;

import com.example.picket.domain.ranking.dto.response.HotShowResponse;
import com.example.picket.domain.ranking.dto.response.LikeShowResponse;
import com.example.picket.domain.ranking.dto.response.PopularKeywordResponse;
import com.example.picket.domain.ranking.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v2/rankings")
@RequiredArgsConstructor
@Slf4j
public class RankingController {

    private final RankingService rankingService;

    @Operation(summary = "인기 검색어 랭킹", description = "인기 검색어 랭킹을 조회할 수 있습니다.")
    @GetMapping("/popular-keywords")
    public ResponseEntity<List<PopularKeywordResponse>> getPopularKeywords() {
        log.info("인기 키워드 조회 요청 수신");
        List<PopularKeywordResponse> keywords = rankingService.getPopularKeywords();
        log.info("{}개 키워드 반환", keywords.size());
        return ResponseEntity.ok(keywords);
    }

    @Operation(summary = "조회수 랭킹", description = "조회수 랭킹을 조회할 수 있습니다.")
    @GetMapping("/hot-shows")
    public ResponseEntity<List<HotShowResponse>> getHotShows() {
        log.info("인기 공연 조회 요청 수신");
        List<HotShowResponse> shows = rankingService.getHotShows();
        log.info("{}개 인기 공연 반환", shows.size());
        return ResponseEntity.ok(shows);
    }

    @Operation(summary = "좋아요 랭킹", description = "좋아요 랭킹을 조회할 수 있습니다.")
    @GetMapping("/like-shows")
    public ResponseEntity<List<LikeShowResponse>> getLikeShows() {
        log.info("좋아요 공연 조회 요청 수신");
        List<LikeShowResponse> shows = rankingService.getLikeShows();
        log.info("{}개 좋아요 공연 반환", shows.size());
        return ResponseEntity.ok(shows);
    }
}