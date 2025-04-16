package com.example.picket.domain.booking.service;

import com.example.picket.common.enums.*;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.order.service.OrderCommandService;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat_holding.service.SeatHoldingService;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.service.ShowDateQueryService;
import com.example.picket.domain.show.service.ShowQueryService;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.ticket.service.TicketCommandService;
import com.example.picket.domain.ticket.service.TicketQueryService;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock rLock;

    @Mock private SeatHoldingService seatHoldingService;
    @Mock private ShowQueryService showQueryService;
    @Mock private UserQueryService userQueryService;
    @Mock private ShowDateQueryService showDateQueryService;
    @Mock private OrderCommandService orderCommandService;
    @Mock private TicketCommandService ticketCommandService;
    @Mock private TicketQueryService ticketQueryService;

    private User user;
    private Show show;
    private Seat seat;
    private ShowDate showDate;

    @BeforeEach
    void setUp() {
        user = User.toEntity(
                "user@example.com", "encodedPw", UserRole.USER, null, "nickname",
                LocalDate.of(1990, 1, 1), Gender.MALE
        );

        show = Show.toEntity(
                1L, "Show Title", "http://poster.url", Category.CONCERT, "Description",
                "Location", LocalDateTime.now(), LocalDateTime.now().plusDays(1), 4
        );

        showDate = ShowDate.toEntity(LocalDate.now().plusDays(7), LocalTime.of(19, 0), LocalTime.of(20, 0), 1, 0, show);

        seat = Seat.toEntity(Grade.A, 1, BigDecimal.valueOf(1000L), showDate);
    }

    @Test
    void 성공적으로_예매할_수_있다() throws InterruptedException {
        // given
        Long showId = 1L;
        Long showDateId = 1L;
        Long userId = 1L;
        List<Long> seatIds = Arrays.asList(1L);
        BigDecimal price = BigDecimal.valueOf(100L);
        List<Ticket> tickets = Arrays.asList(Ticket.toEntity(user, show, seat, price, TicketStatus.TICKET_CREATED));
        Order order = Order.toEntity(user, price, OrderStatus.ORDER_COMPLETE, tickets);
        order.updateOrderStatus(OrderStatus.ORDER_COMPLETE);

        when(showQueryService.getShow(showId)).thenReturn(show);
        when(userQueryService.getUser(userId)).thenReturn(user);
        when(showDateQueryService.getShowDate(showDateId)).thenReturn(showDate);
        when(ticketCommandService.createTicket(user, show, seatIds)).thenReturn(tickets);
        when(orderCommandService.createOrder(user, tickets)).thenReturn(order);
        when(redissonClient.getFairLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), any())).thenReturn(true);

        // when
        Order result = bookingService.booking(showId, showDateId, userId, seatIds);

        // then
        assertNotNull(result);
        assertEquals(OrderStatus.ORDER_COMPLETE, result.getOrderStatus());
        verify(seatHoldingService).seatHoldingCheck(userId, seatIds);
        verify(ticketQueryService).checkTicketLimit(user, show);
        verify(ticketCommandService).createTicket(user, show, seatIds);
        verify(orderCommandService).createOrder(user, tickets);
        verify(seatHoldingService).seatHoldingUnLock(seatIds);
        verify(rLock).unlock();
    }

    @Test
    void 예매_시작_시간_전_예매_시_예외가_발생한다() {
        // given
        Show show = mock(Show.class);
        Long showId = 1L;
        Long showDateId = 1L;
        Long userId = 1L;
        List<Long> seatIds = Arrays.asList(1L);
        when(show.getReservationStart()).thenReturn(LocalDateTime.now().plusDays(1));
        when(showQueryService.getShow(showId)).thenReturn(show);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () ->
                bookingService.booking(showId, showDateId, userId, seatIds));
        assertEquals("예매 시작 시간 전입니다.", exception.getMessage());
        verifyNoInteractions(seatHoldingService, ticketQueryService, ticketCommandService, orderCommandService);
    }

    @Test
    void booking_AfterReservationEnd_ThrowsException() {
        // Arrange
        Show show = mock(Show.class);
        Long showId = 1L;
        Long showDateId = 1L;
        Long userId = 1L;
        List<Long> seatIds = Arrays.asList(1L);
        when(show.getReservationStart()).thenReturn(LocalDateTime.now().minusDays(2));
        when(show.getReservationEnd()).thenReturn(LocalDateTime.now().minusDays(1));
        when(showQueryService.getShow(showId)).thenReturn(show);


        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () ->
                bookingService.booking(showId, showDateId, userId, seatIds));
        assertEquals("예매 종료 시간 이후 입니다.", exception.getMessage());
        verifyNoInteractions(seatHoldingService, ticketQueryService, ticketCommandService, orderCommandService);
    }

    @Test
    void 성공적으로_예매취소_할_수_있다() throws InterruptedException {
        // given
        Long showId = 1L;
        Long showDateId = 1L;
        Long userId = 1L;
        List<Long> ticketIds = Arrays.asList(1L, 2L);
        BigDecimal price = BigDecimal.valueOf(100L);
        List<Ticket> canceledTickets = Arrays.asList(Ticket.toEntity(user, show, seat, price, TicketStatus.TICKET_CREATED));

        when(showDateQueryService.getShowDate(showDateId)).thenReturn(showDate);
        when(userQueryService.getUser(userId)).thenReturn(user);
        when(ticketCommandService.deleteTicket(user, ticketIds)).thenReturn(canceledTickets);
        when(redissonClient.getFairLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), any())).thenReturn(true);

        // when
        List<Ticket> result = bookingService.cancelBooking(showId, showDateId, userId, ticketIds);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(showDateQueryService, times(2)).getShowDate(showDateId);
        verify(userQueryService).getUser(userId);
        verify(ticketCommandService).deleteTicket(user, ticketIds);
        verify(rLock).unlock();
    }

    @Test
    void 공연날짜_이후_예매_취소_시_예외가_발생한다() {
        // given
        Long showId = 1L;
        Long showDateId = 1L;
        Long userId = 1L;
        List<Long> ticketIds = Arrays.asList(1L, 2L);
        ShowDate pastShowDate = ShowDate.toEntity(
                LocalDate.now().minusDays(1), LocalTime.of(19, 0), LocalTime.of(20, 0), 100, 0, show
        );

        when(showDateQueryService.getShowDate(showDateId)).thenReturn(pastShowDate);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () ->
                bookingService.cancelBooking(showId, showDateId, userId, ticketIds));
        assertEquals("공연 시작 날짜 이전에만 취소 가능합니다.", exception.getMessage());
        verifyNoInteractions(userQueryService, ticketCommandService);
    }
}