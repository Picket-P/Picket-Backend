package com.example.picket.domain.show.dto.request;

import com.example.picket.domain.seat.dto.request.SeatCreateRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class ShowDateRequest {

    @NotNull(message = "공연 날짜는 필수 입력값입니다.")
    @FutureOrPresent(message = "공연 날짜는 오늘 이후여야 합니다.")
    private LocalDate date;

    @NotNull(message = "공연 시작 시간은 필수 입력값입니다.")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @NotNull(message = "공연 종료 시간은 필수 입력값입니다.")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    @NotNull(message = "좌석 수는 필수 입력값입니다.")
    @Min(value = 1, message = "좌석 수는 1 이상이어야 합니다.")
    private Integer totalSeatCount;

    private List<SeatCreateRequest> seatCreateRequests;

    private ShowDateRequest(LocalDate date, LocalTime startTime, LocalTime endTime,
                            Integer totalSeatCount, List<SeatCreateRequest> seatCreateRequests) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalSeatCount = totalSeatCount;
        this.seatCreateRequests = seatCreateRequests;
    }

    public static ShowDateRequest toDto(LocalDate date, LocalTime startTime, LocalTime endTime,
                                        Integer totalSeatCount, List<SeatCreateRequest> seatCreateRequests) {
        return new ShowDateRequest(date, startTime, endTime, totalSeatCount, seatCreateRequests);
    }
}

