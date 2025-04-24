package com.example.picket.domain.booking.dto.response;

import com.example.picket.domain.ticket.dto.response.GetTicketResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class CancelBookingResponse {

    private final List<GetTicketResponse> cancelledTickets;

    private CancelBookingResponse(List<GetTicketResponse> cancelledTickets) {
        this.cancelledTickets = cancelledTickets;
    }

    public static CancelBookingResponse of(List<GetTicketResponse> cancelledTickets) {
        return new CancelBookingResponse(cancelledTickets);
    }
}
