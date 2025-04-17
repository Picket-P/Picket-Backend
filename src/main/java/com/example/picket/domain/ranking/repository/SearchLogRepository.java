package com.example.picket.domain.ranking.repository;

import com.example.picket.domain.ranking.entity.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {
    @Query("SELECT s.category, COUNT(s) as count " +
            "FROM SearchLog s " +
            "WHERE s.searchedAt >= :since " +
            "GROUP BY s.category " +
            "ORDER BY count DESC " +
            "LIMIT 10")
    List<Object[]> findTopKeywordsSince(LocalDateTime since);
}