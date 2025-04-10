package com.example.picket.domain.seat.repository.jdbc;

import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.show.entity.ShowDate;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class SeatJdbcRepositoryImpl implements SeatJdbcRepository {

    private final String INSERT_SQL = """
        INSERT INTO seats (show_date_id, grade, seat_number, price, seat_status, created_at, modified_at)
        VALUES(?, ?, ?, ?, ?, ?, ?)
        """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveAllJdbc(List<Seat> seats) {
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.batchUpdate(INSERT_SQL, seats, 1000, (PreparedStatement ps, Seat seat) -> {
            ps.setLong(1, seat.getShowDate().getId());
            ps.setString(2, seat.getGrade().name());
            ps.setInt(3, seat.getSeatNumber());
            ps.setBigDecimal(4, seat.getPrice());
            ps.setString(5, seat.getSeatStatus().name());
            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.setTimestamp(7, Timestamp.valueOf(now));
        });
    }
}
