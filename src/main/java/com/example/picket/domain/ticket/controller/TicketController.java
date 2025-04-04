package com.example.picket.domain.ticket.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.ticket.dto.response.CreateTicketResponse;
import com.example.picket.domain.ticket.dto.response.DeleteTicketResponse;
import com.example.picket.domain.ticket.dto.response.GetTicketResponse;
import com.example.picket.domain.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("seats/{seatId}/tickets")
    public ResponseEntity<CreateTicketResponse> createTicket(
            @PathVariable Long seatId,
            @Auth AuthUser authUser) {
        CreateTicketResponse createTicketResponse = ticketService.createTicket(authUser.getId(), authUser.getUserRole(), seatId);
        return ResponseEntity.ok(createTicketResponse);
    }

    @GetMapping("tickets")
    public ResponseEntity<Page<GetTicketResponse>> getTickets(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "1") int page,
            @Auth AuthUser authUser
    ) {
        Page<GetTicketResponse> getTicketResponsePage = ticketService.getTickets(authUser.getId(), size, page);
        return ResponseEntity.ok(getTicketResponsePage);
    }

    @GetMapping("tickets/{ticketId}")
    public ResponseEntity<GetTicketResponse> getTicket(
            @PathVariable Long ticketId) {
        GetTicketResponse getTicketResponse = ticketService.getTicket(ticketId);
        return ResponseEntity.ok(getTicketResponse);
    }

    @PatchMapping("tickets/{ticketId}")
    public ResponseEntity<DeleteTicketResponse> deleteTicket(
            @PathVariable Long ticketId,
            @Auth AuthUser authUser) {
        DeleteTicketResponse deleteTicketResponse = ticketService.deleteTicket(ticketId, authUser.getId());
        return ResponseEntity.ok(deleteTicketResponse);
    }
}
