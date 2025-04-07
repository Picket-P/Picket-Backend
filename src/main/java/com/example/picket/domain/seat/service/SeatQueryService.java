package com.example.picket.domain.seat.service;

import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatQueryService {

    private final SeatRepository seatRepository;

    public List<Seat> getSeatsByShowDate(Long showDateId) {
        return seatRepository.findAllByShowDateId(showDateId);
    }
}


