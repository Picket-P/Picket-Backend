package com.example.picket.domain.order.service;

import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.OrderStatus;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.order.repository.OrderRepository;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

    @InjectMocks
    private OrderQueryService orderQueryService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserQueryService userQueryService;

    private User user;
    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        // 사용자 생성
        user = User.toEntity(
                "user@example.com", "encodedPw", UserRole.USER, null, "nickname",
                LocalDate.of(1990, 1, 1), Gender.MALE
        );

        // 주문 생성
        order1 = mock(Order.class);
        lenient().when(order1.getId()).thenReturn(1L);
        lenient().when(order1.getUser()).thenReturn(user);
        lenient().when(order1.getTotalPrice()).thenReturn(BigDecimal.valueOf(20000));
        lenient().when(order1.getOrderStatus()).thenReturn(OrderStatus.ORDER_PENDING);

        order2 = mock(Order.class);
        lenient().when(order2.getId()).thenReturn(2L);
        lenient().when(order2.getUser()).thenReturn(user);
        lenient().when(order2.getTotalPrice()).thenReturn(BigDecimal.valueOf(30000));
        lenient().when(order2.getOrderStatus()).thenReturn(OrderStatus.ORDER_COMPLETE);
    }

    @Test
    void 주문_단건_조회_시_정상적으로_조회할_수_있다() {
        // Given
        Long userId = 1L;
        Long orderId = 1L;
        when(userQueryService.getUser(userId)).thenReturn(user);
        when(orderRepository.findByIdAndUser(orderId, user)).thenReturn(Optional.of(order1));

        // When
        Order result = orderQueryService.getOrder(userId, orderId);

        // Then
        assertNotNull(result);
        assertEquals(order1, result);
        verify(userQueryService).getUser(userId);
        verify(orderRepository).findByIdAndUser(orderId, user);
    }

    @Test
    void 주문_단건_조회_시_존재하지_않는_주문일_경우_예외가_발생한다() {
        // Given
        Long userId = 1L;
        Long orderId = 999L;
        when(userQueryService.getUser(userId)).thenReturn(user);
        when(orderRepository.findByIdAndUser(orderId, user)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            orderQueryService.getOrder(userId, orderId);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("존재하지 않는 Order입니다.", exception.getMessage());
        verify(userQueryService).getUser(userId);
        verify(orderRepository).findByIdAndUser(orderId, user);
    }

    @Test
    void 주문_다건_조회_시_정상적으로_조회할_수_있다() {
        // Given
        int size = 10;
        int page = 1;
        Long userId = 1L;
        List<Order> orderList = Arrays.asList(order1, order2);
        Page<Order> orderPage = new PageImpl<>(orderList);

        when(userQueryService.getUser(userId)).thenReturn(user);
        when(orderRepository.findByUser(eq(user), any(PageRequest.class))).thenReturn(orderPage);

        // When
        Page<Order> result = orderQueryService.getOrders(size, page, userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(orderList, result.getContent());

        verify(userQueryService).getUser(userId);
        verify(orderRepository).findByUser(eq(user), any(PageRequest.class));
    }

    @Test
    void 주문_다건_조회_시_페이지_파라미터_검증() {
        // Given
        int size = 20;
        int page = 2;
        Long userId = 1L;
        Page<Order> emptyPage = Page.empty();

        when(userQueryService.getUser(userId)).thenReturn(user);
        when(orderRepository.findByUser(eq(user), any(PageRequest.class))).thenReturn(emptyPage);

        // When
        orderQueryService.getOrders(size, page, userId);

        // Then
        verify(userQueryService).getUser(userId);

        // PageRequest 객체 생성 검증
        verify(orderRepository).findByUser(
                eq(user),
                argThat(pageRequest ->
                        pageRequest.getPageNumber() == 1 &&
                                pageRequest.getPageSize() == 20 &&
                                pageRequest.getSort().equals(Sort.by("createdAt").descending())
                )
        );
    }

    @Test
    void 주문_다건_조회_시_빈_결과일_경우() {
        // Given
        int size = 10;
        int page = 1;
        Long userId = 1L;
        Page<Order> emptyPage = Page.empty();

        when(userQueryService.getUser(userId)).thenReturn(user);
        when(orderRepository.findByUser(eq(user), any(PageRequest.class))).thenReturn(emptyPage);

        // When
        Page<Order> result = orderQueryService.getOrders(size, page, userId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

        verify(userQueryService).getUser(userId);
        verify(orderRepository).findByUser(eq(user), any(PageRequest.class));
    }

}