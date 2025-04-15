package com.example.picket.domain.ranking.controller;

import com.example.picket.domain.ranking.scheduler.HotShowRankingScheduler;
import com.example.picket.domain.ranking.scheduler.LikeRankingScheduler;
import com.example.picket.domain.ranking.scheduler.PopularKeywordScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/test")
public class TestController {

    private final PopularKeywordScheduler popularKeywordScheduler;
    private final HotShowRankingScheduler hotShowRankingScheduler;
    private final LikeRankingScheduler likeRankingScheduler;
//    private final TicketStatusScheduler ticketStatusScheduler;

    @PostMapping("/popular-keywords")
    public void testPopularKeywords() {
        popularKeywordScheduler.updatePopularKeywords();
    }

    @PostMapping("/hot-shows")
    public void testHotShows() {
        hotShowRankingScheduler.updateHotShowRanking();
    }

    @PostMapping("/like-shows")
    public void testLikeShows() {
        likeRankingScheduler.updateLikeRanking();
    }

//    @PostMapping("/ticket-status")
//    public void testTicketStatus() {
//        ticketStatusScheduler.updateShowStatuses();
//    }
}
