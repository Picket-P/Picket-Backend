package com.example.picket.domain.seat.controller;

import com.example.picket.domain.seat.dto.SeatGroupByGradeResponse;
import com.example.picket.domain.seat.service.SeatQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SeatController {

    private final SeatQueryService seatQueryService;

    @GetMapping("/shows/{showId}/dates/{showDateId}/seats")
    public ResponseEntity<List<SeatGroupByGradeResponse>> getSeats(
            @PathVariable Long showId,
            @PathVariable Long showDateId
    ) {
        List<SeatGroupByGradeResponse> response = seatQueryService.getSeatsByShowDate(showDateId);
        return ResponseEntity.ok(response);
    }
}

