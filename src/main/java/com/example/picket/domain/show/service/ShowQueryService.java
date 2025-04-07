package com.example.picket.domain.show.service;

import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.SeatStatus;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.domain.seat.dto.SeatSummaryResponse;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.repository.SeatRepository;
import com.example.picket.domain.show.dto.ShowDateResponse;
import com.example.picket.domain.show.dto.ShowResponse;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import com.example.picket.domain.show.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowQueryService {

    private final ShowRepository showRepository;
    private final ShowDateRepository showDateRepository;
    private final SeatRepository seatRepository;

    public List<ShowResponse> getShows(String category, String sortBy, String order) {
        List<Show> shows;

        // 카테고리 미지정일 경우 기본 정렬
        if (category != null && !category.isBlank()) {
            shows = showRepository.findAllByCategoryAndIsDeletedFalse(Category.valueOf(category.toUpperCase()));
        } else {
            shows = showRepository.findAll();
        }

        List<ShowResponse> responses = shows.stream()
                .map(show -> {
                    List<ShowDateResponse> showDateResponses = showDateRepository.findAllByShowId(show.getId())
                            .stream()
                            .map(showDate -> {
                                ShowDateResponse response = ShowDateResponse.from(showDate);

                                List<Seat> seats = seatRepository.findAllByShowDateId(showDate.getId());
                                List<SeatSummaryResponse> summaryList = buildSeatSummary(seats);
                                response.setSeatSummary(summaryList);

                                return response;
                            })
                            .toList();

                    return ShowResponse.from(show, showDateResponses);
                })
                .collect(Collectors.toList());

        // 정렬 기준 정의
        Comparator<ShowResponse> comparator = Comparator.comparing(ShowResponse::getCreatedAt);
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        responses.sort(comparator);
        return responses;
    }

    public ShowResponse getShowDetails(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHOW_NOT_FOUND));

        List<ShowDate> showDates = showDateRepository.findAllByShowId(showId);

        List<ShowDateResponse> showDateResponses = showDates.stream()
                .map(showDate -> {
                    ShowDateResponse response = ShowDateResponse.from(showDate);

                    List<Seat> seats = seatRepository.findAllByShowDateId(showDate.getId());
                    List<SeatSummaryResponse> summaryList = buildSeatSummary(seats);
                    response.setSeatSummary(summaryList);

                    return response;
                })
                .sorted(Comparator.comparing(ShowDateResponse::getDate))
                .toList();

        return ShowResponse.from(show, showDateResponses);
    }

    private List<SeatSummaryResponse> buildSeatSummary(List<Seat> seats) {
        return seats.stream()
                .collect(Collectors.groupingBy(
                        Seat::getGrade,
                        Collectors.collectingAndThen(Collectors.toList(), groupedSeats -> {
                            int total = groupedSeats.size();
                            int reserved = (int) groupedSeats.stream()
                                    .filter(seat -> seat.getSeatStatus() == SeatStatus.RESERVED)
                                    .count();
                            int available = total - reserved;

                            return SeatSummaryResponse.builder()
                                    .grade(groupedSeats.get(0).getGrade())
                                    .total(total)
                                    .reserved(reserved)
                                    .available(available)
                                    .build();
                        })
                ))
                .values()
                .stream()
                .toList();
    }
}