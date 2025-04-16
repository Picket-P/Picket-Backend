package com.example.picket.domain.order.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.dto.PageResponse;
import com.example.picket.domain.order.dto.response.OrderResponse;
import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.order.repository.OrderRepository;
import com.example.picket.domain.order.service.OrderQueryService;
import com.example.picket.domain.ticket.dto.response.GetTicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v2/")
@Tag(name = "주문 조회 API", description = "주문 다건 조회, 단건 조회 기능 API입니다.")
public class OrderController {

    private final OrderQueryService orderQueryService;

    @Operation(summary = "주문 단건 조회", description = "주문을 단건 조회할 수 있습니다.")
    @GetMapping("orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long orderId,
            @Auth AuthUser authUser) {
        Order order = orderQueryService.getOrder(authUser.getId(), orderId);
        OrderResponse orderResponse = OrderResponse.toDto(order);
        return ResponseEntity.ok(orderResponse);
    }

    @Operation(summary = "주문 다건 조회", description = "주문을 다건 조회할 수 있습니다.")
    @GetMapping("orders")
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "1") int page,
            @Auth AuthUser authUser
    ) {
        Page<OrderResponse> orderResponse = orderQueryService.getOrders(size, page, authUser.getId()).map(OrderResponse::toDto);
        PageResponse<OrderResponse> pageOrderResponse = PageResponse.toDto(orderResponse);
        return ResponseEntity.ok(pageOrderResponse);
    }
}
