package com.example.picket.domain.show.repository;

import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.querydsl.ShowDateQueryDslRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShowDateRepository extends JpaRepository<ShowDate, Long>, ShowDateQueryDslRepository {

    @EntityGraph(attributePaths = {"show"}) // Show와 Seat 관련된 데이터를 fetch join으로 미리 로드
    List<ShowDate> findAllByShowId(Long showId);

    Optional<ShowDate> findShowDateByShow(Show show);

}
