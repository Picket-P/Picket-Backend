package com.example.picket.domain.seat.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.repository.SeatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatQueryService {

    private final SeatRepository seatRepository;

    public Seat findById(Long id) {
        return seatRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.SEAT_NOT_FOUND));
    }

    public List<Seat> getSeatsByShowDate(Long showDateId) {
        return seatRepository.findAllByShowDateId(showDateId);
    }

    public List<Seat> findAllByShowDateId(Long id) {
        return seatRepository.findAllByShowDateId(id);
    }

}


