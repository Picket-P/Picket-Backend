package com.example.picket.domain.booking.dto.response;

import com.example.picket.domain.ticket.dto.response.GetTicketResponse;
import com.example.picket.domain.ticket.entity.Ticket;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CancelBookingResponse {

    private List<Ticket> cancelledTickets;

    private CancelBookingResponse(List<Ticket> cancelledTickets) {
        this.cancelledTickets = cancelledTickets;
    }

    public static CancelBookingResponse toDto(List<Ticket> cancelledTickets) {
        return new CancelBookingResponse(cancelledTickets);
    }
}
