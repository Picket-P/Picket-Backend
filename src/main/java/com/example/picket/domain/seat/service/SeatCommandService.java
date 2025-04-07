package com.example.picket.domain.seat.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Grade;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.seat.dto.SeatUpdateRequest;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.repository.SeatRepository;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import com.example.picket.domain.show.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatCommandService {

    private final SeatRepository seatRepository;
    private final ShowDateRepository showDateRepository;

    // 좌석 수정
    @Transactional
    public void updateSeats(AuthUser authUser, Long showDateId, List<SeatUpdateRequest> requests) {

        ShowDate showDate = showDateRepository.findById(showDateId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "공연 날짜를 찾을 수 없습니다."));

        Map<Grade, List<Seat>> existingSeatsByGrade = seatRepository.findAllByShowDateId(showDateId).stream()
                .collect(Collectors.groupingBy(Seat::getGrade));

        for (SeatUpdateRequest request : requests) {
            Grade grade = Grade.valueOf(request.getGrade());
            List<Seat> currentSeats = existingSeatsByGrade.getOrDefault(grade, new ArrayList<>());
            int currentCount = currentSeats.size();

            if (currentCount < request.getQuantity()) {
                int toAdd = request.getQuantity() - currentCount;
                for (int i = 1; i <= toAdd; i++) {
                    Seat seat = Seat.of(grade, currentCount + i, request.getPrice(), showDate);
                    seatRepository.save(seat);
                }
            } else if (currentCount > request.getQuantity()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "요청 수량보다 기존 좌석이 많습니다. 좌석 감소는 삭제 API를 통해 처리해주세요.");
            }

            // 가격 일괄 업데이트
            currentSeats.forEach(seat -> seat.updatePrice(request.getPrice()));
        }
    }
}
