package com.example.picket.domain.show.service;

import com.example.picket.domain.seat.dto.response.SeatSummaryResponse;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.service.SeatQueryService;
import com.example.picket.domain.show.dto.response.ShowDateResponse;
import com.example.picket.domain.show.dto.response.ShowResponse;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import java.util.List;
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
        List<Seat> seats = seatQueryService.getSeatsByShowDate(showDate.getId());
        ShowDateResponse response = ShowDateResponse.toDto(showDate);
        return response;
    }

}


