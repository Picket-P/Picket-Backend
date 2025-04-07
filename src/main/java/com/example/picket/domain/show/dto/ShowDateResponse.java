package com.example.picket.domain.show.dto;

import com.example.picket.domain.seat.dto.SeatSummaryResponse;
import com.example.picket.domain.show.entity.ShowDate;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ShowDateResponse {

    private final Long id;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Integer totalSeatCount;
    private final Integer reservedSeatCount;
    private final Integer availableSeatCount;
    private final List<SeatSummaryResponse> seatSummary;

    public ShowDateResponse(ShowDate showDate) {
        this.id = showDate.getId();
        this.date = showDate.getDate();
        this.startTime = showDate.getStartTime();
        this.endTime = showDate.getEndTime();
        this.totalSeatCount = showDate.getTotalSeatCount();
        this.reservedSeatCount = showDate.getReservedSeatCount();
        this.availableSeatCount = showDate.getAvailableSeatCount();
        this.seatSummary = new ArrayList<>();
    }

    public static ShowDateResponse toDto(ShowDate showDate) {
        return new ShowDateResponse(showDate);
    }

    public void setSeatSummary(List<SeatSummaryResponse> summaryList) {
        this.seatSummary.clear();
        this.seatSummary.addAll(summaryList);
    }
}