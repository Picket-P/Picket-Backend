package com.example.picket.domain.seat.repository;

import com.example.picket.domain.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findAllByShowDateId(Long showDateId);
}
