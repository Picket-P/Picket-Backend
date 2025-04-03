package com.example.picket.domain.ticket.controller;

import com.example.picket.domain.ticket.dto.response.CreateTicketResponse;
import com.example.picket.domain.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("seats/{seatId}/tickets")
    public ResponseEntity<CreateTicketResponse> createTicket(
            @PathVariable Long seatId,
            @RequestParam Long userId // TODO : 인증/인가 작업 완료 후 수정 필요
    ) {
        CreateTicketResponse createTicketResponse = ticketService.createTicket(userId, seatId);
        return ResponseEntity.ok(createTicketResponse);
    }
}
