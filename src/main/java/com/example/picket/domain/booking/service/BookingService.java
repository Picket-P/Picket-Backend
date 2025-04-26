package com.example.picket.domain.booking.service;

import com.example.picket.common.enums.OrderStatus;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.order.service.OrderCommandService;
import com.example.picket.domain.payment.entity.Payment;
import com.example.picket.domain.payment.service.PaymentService;
import com.example.picket.domain.seat.service.SeatQueryService;
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
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final SeatHoldingService seatHoldingService;

    private final ShowQueryService showQueryService;
    private final UserQueryService userQueryService;
    private final ShowDateQueryService showDateQueryService;
    private final TicketQueryService ticketQueryService;

    private final OrderCommandService orderCommandService;
    private final TicketCommandService ticketCommandService;
    private final ShowDateCommandService showDateCommandService;
    private final PaymentService paymentService;

    private final StringRedisTemplate stringRedisTemplate;
    private static final String KEY_PREFIX = "PAYMENT-INFO:USER:";
    private static final String SECRET_KEY = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
    private static final String TOSS_API_URL = "https://api.tosspayments.com/v1/payments/confirm";

    @Transactional
    public Order booking(Long showId, Long showDateId, Long userId, List<Long> seatIds, String paymentKey, String orderId, Number amount) throws InterruptedException {

        String key = KEY_PREFIX + userId;
        //orderId, amount 비교
        String orderIdFromRedis = (String) stringRedisTemplate.opsForHash().get(key, "orderId");
        String amountFromRedis = (String) stringRedisTemplate.opsForHash().get(key, "amount");

        System.out.println(orderIdFromRedis);
        System.out.println(amountFromRedis);
        System.out.println(amount.toString());

        // orderId 검증
        if (orderIdFromRedis == null || !orderIdFromRedis.equals(orderId)) {
            throw new CustomException(BAD_REQUEST, "결제 인증 정보(orderId)가 변경되었습니다.");
        }


        if (amountFromRedis == null || !amountFromRedis.equals(amount.toString())) {
            throw new CustomException(BAD_REQUEST, "결제 인증 정보(amount)가 변경되었습니다.");
        }

        // 승인 API 호출
        String encodedSecretKey = "Basic " + Base64.getEncoder().encodeToString((SECRET_KEY + ":").getBytes());

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 바디 설정
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", orderId);
        requestBody.put("amount", amount);
        requestBody.put("paymentKey", paymentKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        // API 호출
        ResponseEntity<Map> response = restTemplate.exchange(
                TOSS_API_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            // 결제 성공 비즈니스 로직 구현
            Map<String, Object> responseBody = response.getBody();
            String tossPaymentKey = (String) responseBody.get("paymentKey");
            String tossOrderId = (String) responseBody.get("orderId");
            String tossOrderName = (String) responseBody.get("orderName");
            String tossStatus = (String) responseBody.get("status");
            Number tossTotalAmount = (Number) responseBody.get("totalAmount");

            Show foundShow = showQueryService.getShow(showId); // select
            checkBookingTime(foundShow);

            User foundUser = userQueryService.getUser(userId); // select

            seatHoldingService.seatHoldingCheck(userId, seatIds);

            ticketQueryService.checkTicketLimit(foundUser, foundShow); // select

            List<Ticket> tickets = ticketCommandService.createTicket(foundUser, foundShow, seatIds); // insert

            Order order = orderCommandService.createOrder(foundUser, tickets); // insert

            paymentService.create(tossPaymentKey, tossOrderId, tossOrderName, tossStatus, tossTotalAmount, order);

            showDateCommandService.countUpdate(showDateId, seatIds.size()); // update

            seatHoldingService.seatHoldingUnLock(seatIds);

            return order;
        } else {

            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "결제 승인에 실패하였습니다.");
        }
    }

    @Transactional
    public List<Ticket> cancelBooking(Long showId, Long showDateId, Long userId, List<Long> ticketIds) throws InterruptedException {
        ShowDate foundShowDate = showDateQueryService.getShowDate(showDateId);
        checkCancelBookingTime(foundShowDate);

        User foundUser = userQueryService.getUser(userId);
        List<Ticket> canceledTickets = ticketCommandService.deleteTicket(foundUser, ticketIds);

        showDateCommandService.countUpdate(showDateId, ticketIds.size());

        return canceledTickets;
    }

    private void checkBookingTime(Show show) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(show.getReservationStart())) {
            throw new CustomException(BAD_REQUEST, "예매 시작 시간 전입니다.");
        }

        if (now.isAfter(show.getReservationEnd())) {
            throw new CustomException(BAD_REQUEST, "예매 종료 시간 이후 입니다.");
        }
    }

    private void checkCancelBookingTime(ShowDate showDate) {
        if (LocalDate.now().isAfter(showDate.getDate())) {
            throw new CustomException(BAD_REQUEST, "공연 시작 날짜 이전에만 취소 가능합니다.");
        }
    }
}
