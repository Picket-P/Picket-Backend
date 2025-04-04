package com.example.picket.domain.show.service;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Grade;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.domain.seat.dto.SeatCreateRequest;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.repository.SeatRepository;
import com.example.picket.domain.show.dto.ShowCreateRequest;
import com.example.picket.domain.show.dto.ShowResponse;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import com.example.picket.domain.show.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowCommandService {

    private final ShowRepository showRepository;
    private final ShowDateRepository showDateRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public ShowResponse createShow(@Auth AuthUser authUser, ShowCreateRequest request) {

        validateShowTimes(request); // 공연 시간 검증

        Show show = showRepository.save(
                Show.builder()
                        .directorId(authUser.getId())
                        .title(request.getTitle())
                        .posterUrl(request.getPosterUrl())
                        .category(request.getCategory())
                        .description(request.getDescription())
                        .location(request.getLocation())
                        .reservationStart(request.getReservationStart())
                        .reservationEnd(request.getReservationEnd())
                        .ticketsLimitPerUser(request.getTicketsLimitPerUser())
                        .build()
        );

        List<ShowDate> showDates = request.getDates().stream()
                .map(dateRequest -> {
                    ShowDate showDate = ShowDate.builder()
                            .date(dateRequest.getDate())
                            .startTime(dateRequest.getStartTime())
                            .endTime(dateRequest.getEndTime())
                            .totalSeatCount(dateRequest.getTotalSeatCount())
                            .reservedSeatCount(0)
                            .show(show)
                            .build();

                    showDateRepository.save(showDate);

                    // ⬇️ 좌석 생성
                    createSeatsForShowDate(showDate, dateRequest.getSeatCreateRequests());

                    return showDate;
                }).toList();

        return ShowResponse.from(show, showDates);
    }

    // 공연 시간 검증
    private void validateShowTimes(ShowCreateRequest request) {
        request.getDates().forEach(dateRequest -> {
            if (dateRequest.getStartTime().isAfter(dateRequest.getEndTime())) {
                throw new CustomException(ErrorCode.SHOW_DATE_INVALID_TIME);
            }
        });
    }

    private void createSeatsForShowDate(ShowDate showDate, List<SeatCreateRequest> seatRequests) {
        List<Seat> seats = new ArrayList<>();

        for (SeatCreateRequest seatRequest : seatRequests) {
            Grade grade = seatRequest.getGrade();
            int count = seatRequest.getSeatCount();
            BigDecimal price = seatRequest.getPrice();

            for (int i = 1; i <= count; i++) {
                Seat seat = Seat.builder()
                        .seatNumber(i)
                        .grade(grade)
                        .price(price)
                        .showDate(showDate) // ✅ 이 부분이 중요!
                        .build();

                seats.add(seat);
            }
        }

        seatRepository.saveAll(seats);
    }
}