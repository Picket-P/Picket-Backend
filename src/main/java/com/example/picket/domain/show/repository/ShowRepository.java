package com.example.picket.domain.show.repository;

import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.ShowStatus;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.querydsl.ShowQueryDslRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long>, ShowQueryDslRepository {

    List<Show> findAllByCategoryAndDeletedAtIsNull(Category category);

    @Query("SELECT s FROM Show s WHERE s.status != :status ORDER BY s.viewCount DESC")
    List<Show> findTop10ByStatusNotAndOrderByViewCountDesc(@Param("status") ShowStatus status, Pageable pageable);

    default List<Show> findTop10ByStatusNotAndOrderByViewCountDesc(ShowStatus status) {
        return findTop10ByStatusNotAndOrderByViewCountDesc(status, PageRequest.of(0, 10));
    }
}
