package com.example.picket.domain.booking.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.booking.dto.request.SeatHoldingRequest;
import com.example.picket.domain.booking.service.SeatHoldingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v2/")
@Tag(name = "좌석 선점 API", description = "사용자가 선택한 좌석을 선점하는 API입니다.")
public class SeatHoldingController {

    private final SeatHoldingService seatHoldingService;

    @Operation(summary = "좌석 선점", description = "사용자가 선택한 좌석을 선점합니다.")
    @PostMapping("show/{showId}/seat-holding")
    public ResponseEntity<Void> seatHolding(
            @PathVariable Long showId,
            @Auth AuthUser authUser,
            @RequestBody SeatHoldingRequest dto
            ) {
        seatHoldingService.seatHoldingLock(authUser.getId(), showId, dto.getSeatIds());
        return ResponseEntity.ok().build();
    }

}
