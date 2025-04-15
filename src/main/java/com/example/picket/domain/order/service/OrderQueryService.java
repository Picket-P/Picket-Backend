package com.example.picket.domain.order.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.order.repository.OrderRepository;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final UserQueryService userQueryService;

    @Transactional(readOnly = true)
    public Order getOrder(Long userId, Long orderId) {

        User user = userQueryService.getUser(userId);

        Order foundOrder = orderRepository.findByUser(user).orElseThrow(
                () -> new CustomException(NOT_FOUND, "존재하지 않는 Order입니다."));
        return foundOrder;
    }
}
