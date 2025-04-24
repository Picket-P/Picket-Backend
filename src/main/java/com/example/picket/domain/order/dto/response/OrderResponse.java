package com.example.picket.domain.order.dto.response;

import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.ticket.dto.response.GetTicketResponse;
import lombok.Getter;

import java.math.BigDecimal;
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
        this.tickets = order.getTicket().stream().map(GetTicketResponse::of).toList();
    }

    public static OrderResponse of(Order order) {
        return new OrderResponse(order);
    }

}
