package com.example.picket.domain.seat.service;

import com.example.picket.common.enums.Grade;
import com.example.picket.domain.seat.dto.SeatGroupByGradeResponse;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatQueryService {

    private final SeatRepository seatRepository;

    public List<SeatGroupByGradeResponse> getSeatsByShowDate(Long showDateId) {

        List<Seat> seats = seatRepository.findAllByShowDateId(showDateId);

        return seats.stream()
                .collect(Collectors.groupingBy(seat -> seat.getGrade()))
                .entrySet()
                .stream()
                .map(entry -> {
                    Grade grade = entry.getKey();
                    List<Seat> groupedSeats = entry.getValue();
                    BigDecimal price = groupedSeats.get(0).getPrice(); // 등급별 가격 동일하니까 첫 번째 꺼
                    return SeatGroupByGradeResponse.from(grade, price, groupedSeats);
                })
                .toList();
    }
}

