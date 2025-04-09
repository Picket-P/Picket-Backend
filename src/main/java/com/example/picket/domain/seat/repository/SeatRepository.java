package com.example.picket.domain.seat.repository;

import com.example.picket.domain.seat.entity.Seat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findAllByShowDateId(Long showDateId);

    @Query("SELECT s FROM Seat s JOIN FETCH s.showDate sd JOIN FETCH sd.show WHERE s.id = :seatId")
    Optional<Seat> findByIdWithShowDateAndShow(@Param("seatId") Long seatId);

}
