package com.example.picket.domain.show.service;

import com.example.picket.common.enums.SeatStatus;
import com.example.picket.domain.seat.dto.response.SeatSummaryResponse;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.service.SeatQueryService;
import com.example.picket.domain.show.dto.response.ShowDateResponse;
import com.example.picket.domain.show.dto.response.ShowResponse;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShowResponseMapper {

    private final SeatQueryService seatQueryService;

    // 공연 + 날짜 + 좌석 요약 정보를 포함한 응답 변환
    public ShowResponse toDto(Show show, List<ShowDate> showDates) {
        List<ShowDateResponse> showDateResponses = showDates.stream()
                .map(this::toShowDateResponseWithSeatSummary)
                .toList();

        return ShowResponse.toDto(show, showDateResponses);
    }

    // 좌석 요약 정보 포함한 공연 날짜 응답 생성
    private ShowDateResponse toShowDateResponseWithSeatSummary(ShowDate showDate) {
        List<Seat> seats = seatQueryService.findAllByShowDateId(showDate.getId());
        List<SeatSummaryResponse> seatSummaries = buildSeatSummary(seats);

        ShowDateResponse response = ShowDateResponse.toDto(showDate);
        response.setSeatSummary(seatSummaries);
        return response;
    }

    // 좌석 등급별 요약 정보 생성 (전체/예약됨/남은 좌석 수)
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

                            return SeatSummaryResponse.toDto(
                                    groupedSeats.get(0).getGrade(),
                                    total,
                                    reserved,
                                    available
                            );
                        })
                ))
                .values()
                .stream()
                .toList();
    }
}


