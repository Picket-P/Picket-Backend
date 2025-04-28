package com.example.picket.domain.booking.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CancelBookingRequest {

    @Schema(description = "취소할 티켓 ID", example = "[1]")
    @NotNull(message = "티켓 ID는 필수 입력값입니다.")
    private List<Long> ticketIds = new ArrayList<>();

    @Schema(description = "취소 이유", example = "단순 변심")
    @NotNull(message = "취소 이유는 필수 입력값입니다.")
    private String cancelReason;
}
