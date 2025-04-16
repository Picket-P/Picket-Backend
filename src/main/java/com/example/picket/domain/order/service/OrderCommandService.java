package com.example.picket.domain.order.service;

import com.example.picket.common.enums.OrderStatus;
import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.order.repository.OrderRepository;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCommandService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(User user, List<Ticket> tickets) {
        BigDecimal totalPrice = tickets.stream()
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Order order = Order.toEntity(user, totalPrice, OrderStatus.ORDER_PENDING, tickets);
        orderRepository.save(order);
        return order;
    }
}
