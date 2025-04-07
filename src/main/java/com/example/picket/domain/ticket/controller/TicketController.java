package com.example.picket.domain.ticket.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.ticket.dto.response.CreateTicketResponse;
import com.example.picket.domain.ticket.dto.response.DeleteTicketResponse;
import com.example.picket.domain.ticket.dto.response.GetTicketResponse;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.ticket.service.TicketCommandService;
import com.example.picket.domain.ticket.service.TicketQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class TicketController {

    private final TicketCommandService ticketCommandService;
    private final TicketQueryService ticketQueryService;

    @PostMapping("seats/{seatId}/tickets")
    public ResponseEntity<CreateTicketResponse> createTicket(
            @PathVariable Long seatId,
            @Auth AuthUser authUser) {
        Ticket ticket = ticketCommandService.createTicket(authUser.getId(), authUser.getUserRole(), seatId);
        return ResponseEntity.ok(CreateTicketResponse.toDto(ticket));
    }

    @GetMapping("tickets")
    public ResponseEntity<Page<GetTicketResponse>> getTickets(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "1") int page,
            @Auth AuthUser authUser
    ) {
        Page<GetTicketResponse> getTicketResponsePage = ticketQueryService.getTickets(authUser.getId(), size, page).map(GetTicketResponse::toDto);
        return ResponseEntity.ok(getTicketResponsePage);
    }

    @GetMapping("tickets/{ticketId}")
    public ResponseEntity<GetTicketResponse> getTicket(
            @PathVariable Long ticketId) {
        Ticket ticket = ticketQueryService.getTicket(ticketId);
        return ResponseEntity.ok(GetTicketResponse.toDto(ticket));
    }

    @PatchMapping("tickets/{ticketId}")
    public ResponseEntity<DeleteTicketResponse> deleteTicket(
            @PathVariable Long ticketId,
            @Auth AuthUser authUser) {
        Ticket ticket = ticketCommandService.deleteTicket(ticketId, authUser.getId());
        return ResponseEntity.ok(DeleteTicketResponse.toDto(ticket));
    }
}
