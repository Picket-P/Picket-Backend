package com.example.picket.domain.order.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.order.repository.OrderRepository;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final UserQueryService userQueryService;

    @Transactional(readOnly = true)
    public Order getOrder(Long userId, Long orderId) {
        User user = userQueryService.getUser(userId);
        return orderRepository.findByIdAndUser(orderId, user).orElseThrow(
                () -> new CustomException(NOT_FOUND, "존재하지 않는 Order입니다."));
    }

    @Transactional(readOnly = true)
    public Page<Order> getOrders(int size, int page, Long userId) {
        User user = userQueryService.getUser(userId);
        Sort sortStandard = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page - 1, size, sortStandard);

        return orderRepository.findByUser(user, pageable);
    }
}
