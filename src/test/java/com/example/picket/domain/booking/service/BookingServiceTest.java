package com.example.picket.domain.booking.service;

import com.example.picket.common.enums.*;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.order.service.OrderCommandService;
import com.example.picket.domain.seat.entity.Seat;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private RedissonClient redissonClient;
    @Mock private RLock rLock;
    @Mock private SeatHoldingService seatHoldingService;
    @Mock private ShowQueryService showQueryService;
    @Mock private UserQueryService userQueryService;
    @Mock private ShowDateQueryService showDateQueryService;
    @Mock private TicketQueryService ticketQueryService;
    @Mock private OrderCommandService orderCommandService;
    @Mock private TicketCommandService ticketCommandService;
    @Mock private ShowDateCommandService showDateCommandService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void 예매에_성공한다() throws InterruptedException {
        // given
        Long showId = 1L;
        Long showDateId = 1L;
        Long userId = 1L;
        List<Long> seatIds = List.of(10L, 11L);

        Show mockShow = mock(Show.class);
        when(mockShow.getReservationStart()).thenReturn(LocalDateTime.now().minusHours(1));
        when(mockShow.getReservationEnd()).thenReturn(LocalDateTime.now().plusHours(1));

        User mockUser = mock(User.class);
        List<Ticket> mockTickets = List.of(mock(Ticket.class));
        Order mockOrder = mock(Order.class);

        when(showQueryService.getShow(showId)).thenReturn(mockShow);
        when(userQueryService.getUser(userId)).thenReturn(mockUser);

        doNothing().when(seatHoldingService).seatHoldingCheck(userId, seatIds);
        doNothing().when(ticketQueryService).checkTicketLimit(mockUser, mockShow);

        when(ticketCommandService.createTicket(mockUser, mockShow, seatIds)).thenReturn(mockTickets);
        when(orderCommandService.createOrder(mockUser, mockTickets)).thenReturn(mockOrder);

        doNothing().when(showDateCommandService).countUpdate(showDateId, seatIds.size());
        doNothing().when(seatHoldingService).seatHoldingUnLock(seatIds);

        // when
        Order result = bookingService.booking(showId, showDateId, userId, seatIds);

        // then
        assertThat(result).isEqualTo(mockOrder);
    }

    @Test
    void 예매취소에_성공한다() throws InterruptedException {
        // given
        Long showId = 1L;
        Long showDateId = 1L;
        Long userId = 1L;
        List<Long> seatIds = List.of(10L, 11L);
        List<Long> ticketIds = List.of(1L, 2L);

        ShowDate mockShowDate = mock(ShowDate.class);
        when(mockShowDate.getDate()).thenReturn(LocalDate.now().plusDays(1));

        User mockUser = mock(User.class);
        List<Ticket> mockTickets = List.of(mock(Ticket.class));

        when(showDateQueryService.getShowDate(showDateId)).thenReturn(mockShowDate);
        when(userQueryService.getUser(userId)).thenReturn(mockUser);

        when(ticketCommandService.deleteTicket(mockUser, ticketIds)).thenReturn(mockTickets);

        doNothing().when(showDateCommandService).countUpdate(showDateId, seatIds.size());

        // when
        List<Ticket> result = bookingService.cancelBooking(showId, showDateId, userId, ticketIds);

        // then
        assertThat(result).isEqualTo(mockTickets);
    }
}
