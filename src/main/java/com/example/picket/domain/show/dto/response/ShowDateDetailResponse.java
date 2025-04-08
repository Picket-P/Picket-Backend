package com.example.picket.domain.show.dto.response;

import com.example.picket.domain.seat.dto.response.SeatSummaryResponse;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class ShowDateDetailResponse {

    private final Long id;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Integer totalSeatCount;
    private final Integer reservedSeatCount;
    private final Integer availableSeatCount;
    private final List<SeatSummaryResponse> seats;

    private ShowDateDetailResponse(Long id, LocalDate date, LocalTime startTime, LocalTime endTime, Integer totalSeatCount,
                                   Integer reservedSeatCount, Integer availableSeatCount, List<SeatSummaryResponse> seats) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalSeatCount = totalSeatCount;
        this.reservedSeatCount = reservedSeatCount;
        this.availableSeatCount = availableSeatCount;
        this.seats = seats;
    }

    public static ShowDateDetailResponse toDto(Long id, LocalDate date, LocalTime startTime, LocalTime endTime, Integer totalSeatCount,
                                               Integer reservedSeatCount, Integer availableSeatCount, List<SeatSummaryResponse> seats) {
        return new ShowDateDetailResponse(
            id,
            date,
            startTime,
            endTime,
            totalSeatCount,
            reservedSeatCount,
            availableSeatCount,
            seats
        );
    }

}
