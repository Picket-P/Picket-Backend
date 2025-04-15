package com.example.picket.domain.order.dto.response;

import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.ticket.dto.response.GetTicketResponse;
import com.example.picket.domain.ticket.entity.Ticket;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
public class OrderResponse {

    private Long id;

    private Long userId;

    private BigDecimal totalPrice;

    private List<GetTicketResponse> tickets;

    private OrderResponse(Order order) {
        this.id = order.getId();
        this.userId = order.getUser().getId();
        this.totalPrice = order.getTotalPrice();
        this.tickets = order.getTicket().stream().map(GetTicketResponse::toDto).toList();
    }

    public static OrderResponse toDto(Order order) {
        return new OrderResponse(order);
    }

}
