package com.example.picket.domain.booking.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.booking.dto.request.BookingRequest;
import com.example.picket.domain.booking.dto.request.CancelBookingRequest;
import com.example.picket.domain.booking.dto.response.CancelBookingResponse;
import com.example.picket.domain.booking.service.BookingService;
import com.example.picket.domain.order.dto.response.OrderResponse;
import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.ticket.dto.response.GetTicketResponse;
import com.example.picket.domain.ticket.entity.Ticket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;

@Controller
@RequestMapping("/api/v2/")
@RequiredArgsConstructor
@Tag(name = "예매 API", description = "예매, 예매취소 기능 API입니다.")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "예매", description = "좌석을 예매하여 티켓, 주문을 생성할 수 있습니다.")
    @PostMapping("show/{showId}/show-date/{showDateId}/booking")
    public ResponseEntity<OrderResponse> booking(
            @PathVariable Long showId,
            @PathVariable Long showDateId,
            @Auth AuthUser authUser,
            @RequestBody BookingRequest dto
            ) throws InterruptedException {
        Order order = bookingService.booking(showId, showDateId, authUser.getId(), dto.getSeatIds());
        OrderResponse orderResponse = OrderResponse.toDto(order);
        return ResponseEntity.ok(orderResponse);
    }

    @Operation(summary = "예매취소", description = "예매된 티켓을 취소할 수 있습니다.")
    @DeleteMapping("show/{showId}/show-date/{showDateId}/booking")
    public ResponseEntity<CancelBookingResponse> cancelBooking(
            @PathVariable Long showId,
            @PathVariable Long showDateId,
            @Auth AuthUser authUser,
            @RequestBody CancelBookingRequest dto
    ) throws InterruptedException {
        List<Ticket> canceledTickets = bookingService.cancelBooking(showId, showDateId, authUser.getId(), dto.getTicketIds());
        List<GetTicketResponse> getTicketResponses = canceledTickets.stream().map(GetTicketResponse::toDto).toList();
        CancelBookingResponse cancelBookingResponse = CancelBookingResponse.toDto(getTicketResponses);
        return ResponseEntity.ok(cancelBookingResponse);
    }
}
