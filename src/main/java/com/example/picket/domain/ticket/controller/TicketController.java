package com.example.picket.domain.ticket.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.annotation.AuthPermission;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.ticket.dto.response.CreateTicketResponse;
import com.example.picket.domain.ticket.dto.response.DeleteTicketResponse;
import com.example.picket.domain.ticket.dto.response.GetTicketResponse;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.ticket.service.TicketCommandService;
import com.example.picket.domain.ticket.service.TicketQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@Tag(name = "티켓 관리 API", description = "티켓 생성, 다건 조회, 단건조회, 삭제 기능 API입니다.")
public class TicketController {

    private final TicketCommandService ticketCommandService;
    private final TicketQueryService ticketQueryService;

    @Operation(summary = "티켓 생성", description = "티켓을 생성할 수 있습니다.")
    @PostMapping("seats/{seatId}/tickets")
    @AuthPermission(role = UserRole.USER)
    public ResponseEntity<CreateTicketResponse> createTicket(
            @PathVariable Long seatId,
            @Auth AuthUser authUser) {
        Ticket ticket = ticketCommandService.createTicket(authUser.getId(), authUser.getUserRole(), seatId);
        return ResponseEntity.ok(CreateTicketResponse.toDto(ticket));
    }

    @Operation(summary = "티켓 다건 조회", description = "티켓을 다건 조회할 수 있습니다.")
    @GetMapping("tickets")
    public ResponseEntity<Page<GetTicketResponse>> getTickets(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "1") int page,
            @Auth AuthUser authUser
    ) {
        Page<GetTicketResponse> getTicketResponsePage = ticketQueryService.getTickets(authUser.getId(), size, page)
                .map(GetTicketResponse::toDto);
        return ResponseEntity.ok(getTicketResponsePage);
    }

    @Operation(summary = "티켓 단건 조회", description = "티켓을 단건 조회할 수 있습니다.")
    @GetMapping("tickets/{ticketId}")
    public ResponseEntity<GetTicketResponse> getTicket(
            @PathVariable Long ticketId,
            @Auth AuthUser authUser
    ) {
        Ticket ticket = ticketQueryService.getTicket(authUser.getId(), ticketId);
        return ResponseEntity.ok(GetTicketResponse.toDto(ticket));
    }

    @Operation(summary = "티켓 삭제", description = "티켓을 삭제할 수 있습니다.")
    @PutMapping("tickets/{ticketId}")
    public ResponseEntity<DeleteTicketResponse> deleteTicket(
            @PathVariable Long ticketId,
            @Auth AuthUser authUser) {
        Ticket ticket = ticketCommandService.deleteTicket(ticketId, authUser.getId());
        return ResponseEntity.ok(DeleteTicketResponse.toDto(ticket));
    }
}
