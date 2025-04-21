package com.example.picket.domain.ranking.controller;

import com.example.picket.domain.ranking.scheduler.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/test")
@RequiredArgsConstructor
public class RankingSchedulerTestController {

    private final PopularKeywordScheduler popularKeywordScheduler;
    private final HotShowRankingScheduler hotShowRankingScheduler;
    private final LikeRankingScheduler likeRankingScheduler;
    private final ShowStatusScheduler showStatusScheduler;
    private final TicketExpiryScheduler ticketExpiryScheduler; // Added dependency

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

    @PostMapping("/ticket-expiry")
    public ResponseEntity<Void> testTicketExpiry() {
        ticketExpiryScheduler.expireTicketsForFinishedShows();
        return ResponseEntity.ok().build();
    }
}