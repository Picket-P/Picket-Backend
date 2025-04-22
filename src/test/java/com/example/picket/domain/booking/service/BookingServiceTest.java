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

        when(redissonClient.getFairLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(true);

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
        verify(rLock).unlock();
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

        when(redissonClient.getFairLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(true);

        when(showDateQueryService.getShowDate(showDateId)).thenReturn(mockShowDate);
        when(userQueryService.getUser(userId)).thenReturn(mockUser);

        when(ticketCommandService.deleteTicket(mockUser, ticketIds)).thenReturn(mockTickets);

        doNothing().when(showDateCommandService).countUpdate(showDateId, seatIds.size());

        // when
        List<Ticket> result = bookingService.cancelBooking(showId, showDateId, userId, ticketIds);

        // then
        assertThat(result).isEqualTo(mockTickets);
        verify(rLock).unlock();

    }
    @Test
    void 동시에_예매할_경우_Lock을_통해_제어한다() throws InterruptedException {

        // given
        Long showId = 1L;
        Long showDateId = 1L;
        Long userId1 = 1L;
        Long userId2 = 2L;
        List<Long> seatIds1 = List.of(100L);
        List<Long> seatIds2 = List.of(101L);

        Show mockShow = mock(Show.class);
        when(mockShow.getReservationStart()).thenReturn(LocalDateTime.now().minusMinutes(10));
        when(mockShow.getReservationEnd()).thenReturn(LocalDateTime.now().plusMinutes(10));
        when(showQueryService.getShow(any())).thenReturn(mockShow);

        when(userQueryService.getUser(any())).thenReturn(mock(User.class));
        doNothing().when(seatHoldingService).seatHoldingCheck(any(), any());
        doNothing().when(ticketQueryService).checkTicketLimit(any(), any());

        when(ticketCommandService.createTicket(any(), any(), any())).thenReturn(List.of(mock(Ticket.class)));
        when(orderCommandService.createOrder(any(), any())).thenReturn(mock(Order.class));

        doNothing().when(showDateCommandService).countUpdate(any(), anyInt());
        doNothing().when(seatHoldingService).seatHoldingUnLock(any());

        RLock lock = mock(RLock.class);
        when(redissonClient.getFairLock(anyString())).thenReturn(lock);

        // 두 번 호출될 때 다른 값을 반환하게 설정 (첫 호출 true, 두 번째 false)
        when(lock.tryLock(anyLong(), any(TimeUnit.class))).thenAnswer(new Answer<Boolean>() {
            private int count = 0;
            @Override
            public Boolean answer(InvocationOnMock invocation) {
                return count++ == 0; // 첫 번째만 true
            }
        });

        // when
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<Order> future1 = executor.submit(() ->
                bookingService.booking(showId, showDateId, userId1, seatIds1));
        Future<Order> future2 = executor.submit(() ->
                bookingService.booking(showId, showDateId, userId2, seatIds2));

        int success = 0;
        int fail = 0;

        for (Future<Order> future : List.of(future1, future2)) {
            try {
                future.get(); // 성공하면 Order 반환
                success++;
            } catch (ExecutionException e) {
                // 실패는 락 획득 실패
                if (e.getCause() instanceof IllegalStateException) {
                    fail++;
                }
            }
        }

        executor.shutdown();

        // then
        assertThat(success).isEqualTo(1);
        assertThat(fail).isEqualTo(1);
        verify(lock, times(2)).tryLock(anyLong(), any(TimeUnit.class));
        verify(lock, times(1)).unlock(); // 성공한 쪽만 unlock
        verify(showDateCommandService, times(1)).countUpdate(eq(showDateId), eq(1));
    }

    @Test
    void 두개의_스레드가_Lock을_통해_booking을_순차적으로_진행하고_countUpdate가_2번_호출된다() throws Exception {
        // given
        Long showId = 1L;
        Long showDateId = 1L;
        Long userId1 = 1L;
        Long userId2 = 2L;
        List<Long> seatIds1 = List.of(100L);
        List<Long> seatIds2 = List.of(101L);

        Show mockShow = mock(Show.class);
        when(mockShow.getReservationStart()).thenReturn(LocalDateTime.now().minusMinutes(10));
        when(mockShow.getReservationEnd()).thenReturn(LocalDateTime.now().plusMinutes(10));
        when(showQueryService.getShow(any())).thenReturn(mockShow);

        when(userQueryService.getUser(any())).thenReturn(mock(User.class));

        doNothing().when(seatHoldingService).seatHoldingCheck(any(), any());
        doNothing().when(ticketQueryService).checkTicketLimit(any(), any());
        doNothing().when(showDateCommandService).countUpdate(anyLong(), anyInt());

        RLock lock = mock(RLock.class);
        when(redissonClient.getFairLock(anyString())).thenReturn(lock);

        // 순차적으로 실행되도록 설정 (두 번째 스레드는 첫 번째가 끝날 때까지 기다렸다가 true 반환)
        CountDownLatch firstLockAcquired = new CountDownLatch(1);
        CountDownLatch allowSecondThread = new CountDownLatch(1);

        when(lock.tryLock(anyLong(), any(TimeUnit.class)))
                .thenAnswer(new Answer<Boolean>() {
                    private boolean firstCall = true;

                    @Override
                    public Boolean answer(InvocationOnMock invocation) throws InterruptedException {
                        if (firstCall) {
                            firstCall = false;
                            firstLockAcquired.countDown(); // 첫 번째 스레드가 락을 잡음
                            allowSecondThread.await(); // 두 번째 스레드는 여기서 기다림
                            return true;
                        } else {
                            return true;
                        }
                    }
                });

        // 첫 번째 스레드가 unlock 호출할 때 두 번째 스레드 진행 허용
        doAnswer(invocation -> {
            allowSecondThread.countDown(); // 이제 두 번째 스레드가 tryLock 가능
            return null;
        }).when(lock).unlock();

        doReturn(List.of(mock(Ticket.class))).when(ticketCommandService).createTicket(any(), any(), any());

        doReturn(mock(Order.class)).when(orderCommandService).createOrder(any(), any());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> future1 = executor.submit(() ->
                bookingService.booking(showId, showDateId, userId1, seatIds1)
        );
        Future<?> future2 = executor.submit(() ->
                bookingService.booking(showId, showDateId, userId2, seatIds2)
        );

        future1.get();
        future2.get();

        // then
        verify(showDateCommandService, times(2)).countUpdate(showDateId, 1);
    }
}
