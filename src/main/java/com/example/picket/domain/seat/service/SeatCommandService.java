package com.example.picket.domain.seat.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Grade;
import com.example.picket.common.enums.SeatStatus;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.seat.dto.request.SeatUpdateRequest;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.repository.SeatRepository;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.service.ShowDateQueryService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatCommandService {

    private final SeatRepository seatRepository;
    private final ShowDateQueryService showDateQueryService;

    // 좌석 수정
    @Transactional
    public List<Seat> updateSeats(AuthUser authUser, Long showDateId, List<SeatUpdateRequest> requests) {
        ShowDate showDate = showDateQueryService.getShowDate(showDateId);

        Show show = showDate.getShow();
        boolean isReservationStarted = show.getReservationStart().isBefore(LocalDateTime.now());

        // 기존 좌석들을 Grade별로 그룹화하여 가져오기
        Map<Grade, List<Seat>> existingSeatsByGrade = seatRepository.findAllByShowDateId(showDateId).stream()
                .collect(Collectors.groupingBy(Seat::getGrade));

        List<Seat> result = new ArrayList<>();

        // 각 요청에 대해 처리
        for (SeatUpdateRequest request : requests) {
            Grade grade = Grade.valueOf(request.getGrade());
            List<Seat> currentSeats = existingSeatsByGrade.getOrDefault(grade, new ArrayList<>());
            int requestedCount = request.getQuantity();

            // 좌석 수 변경 처리
            handleSeatCountChange(grade, currentSeats, requestedCount, isReservationStarted, showDate,
                    request.getPrice(), result);

            // 좌석 가격 변경
            updateSeatPrices(currentSeats, request.getPrice());
        }

        return result;
    }

    // 좌석 단건 삭제
    @Transactional
    public void deleteSeat(AuthUser authUser, Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "좌석을 찾을 수 없습니다."));

        if (seat.getSeatStatus() == SeatStatus.RESERVED) {
            throw new CustomException(HttpStatus.FORBIDDEN, "이미 예약된 좌석은 삭제할 수 없습니다.");
        }

        seatRepository.delete(seat);
    }

    // 좌석 수 변경 처리
    private void handleSeatCountChange(Grade grade, List<Seat> currentSeats, int requestedCount,
                                       boolean isReservationStarted, ShowDate showDate, BigDecimal price,
                                       List<Seat> result) {
        int currentCount = currentSeats.size();

        // 요청된 수가 현재 수보다 적으면 좌석 수 줄이기
        if (requestedCount < currentCount) {
            reduceSeats(currentSeats, requestedCount, isReservationStarted);
        }

        // 요청된 수가 현재 수보다 많으면 좌석 추가
        if (requestedCount > currentCount) {
            addSeats(grade, showDate, price, currentCount, requestedCount, result, currentSeats);
        }
    }

    // 좌석 수 줄이기
    private void reduceSeats(List<Seat> currentSeats, int requestedCount, boolean isReservationStarted) {
        if (isReservationStarted) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "예매 시작 이후에는 좌석 수 감소가 불가능합니다. 삭제 API를 사용해주세요.");
        }

        int toRemove = currentSeats.size() - requestedCount;

        List<Seat> removable = currentSeats.stream()
                .filter(seat -> seat.getSeatStatus() == SeatStatus.AVAILABLE)
                .sorted(Comparator.comparingInt(Seat::getSeatNumber).reversed())
                .limit(toRemove)
                .toList();

        if (removable.size() < toRemove) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "예약된 좌석이 있어 일부 좌석을 줄일 수 없습니다.");
        }

        seatRepository.deleteAll(removable);
        currentSeats.removeAll(removable);
    }

    // 좌석 추가
    private void addSeats(Grade grade, ShowDate showDate, BigDecimal price, int currentCount,
                          int requestedCount, List<Seat> result, List<Seat> currentSeats) {
        int toAdd = requestedCount - currentCount;

        // 최대 번호 기준으로 좌석 번호 계산
        int nextNumber = currentSeats.stream()
                .mapToInt(Seat::getSeatNumber)
                .max()
                .orElse(0); // 현재 좌석이 없으면 0을 기준으로 시작

        for (int i = 1; i <= toAdd; i++) {
            Seat newSeat = Seat.toEntity(grade, nextNumber + i, price, showDate);
            Seat savedSeat = seatRepository.save(newSeat);

            result.add(savedSeat);
            currentSeats.add(savedSeat); // 저장된 Seat를 currentSeats에도 추가
        }
    }

    // 좌석 가격 업데이트
    private void updateSeatPrices(List<Seat> seats, BigDecimal newPrice) {
        for (Seat seat : seats) {
            seat.updatePrice(newPrice);
        }
    }

    public void saveAll(List<Seat> seats) {
        seatRepository.saveAll(seats);
    }
}