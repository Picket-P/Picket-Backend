package com.example.picket.domain.booking.service;

import com.example.picket.common.enums.OrderStatus;
import com.example.picket.common.exception.CustomException;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

    private final StringRedisTemplate stringRedisTemplate;

    private static final String SECRET_KEY = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
    private static final String TOSS_PAY_BASE_URL = "https://api.tosspayments.com/v1/payments/";
    private static final String PAYMENT_INFO_KEY_PREFIX = "PAYMENT-INFO:USER:";


    @Transactional
    public Order booking(Long showId, Long showDateId, Long userId, List<Long> seatIds, String paymentKey, String orderId, Number amount) throws InterruptedException {

        // Redis에서 결제 인증 정보(orderId, amount) 가져오기
        String key = PAYMENT_INFO_KEY_PREFIX + userId;
        String orderIdFromRedis = (String) stringRedisTemplate.opsForHash().get(key, "orderId");
        String amountFromRedis = (String) stringRedisTemplate.opsForHash().get(key, "amount");

        // Redis에 저장된 orderId와 요청값(orderId)이 다르면 예외 발생
        if (orderIdFromRedis == null || !orderIdFromRedis.equals(orderId)) {
            throw new CustomException(BAD_REQUEST, "결제 인증 정보(orderId)가 변경되었습니다.");
        }

        // Redis에 저장된 amount와 요청값(amount)이 다르면 예외 발생
        if (amountFromRedis == null || !amountFromRedis.equals(amount.toString())) {
            throw new CustomException(BAD_REQUEST, "결제 인증 정보(amount)가 변경되었습니다.");
        }

        // 토스페이 결제 승인 요청 준비
        String TOSS_PAY_CONFIRM_URL =TOSS_PAY_BASE_URL + "/confirm";
        String encodedSecretKey = "Basic " + Base64.getEncoder().encodeToString((SECRET_KEY + ":").getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", orderId);
        requestBody.put("amount", amount);
        requestBody.put("paymentKey", paymentKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        // 토스페이에 결제 승인 요청 API 호출
        ResponseEntity<Map> response = restTemplate.exchange(
                TOSS_PAY_CONFIRM_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {

            // 결제 승인이 완료되었으므로 Redis에 저장된 인증정보 삭제
            stringRedisTemplate.delete(key);

            // 결제 승인 응답 데이터 파싱
            Map<String, Object> responseBody = response.getBody();
            String tossPaymentKey = (String) responseBody.get("paymentKey");
            String tossOrderId = (String) responseBody.get("orderId");
            String tossOrderName = (String) responseBody.get("orderName");
            String tossStatus = (String) responseBody.get("status");
            Number totalAmount = (Number) responseBody.get("totalAmount");
            BigDecimal tossTotalAmount = new BigDecimal(totalAmount.toString());

            // 예매 로직
            Show foundShow = showQueryService.getShow(showId);
            checkBookingTime(foundShow);

            User foundUser = userQueryService.getUser(userId);

            seatHoldingService.seatHoldingCheck(userId, seatIds);

            ticketQueryService.checkTicketLimit(foundUser, foundShow);

            List<Ticket> tickets = ticketCommandService.createTicket(foundUser, foundShow, seatIds);

            Order order = orderCommandService.createOrder(foundUser, tickets);

            paymentCommandService.create(tossPaymentKey, tossOrderId, tossOrderName, tossStatus, tossTotalAmount, order);

            showDateCommandService.countUpdateOnBooking(showDateId, seatIds.size());

            seatHoldingService.seatHoldingUnLock(seatIds);

            return order;
        } else {

            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "결제 승인에 실패하였습니다.");
        }
    }

    @Transactional
    public List<Ticket> cancelBooking(Long showId, Long showDateId, Long userId, Long paymentId, List<Long> ticketIds, String cancelReason) throws InterruptedException {

        // 취소할 티켓들의 총 가격 계산
        BigDecimal amountToCancel = ticketQueryService.addUpTicketPrice(ticketIds);

        // 결제 정보 조회
        Payment payment = paymentQueryService.getPayment(paymentId);
        String foundTossPaymentKey = payment.getTossPaymentKey();
        Order foundOrder = payment.getOrder();

        // 토스페이 결제 취소 요청 준비
        String TOSS_PAY_CANCEL_URL = TOSS_PAY_BASE_URL + foundTossPaymentKey + "/cancel";
        String encodedSecretKey = "Basic " + Base64.getEncoder().encodeToString((SECRET_KEY + ":").getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cancelReason", cancelReason);
        requestBody.put("cancelAmount", (Number) amountToCancel);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        // 토스페이에 결제 취소 요청 API 호출
        ResponseEntity<Map> response = restTemplate.exchange(
                TOSS_PAY_CANCEL_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );

        // 결제 취소 응답 데이터 파싱
        Map<String, Object> responseBody = response.getBody();
        String tossPaymentKey = (String) responseBody.get("paymentKey");
        String tossOrderId = (String) responseBody.get("orderId");
        String tossOrderName = (String) responseBody.get("orderName");
        String tossStatus = (String) responseBody.get("status");
        List<Map<String, Object>> cancels = (List<Map<String, Object>>) responseBody.get("cancels");

        // cancels 리스트에서 이번 요청의 실제 취소된 금액 가져오기
        BigDecimal cancelAmount = BigDecimal.ZERO;
        if (cancels != null && !cancels.isEmpty()) {
            Map<String, Object> firstCancel = cancels.get(cancels.size()-1);
            Number cancelAmountNumber = (Number) firstCancel.get("cancelAmount");
            cancelAmount = new BigDecimal(cancelAmountNumber.toString());
        }

        // Order 갱신
        BigDecimal foundTotalPrice = foundOrder.getTotalPrice();
        BigDecimal totalPriceToSave = foundTotalPrice.subtract(cancelAmount);

        if (totalPriceToSave.compareTo(BigDecimal.ZERO) == 0) {
            foundOrder.updateTotalPrice(totalPriceToSave);
            foundOrder.updateOrderStatus(OrderStatus.ORDER_CANCELED);
        } else {
            foundOrder.updateTotalPrice(totalPriceToSave);
        }

        // 예매 취소 로직
        paymentCommandService.create(tossPaymentKey, tossOrderId, tossOrderName, tossStatus, cancelAmount, foundOrder);

        ShowDate foundShowDate = showDateQueryService.getShowDate(showDateId);
        checkCancelBookingTime(foundShowDate);

        User foundUser = userQueryService.getUser(userId);
        List<Ticket> canceledTickets = ticketCommandService.deleteTicket(foundUser, ticketIds);

        showDateCommandService.countUpdateOnCancellation(showDateId,ticketIds.size());

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
