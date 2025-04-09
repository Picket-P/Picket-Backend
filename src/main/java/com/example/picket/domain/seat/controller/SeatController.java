package com.example.picket.domain.seat.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.seat.dto.request.SeatUpdateRequest;
import com.example.picket.domain.seat.dto.response.SeatGroupByGradeResponse;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.service.SeatCommandService;
import com.example.picket.domain.seat.service.SeatQueryService;
import com.example.picket.domain.seat.service.SeatResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "좌석 관리 API", description = "좌석 조회, 좌석 수정, 좌석 삭제 기능 API입니다.")
public class SeatController {

    private final SeatQueryService seatQueryService;
    private final SeatCommandService seatCommandService;
    private final SeatResponseMapper seatResponseMapper;

    @Operation(summary = "좌석 상세 조회", description = "좌석을 조회할 수 있습니다.")
    @GetMapping("/dates/{showDateId}/seats")
    public ResponseEntity<List<SeatGroupByGradeResponse>> getSeats(
            @PathVariable Long showDateId
    ) {
        List<Seat> seats = seatQueryService.getSeatsByShowDate(showDateId);
        List<SeatGroupByGradeResponse> response = seatResponseMapper.toGroupByGradeResponses(seats);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "좌석 수정", description = "좌석을 수정할 수 있습니다.")
    @PutMapping("/dates/{showDateId}/seats")
    public ResponseEntity<List<SeatGroupByGradeResponse>> updateSeats(
            @Auth AuthUser authUser,
            @PathVariable Long showDateId,
            @RequestBody List<SeatUpdateRequest> requests
    ) {
        List<Seat> updatedSeats = seatCommandService.updateSeats(authUser, showDateId, requests);
        List<SeatGroupByGradeResponse> response = seatResponseMapper.toGroupByGradeResponses(updatedSeats);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "좌석 삭제", description = "좌석을 삭제할 수 있습니다.")
    @DeleteMapping("/seats/{seatId}")
    public ResponseEntity<Void> deleteSeat(
            @Auth AuthUser authUser,
            @PathVariable Long seatId
    ) {
        seatCommandService.deleteSeat(authUser, seatId);
        return ResponseEntity.noContent().build();
    }
}
