package com.example.picket.domain.booking.service;

import com.example.picket.common.enums.OrderStatus;
import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.order.service.OrderCommandService;
import com.example.picket.domain.payment.entity.Payment;
import com.example.picket.domain.payment.service.PaymentCommandService;
import com.example.picket.domain.payment.service.PaymentQueryService;
import com.example.picket.domain.seat_holding.service.SeatHoldingService;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.service.ShowDateCommandService;
import com.example.picket.domain.show.service.ShowDateQueryService;
import com.example.picket.domain.show.service.ShowQueryService;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.ticket.service.TicketCommandService;
import com.example.picket.domain.ticket.service.TicketQueryService;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final String TOSS_PAY_BASE_URL = "https://api.tosspayments.com/v1/payments/";

    @Mock private SeatHoldingService seatHoldingService;
    @Mock private ShowQueryService showQueryService;
    @Mock private UserQueryService userQueryService;
    @Mock private ShowDateQueryService showDateQueryService;
    @Mock private TicketQueryService ticketQueryService;
    @Mock private OrderCommandService orderCommandService;
    @Mock private TicketCommandService ticketCommandService;
    @Mock private ShowDateCommandService showDateCommandService;
    @Mock private PaymentCommandService paymentCommandService;
    @Mock private PaymentQueryService paymentQueryService;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private WebClient webClient;
    @Mock private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private BookingService bookingService;

    private final Long showId = 1L;
    private final Long showDateId = 2L;
    private final Long userId = 3L;
    private final List<Long> seatIds = Arrays.asList(10L, 11L, 12L);
    private final String paymentKey = "payment_key_123";
    private final String orderId = "order_id_123";
    private final BigDecimal amount = new BigDecimal("30000");

    @Test
    @DisplayName("성공적으로 예매할 수 있다.")
    void successBooking() throws InterruptedException {
        // given
        String key = "PAYMENT-INFO:USER:" + userId;
        when(hashOperations.get(key, "orderId")).thenReturn(orderId);
        when(hashOperations.get(key, "amount")).thenReturn(amount.toString());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("paymentKey", "toss_payment_key_123");
        responseBody.put("orderId", "order_id_123");
        responseBody.put("orderName", "테스트 주문");
        responseBody.put("status", "DONE");
        responseBody.put("totalAmount", 30000);

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TOSS_PAY_BASE_URL + "/confirm")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        // 이 부분에서 정확한 헤더값을 검증하고 싶다면 ArgumentMatcher 사용
        when(requestBodySpec.header(eq("Authorization"), anyString())).thenReturn(requestBodySpec);
        // 요청 본문 값을 검증하고 싶다면 ArgumentCaptor 사용
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(responseBody));

        Show show = mock(Show.class);
        User user = mock(User.class);
        List<Ticket> tickets = Arrays.asList(mock(Ticket.class), mock(Ticket.class));
        Order order = mock(Order.class);
        Payment payment = mock(Payment.class);

        when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(show.getReservationStart()).thenReturn(LocalDateTime.now().minusDays(1));
        when(show.getReservationEnd()).thenReturn(LocalDateTime.now().plusDays(1));
        when(showQueryService.getShow(showId)).thenReturn(show);
        when(userQueryService.getUser(userId)).thenReturn(user);
        doNothing().when(seatHoldingService).seatHoldingCheck(userId, seatIds);
        doNothing().when(ticketQueryService).checkTicketLimit(user, show);
        when(ticketCommandService.createTicket(user, show, seatIds)).thenReturn(tickets);
        when(orderCommandService.createOrder(user, tickets)).thenReturn(order);
        when(paymentCommandService.createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), eq(order))).thenReturn(payment);
        doNothing().when(showDateCommandService).countUpdateOnBooking(showDateId, seatIds.size());
        doNothing().when(seatHoldingService).seatHoldingUnLock(seatIds);

        // when
        Order result = bookingService.booking(showId, showDateId, userId, seatIds, paymentKey, orderId, amount);

        // then
        assertNotNull(result);
        assertEquals(order, result);

        verify(showQueryService).getShow(showId);
        verify(userQueryService).getUser(userId);
        verify(seatHoldingService).seatHoldingCheck(userId, seatIds);
        verify(ticketQueryService).checkTicketLimit(user, show);
        verify(ticketCommandService).createTicket(user, show, seatIds);
        verify(orderCommandService).createOrder(user, tickets);
        verify(paymentCommandService).createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), eq(order));
        verify(showDateCommandService).countUpdateOnBooking(showDateId, seatIds.size());
        verify(seatHoldingService).seatHoldingUnLock(seatIds);
        verify(stringRedisTemplate).delete(key);

    }

    @Test
    void successCancelBooking() throws InterruptedException {
        // given
        Long paymentId = 1L;
        String cancelReason = "고객 요청";
        BigDecimal cancelAmount = new BigDecimal("30000");
        User user = mock(User.class);
        Order order = mock(Order.class);
        Payment payment = mock(Payment.class);
        ShowDate showDate = mock(ShowDate.class);
        List<Ticket> canceledTickets = Arrays.asList(mock(Ticket.class), mock(Ticket.class));

        Map<String, Object> cancelResponseBody = new HashMap<>();
        List<Map<String, Object>> cancels = new ArrayList<>();
        Map<String, Object> cancelInfo = new HashMap<>();
        cancelInfo.put("cancelAmount", 30000);
        cancels.add(cancelInfo);
        cancelResponseBody.put("cancels", cancels);
        cancelResponseBody.put("status", "CANCELED");
        cancelResponseBody.put("paymentKey", "toss_payment_key_123");
        cancelResponseBody.put("orderId", "order_id_123");
        cancelResponseBody.put("orderName", "테스트 주문");
        cancelResponseBody.put("totalAmount", 30000);

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TOSS_PAY_BASE_URL + "/" + paymentKey + "/cancel")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Authorization"), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(cancelResponseBody));

        when(paymentCommandService.createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), eq(order))).thenReturn(payment);
        when(ticketQueryService.addUpTicketPrice(seatIds)).thenReturn(cancelAmount);
        when(paymentQueryService.getPayment(paymentId)).thenReturn(payment);
        when(payment.getTossPaymentKey()).thenReturn(paymentKey);
        when(payment.getOrder()).thenReturn(order);
        when(showDateQueryService.getShowDate(showDateId)).thenReturn(showDate);
        when(userQueryService.getUser(userId)).thenReturn(user);
        when(ticketCommandService.deleteTicket(user, seatIds)).thenReturn(canceledTickets);

        when(showDate.getDate()).thenReturn(LocalDate.now().plusDays(1));

        when(order.getTotalPrice()).thenReturn(cancelAmount);

        // when
        List<Ticket> result = bookingService.cancelBooking(showId, showDateId, userId, paymentId, seatIds, cancelReason);

        // then
        assertEquals(canceledTickets, result);
        verify(paymentCommandService).createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), eq(order));
        verify(ticketCommandService).deleteTicket(user, seatIds);
        verify(showDateCommandService).countUpdateOnCancellation(showDateId, seatIds.size());
        verify(order).updateTotalPrice(BigDecimal.ZERO);
        verify(order).updateOrderStatus(OrderStatus.ORDER_CANCELED);
    }
}
