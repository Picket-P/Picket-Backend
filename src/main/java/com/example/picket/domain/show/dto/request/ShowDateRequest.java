package com.example.picket.domain.show.dto.request;

import com.example.picket.domain.seat.dto.request.SeatCreateRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class ShowDateRequest {

    @Schema(description = "공연 날짜 (형식: yyyy-MM-dd)", example = "2025-05-12")
    @NotNull(message = "공연 날짜는 필수 입력값입니다.")
    @FutureOrPresent(message = "공연 날짜는 오늘 이후여야 합니다.")
    private LocalDate date;

    @Schema(description = "공연 시작 시간 (형식: HH:mm:ss)", example = "14:00:00")
    @NotNull(message = "공연 시작 시간은 필수 입력값입니다.")
    private LocalTime startTime;

    @Schema(description = "공연 종료 시간 (형식: HH:mm:ss)", example = "16:00:00")
    @NotNull(message = "공연 종료 시간은 필수 입력값입니다.")
    private LocalTime endTime;

    @Schema(description = "총 좌석 수", example = "100")
    @NotNull(message = "좌석 수는 필수 입력값입니다.")
    @Min(value = 1, message = "좌석 수는 1 이상이어야 합니다.")
    private Integer totalSeatCount;

    @Schema(description = "좌석 생성 요청 리스트", example = "[{\"grade\": \"VIP\", \"seatCount\": 100, \"price\": 50000}]")
    private List<SeatCreateRequest> seatCreateRequests;

}

