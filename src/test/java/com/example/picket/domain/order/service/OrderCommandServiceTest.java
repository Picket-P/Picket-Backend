package com.example.picket.domain.order.service;

import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.OrderStatus;
import com.example.picket.common.enums.TicketStatus;
import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.order.repository.OrderRepository;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {

    @InjectMocks
    private OrderCommandService orderCommandService;

    @Mock
    private OrderRepository orderRepository;

    private User user;
    private Ticket ticket1;
    private Ticket ticket2;
    private List<Ticket> tickets;

    @BeforeEach
    void setUp() {
        // 사용자 생성
        user = User.toEntity(
                "user@example.com", "encodedPw", UserRole.USER, null, "nickname",
                LocalDate.of(1990, 1, 1), Gender.MALE
        );

        // 티켓 목업 생성
        ticket1 = mock(Ticket.class);
        when(ticket1.getPrice()).thenReturn(BigDecimal.valueOf(10000));

        ticket2 = mock(Ticket.class);
        when(ticket2.getPrice()).thenReturn(BigDecimal.valueOf(15000));

        tickets = Arrays.asList(ticket1, ticket2);
    }

    @Test
    void createOrder_정상_생성() {
        // Given
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Order result = orderCommandService.createOrder(user, tickets);

        // Then
        assertNotNull(result);

        // Order 엔티티 생성 검증
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order capturedOrder = orderCaptor.getValue();
        assertEquals(user, capturedOrder.getUser());
        assertEquals(BigDecimal.valueOf(25000), capturedOrder.getTotalPrice());
        assertEquals(OrderStatus.ORDER_PENDING, capturedOrder.getOrderStatus());
        assertEquals(tickets, capturedOrder.getTicket());
    }
}