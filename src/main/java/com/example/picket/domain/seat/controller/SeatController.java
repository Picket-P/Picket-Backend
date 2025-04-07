package com.example.picket.domain.seat.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.seat.dto.SeatGroupByGradeResponse;
import com.example.picket.domain.seat.dto.SeatUpdateRequest;
import com.example.picket.domain.seat.service.SeatCommandService;
import com.example.picket.domain.seat.service.SeatQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SeatController {

    private final SeatQueryService seatQueryService;
    private final SeatCommandService seatCommandService;

    // 좌석 상세 조회
    @GetMapping("/shows/{showId}/dates/{showDateId}/seats")
    public ResponseEntity<List<SeatGroupByGradeResponse>> getSeats(
            @PathVariable Long showId,
            @PathVariable Long showDateId
    ) {
        List<SeatGroupByGradeResponse> response = seatQueryService.getSeatsByShowDate(showDateId);
        return ResponseEntity.ok(response);
    }

    // 좌석 수정
    @PatchMapping("/shows/{showId}/dates/{showDateId}/seats")
    public ResponseEntity<Void> updateSeats(
            @Auth AuthUser authUser,
            @PathVariable Long showDateId,
            @RequestBody List<SeatUpdateRequest> requests
    ) {
        seatCommandService.updateSeats(authUser, showDateId, requests);
        return ResponseEntity.ok().build();
    }

    // 좌석 삭제
    @DeleteMapping("/shows/{showId}/dates/{showDateId}/seats/{seatId}")
    public ResponseEntity<Void> deleteSeat(
            @Auth AuthUser authUser,
            @PathVariable Long seatId
    ) {
        seatCommandService.deleteSeat(authUser, seatId);
        return ResponseEntity.noContent().build();
    }
}

