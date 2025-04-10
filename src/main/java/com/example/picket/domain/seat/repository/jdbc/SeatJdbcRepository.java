package com.example.picket.domain.seat.repository.jdbc;

import com.example.picket.domain.seat.entity.Seat;

import java.util.List;

public interface SeatJdbcRepository {

    void saveAllJdbc(List<Seat> seats);

}
