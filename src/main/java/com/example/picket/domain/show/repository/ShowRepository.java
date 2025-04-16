package com.example.picket.domain.show.repository;

import com.example.picket.common.enums.Category;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.querydsl.ShowQueryDslRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long>, ShowQueryDslRepository {

    List<Show> findAllByCategoryAndDeletedAtIsNull(Category category);

    @Modifying
    @Query("update Show s set s.viewCount = s.viewCount + 1 where s.id = :showId")
    void incrementViewCount(@Param("showId") Long showId);

}
