package com.example.picket.domain.show.repository;

import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface ShowDateRepository extends JpaRepository<ShowDate, Long> {

    List<ShowDate> findAllByShowId(Long showId);
    Optional<ShowDate> findByShow(Show show);
}
