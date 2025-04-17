package com.example.picket.domain.show.repository;

import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.ShowStatus;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.querydsl.ShowQueryDslRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long>, ShowQueryDslRepository {

    List<Show> findAllByCategoryAndDeletedAtIsNull(Category category);

    @Query("SELECT s FROM Show s WHERE s.status != :status AND s.deletedAt IS NULL ORDER BY s.viewCount DESC")
    List<Show> findTop10ByStatusNotAndOrderByViewCountDesc(ShowStatus status);

    @Modifying
    @Query("UPDATE Show s SET s.viewCount = s.viewCount + 1 WHERE s.id = :showId AND s.deletedAt IS NULL")
    int incrementViewCount(@Param("showId") Long showId);
}
