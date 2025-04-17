package com.example.picket.domain.ranking.controller;

import com.example.picket.domain.ranking.scheduler.HotShowRankingScheduler;
import com.example.picket.domain.ranking.scheduler.LikeRankingScheduler;
import com.example.picket.domain.ranking.scheduler.PopularKeywordScheduler;
import com.example.picket.domain.ranking.scheduler.ShowStatusScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/test")
@RequiredArgsConstructor
public class TestController {

    private final PopularKeywordScheduler popularKeywordScheduler;
    private final HotShowRankingScheduler hotShowRankingScheduler;
    private final LikeRankingScheduler likeRankingScheduler;
    private final ShowStatusScheduler showStatusScheduler;

    @PostMapping("/popular-keywords")
    public ResponseEntity<Void> testPopularKeywords() {
        popularKeywordScheduler.updatePopularKeywords();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/hot-shows")
    public ResponseEntity<Void> testHotShows() {
        hotShowRankingScheduler.updateHotShowRanking();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/like-shows")
    public ResponseEntity<Void> testLikeShows() {
        likeRankingScheduler.updateLikeRanking();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/show-status")
    public ResponseEntity<Void> testShowStatus() {
        showStatusScheduler.updateShowStatuses();
        return ResponseEntity.ok().build();
    }
}
