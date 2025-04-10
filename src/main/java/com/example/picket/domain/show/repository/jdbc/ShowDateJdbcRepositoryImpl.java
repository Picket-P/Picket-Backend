package com.example.picket.domain.show.repository.jdbc;

import com.example.picket.domain.show.entity.ShowDate;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class ShowDateJdbcRepositoryImpl implements ShowDateJdbcRepository {

    private final String INSERT_SQL = """
        INSERT INTO show_dates (show_id, date, start_time, end_time, total_seat_count, reserved_seat_count, available_seat_count, created_at, modified_at)
        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveAllJdbc(List<ShowDate> showDates) {
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.batchUpdate(INSERT_SQL, showDates, 1000, (PreparedStatement ps, ShowDate showDate) -> {
            ps.setLong(1, showDate.getShow().getId());
            ps.setDate(2, Date.valueOf(showDate.getDate()));
            ps.setTime(3, Time.valueOf(showDate.getStartTime()));
            ps.setTime(4, Time.valueOf(showDate.getEndTime()));
            ps.setInt(5, showDate.getTotalSeatCount());
            ps.setInt(6, showDate.getReservedSeatCount());
            ps.setInt(7, showDate.getTotalSeatCount() - showDate.getReservedSeatCount());
            ps.setTimestamp(8, Timestamp.valueOf(now));
            ps.setTimestamp(9, Timestamp.valueOf(now));
        });
    }

}
