package com.example.picket.domain.ticket.dto.response;

import com.example.picket.common.enums.TicketStatus;
import com.example.picket.domain.ticket.entity.Ticket;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class CreateTicketResponse {

    private Long id;

    private Long userId;

    private Long showId;

    private Long seatId;

    private BigDecimal price;

    private TicketStatus ticketStatus;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private CreateTicketResponse(Ticket ticket) {
        this.id = ticket.getId();
        this.userId = ticket.getUser().getId();
        this.showId = ticket.getShow().getId();
        this.seatId = ticket.getSeat().getId();
        this.price = ticket.getPrice();
        this.ticketStatus = ticket.getStatus();
        this.createdAt = ticket.getCreatedAt();
        this.modifiedAt = ticket.getModifiedAt();
    }

    public static CreateTicketResponse toDto(Ticket ticket) {
        return new CreateTicketResponse(ticket);
    }
}
