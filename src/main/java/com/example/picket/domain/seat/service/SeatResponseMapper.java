package com.example.picket.domain.seat.service;

import com.example.picket.common.enums.Grade;
import com.example.picket.domain.seat.dto.response.SeatGroupByGradeResponse;
import com.example.picket.domain.seat.entity.Seat;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SeatResponseMapper {

    public List<SeatGroupByGradeResponse> toGroupByGradeResponses(List<Seat> seats) {
        return seats.stream()
                .collect(Collectors.groupingBy(Seat::getGrade))
                .entrySet()
                .stream()
                .map(entry -> {
                    Grade grade = entry.getKey();
                    List<Seat> groupedSeats = entry.getValue();
                    BigDecimal price = groupedSeats.get(0).getPrice(); // 등급별 가격 동일
                    return SeatGroupByGradeResponse.of(grade, price, groupedSeats);
                })
                .toList();
    }
}

