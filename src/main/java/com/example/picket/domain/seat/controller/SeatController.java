package com.example.picket.domain.seat.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.seat.dto.SeatGroupByGradeResponse;
import com.example.picket.domain.seat.dto.SeatUpdateRequest;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.service.SeatCommandService;
import com.example.picket.domain.seat.service.SeatQueryService;
import com.example.picket.domain.seat.service.SeatResponseMapper;
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
    private final SeatResponseMapper seatResponseMapper;

    // 좌석 상세 조회
    @GetMapping("/shows/{showId}/dates/{showDateId}/seats")
    public ResponseEntity<List<SeatGroupByGradeResponse>> getSeats(
            @PathVariable Long showId,
            @PathVariable Long showDateId
    ) {
        List<Seat> seats = seatQueryService.getSeatsByShowDate(showDateId);
        List<SeatGroupByGradeResponse> response = seatResponseMapper.toGroupByGradeResponses(seats);
        return ResponseEntity.ok(response);
    }

    // 좌석 수정
    @PatchMapping("/shows/{showId}/dates/{showDateId}/seats")
    public ResponseEntity<List<SeatGroupByGradeResponse>> updateSeats(
            @Auth AuthUser authUser,
            @PathVariable Long showDateId,
            @RequestBody List<SeatUpdateRequest> requests
    ) {
        List<Seat> updatedSeats = seatCommandService.updateSeats(authUser, showDateId, requests);
        List<SeatGroupByGradeResponse> response = seatResponseMapper.toGroupByGradeResponses(updatedSeats);
        return ResponseEntity.ok(response);
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
