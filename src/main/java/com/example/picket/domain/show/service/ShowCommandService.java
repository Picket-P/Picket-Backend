package com.example.picket.domain.show.service;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.Grade;
import com.example.picket.common.enums.SeatStatus;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.domain.seat.dto.SeatCreateRequest;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.repository.SeatRepository;
import com.example.picket.domain.show.dto.ShowCreateRequest;
import com.example.picket.domain.show.dto.ShowDateResponse;
import com.example.picket.domain.show.dto.ShowResponse;
import com.example.picket.domain.show.dto.ShowUpdateRequest;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import com.example.picket.domain.show.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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

                    // 좌석 생성
                    createSeatsForShowDate(showDate, dateRequest.getSeatCreateRequests());

                    return showDate;
                }).toList();

        return ShowResponse.from(
                show,
                showDates.stream()
                        .map(ShowDateResponse::from)
                        .toList()
        );
    }

    // 공연 정보 수정
    @Transactional
    public ShowResponse updateShow(@Auth AuthUser authUser, Long showId, ShowUpdateRequest request) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHOW_NOT_FOUND));

        if (!show.getDirectorId().equals(authUser.getId())) {
            throw new CustomException(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다.");
        }

        if (show.getReservationStart().isBefore(LocalDateTime.now())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "예매 시작 이후에는 공연을 수정할 수 없습니다.");
        }

        List<ShowDate> existingDates = showDateRepository.findAllByShowId(showId);
        boolean anyDateEnded = existingDates.stream()
                .anyMatch(sd -> sd.getDate().atTime(sd.getEndTime()).isBefore(LocalDateTime.now()));
        if (anyDateEnded) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "종료된 공연은 수정할 수 없습니다.");
        }

        if (request.getCategory() != null) {
            boolean isValidCategory = Arrays.stream(Category.values())
                    .anyMatch(c -> c.name().equalsIgnoreCase(request.getCategory().name()));

            if (!isValidCategory) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "지원하지 않는 카테고리입니다.");
            }
        }

        // 업데이트
        show.update(request);

        return ShowResponse.from(show,
                showDateRepository.findAllByShowId(showId).stream()
                        .map(ShowDateResponse::from)
                        .toList());
    }

    // 공연 삭제
    @Transactional
    public void deleteShow(@Auth AuthUser authUser, Long showId) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHOW_NOT_FOUND));

        if (!show.getDirectorId().equals(authUser.getId())) {
            throw new CustomException(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다.");
        }

        if (show.getReservationStart().isBefore(LocalDateTime.now())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "예매 시작 이후에는 공연을 삭제할 수 없습니다.");
        }

        show.softDelete(); // soft delete
    }

    // 공연 시간 검증
    private void validateShowTimes(ShowCreateRequest request) {

        request.getDates().forEach(dateRequest -> {
            if (dateRequest.getStartTime().isAfter(dateRequest.getEndTime())) {
                throw new CustomException(ErrorCode.SHOW_DATE_INVALID_TIME);
            }
        });
    }

    // 공연 날짜 별로 좌석 생성
    private void createSeatsForShowDate(ShowDate showDate, List<SeatCreateRequest> seatRequests) {

        List<Seat> seats = new ArrayList<>();

        for (SeatCreateRequest seatRequest : seatRequests) {
            Grade grade = seatRequest.getGrade();
            int count = seatRequest.getSeatCount();
            BigDecimal price = seatRequest.getPrice();

            for (int i = 1; i <= count; i++) {
                Seat seat = Seat.builder()
                        .seatNumber(i)
                        .seatStatus(SeatStatus.AVAILABLE)
                        .grade(grade)
                        .price(price)
                        .showDate(showDate)
                        .build();

                seats.add(seat);
            }
        }

        seatRepository.saveAll(seats);
    }
}