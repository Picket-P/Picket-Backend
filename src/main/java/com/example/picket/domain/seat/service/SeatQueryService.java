package com.example.picket.domain.seat.service;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.repository.SeatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatQueryService {

    private final SeatRepository seatRepository;

    public Seat getSeat(Long id) {
        return seatRepository.findByIdWithShowDateAndShow(id).orElseThrow(
                () -> new CustomException(NOT_FOUND, "존재하지 않는 Seat입니다."));
    }

    public List<Seat> getSeatsByShowDate(Long showDateId) {
        return seatRepository.findAllByShowDateId(showDateId);
    }

    public Seat getSeatForBooking(Long id) {
        return seatRepository.findById(id).orElseThrow(
                () -> new CustomException(NOT_FOUND, "존재하지 않는 Seat입니다."));
    }

}


