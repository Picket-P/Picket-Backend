package com.example.picket.domain.ticket.dto.response;

import com.example.picket.common.enums.TicketStatus;
import com.example.picket.domain.ticket.entity.Ticket;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class GetTicketResponse {

    private Long id;

    private Long userId;

    private Long showId;

    private Long seatId;

    private BigDecimal price;

    private TicketStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    public GetTicketResponse(Ticket ticket) {
        this.id = ticket.getId();
        this.userId = ticket.getUser().getId();
        this.showId = ticket.getShow().getId();
        this.seatId = ticket.getSeat().getId();
        this.price = ticket.getPrice();
        this.status = ticket.getStatus();
        this.createdAt = ticket.getCreatedAt();
        this.modifiedAt = ticket.getModifiedAt();
    }

    public static GetTicketResponse from(Ticket ticket) {
        return new GetTicketResponse(ticket);
    }
}
