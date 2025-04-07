package com.example.picket.domain.seat.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Grade;
import com.example.picket.common.enums.SeatStatus;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.seat.dto.SeatUpdateRequest;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.repository.SeatRepository;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import com.example.picket.domain.show.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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

        Show show = showDate.getShow();
        boolean isReservationStarted = show.getReservationStart().isBefore(LocalDateTime.now());

        Map<Grade, List<Seat>> existingSeatsByGrade = seatRepository.findAllByShowDateId(showDateId).stream()
                .collect(Collectors.groupingBy(Seat::getGrade));

        for (SeatUpdateRequest request : requests) {
            Grade grade = Grade.valueOf(request.getGrade());
            List<Seat> currentSeats = existingSeatsByGrade.getOrDefault(grade, new ArrayList<>());
            int currentCount = currentSeats.size();
            int requestedCount = request.getQuantity();

            if (requestedCount < currentCount) {
                if (isReservationStarted) {
                    throw new CustomException(HttpStatus.BAD_REQUEST, "예매 시작 이후에는 좌석 수 감소가 불가능합니다. 삭제 API를 사용해주세요.");
                }

                // 예매 시작 전 → 좌석 감소 허용
                int toRemove = currentCount - requestedCount;
                List<Seat> removable = currentSeats.stream()
                        .filter(seat -> seat.getSeatStatus() == SeatStatus.AVAILABLE)
                        .sorted(Comparator.comparingInt(Seat::getSeatNumber).reversed()) // 높은 번호부터 제거
                        .limit(toRemove)
                        .toList();

                if (removable.size() < toRemove) {
                    throw new CustomException(HttpStatus.BAD_REQUEST, "예약된 좌석이 있어 일부 좌석을 줄일 수 없습니다.");
                }

                seatRepository.deleteAll(removable);
            }

            if (requestedCount > currentCount) {
                int toAdd = requestedCount - currentCount;
                for (int i = 1; i <= toAdd; i++) {
                    Seat seat = Seat.of(grade, currentCount + i, request.getPrice(), showDate);
                    seatRepository.save(seat);
                }
            }

            // 기존 좌석 가격 일괄 업데이트
            for (Seat seat : currentSeats) {
                seat.updatePrice(request.getPrice());
            }
        }
    }
}
