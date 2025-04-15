package com.example.picket.domain.ranking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RedisTemplate<String, String> redisTemplate;

    @GetMapping("/popular-keywords")
    public ResponseEntity<List<String>> getPopularKeywords() {
        List<String> keywords = redisTemplate.opsForList().range("ranking:popular_keywords", 0, -1);
        return ResponseEntity.ok(keywords != null ? keywords : Collections.emptyList());
    }

    @GetMapping("/hot-shows")
    public ResponseEntity<List<String>> getHotShows() {
        Set<String> shows = redisTemplate.opsForZSet().reverseRange("ranking:hot_shows", 0, 9);
        return ResponseEntity.ok(new ArrayList<>(shows != null ? shows : Collections.emptySet()));
    }

    @GetMapping("/like-shows")
    public ResponseEntity<List<String>> getLikeShows() {
        Set<String> shows = redisTemplate.opsForZSet().reverseRange("ranking:like_shows", 0, 9);
        return ResponseEntity.ok(new ArrayList<>(shows != null ? shows : Collections.emptySet()));
    }
}
