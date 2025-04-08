package com.example.picket.domain.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.picket.common.enums.TicketStatus;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.service.SeatQueryService;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.service.ShowDateQueryService;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.ticket.repository.TicketRepository;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserQueryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketCommandServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private UserQueryService userQueryService;
    @Mock
    private SeatQueryService seatQueryService;
    @Mock
    private ShowDateQueryService showDateQueryService;

    @InjectMocks
    private TicketCommandService ticketCommandService;

    private final Long userId = 1L;
    private final Long seatId = 2L;
    private final Long ticketId = 3L;

    @Test
    void 티켓을_생성할_수_있다() {

        // given
        Seat seat = mock(Seat.class);
        Show show = mock(Show.class);
        ShowDate showDate = mock(ShowDate.class);
        User user = mock(User.class);

        when(seatQueryService.getSeat(seatId)).thenReturn(seat);
        when(seat.getShowDate()).thenReturn(showDate);
        when(showDate.getShow()).thenReturn(show);
        when(show.getReservationStart()).thenReturn(LocalDateTime.now().minusHours(1));
        when(show.getReservationEnd()).thenReturn(LocalDateTime.now().plusHours(1));
        when(ticketRepository.existsBySeat(seat)).thenReturn(false);
        when(userQueryService.getUser(userId)).thenReturn(user);
        when(seat.getPrice()).thenReturn(BigDecimal.valueOf(10000));
        when(seat.getShowDate()).thenReturn(showDate);

        Ticket ticket = mock(Ticket.class);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        // when
        Ticket result = ticketCommandService.createTicket(userId, UserRole.USER, seatId);

        // then
        assertThat(result).isNotNull();
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void 티켓_생성에_성공하면_남은_좌석_수를_discount_한다() {
        // given
        Seat seat = mock(Seat.class);
        Show show = mock(Show.class);
        ShowDate showDate = mock(ShowDate.class);
        User user = mock(User.class);

        when(seatQueryService.getSeat(seatId)).thenReturn(seat);
        when(seat.getShowDate()).thenReturn(showDate);
        when(showDate.getShow()).thenReturn(show);
        when(show.getReservationStart()).thenReturn(LocalDateTime.now().minusHours(1));
        when(show.getReservationEnd()).thenReturn(LocalDateTime.now().plusHours(1));
        when(ticketRepository.existsBySeat(seat)).thenReturn(false);
        when(userQueryService.getUser(userId)).thenReturn(user);
        when(seat.getPrice()).thenReturn(BigDecimal.valueOf(10000));

        Ticket ticket = mock(Ticket.class);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        // when
        ticketCommandService.createTicket(userId, UserRole.USER, seatId);

        // then
        verify(showDate, times(1)).discountRemainCount();
    }

    @Test
    void 이미_예매된_좌석으로_티켓을_생성할_시_예외를_던진다() {

        // given
        Seat seat = mock(Seat.class);
        when(seatQueryService.getSeat(seatId)).thenReturn(seat);
        when(seat.getShowDate()).thenReturn(mock(ShowDate.class));
        when(seat.getShowDate().getShow()).thenReturn(mock(Show.class));
        when(seat.getShowDate().getShow().getReservationStart()).thenReturn(LocalDateTime.now().minusDays(1));
        when(seat.getShowDate().getShow().getReservationEnd()).thenReturn(LocalDateTime.now().plusDays(1));
        when(ticketRepository.existsBySeat(seat)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> ticketCommandService.createTicket(userId, UserRole.USER, seatId))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 예매된 좌석입니다.");
    }

    @Test
    void 예매_시작_시간_이전에_예매할_경우_예외를_던진다() {
        // given
        Seat seat = mock(Seat.class);
        Show show = mock(Show.class);
        ShowDate showDate = mock(ShowDate.class);

        when(seatQueryService.getSeat(seatId)).thenReturn(seat);
        when(seat.getShowDate()).thenReturn(showDate);
        when(showDate.getShow()).thenReturn(show);

        // 예약 시작 시간 이후가 아님
        when(show.getReservationStart()).thenReturn(LocalDateTime.now().plusHours(1));

        // when & then
        assertThatThrownBy(() -> ticketCommandService.createTicket(userId, UserRole.USER, seatId))
                .isInstanceOf(CustomException.class)
                .hasMessage("예매 시작 시간 전입니다.");
    }

    @Test
    void 예매_종료_시간_이후에_예매할_경우_예외를_던진다() {
        // given
        Seat seat = mock(Seat.class);
        Show show = mock(Show.class);
        ShowDate showDate = mock(ShowDate.class);

        when(seatQueryService.getSeat(seatId)).thenReturn(seat);
        when(seat.getShowDate()).thenReturn(showDate);
        when(showDate.getShow()).thenReturn(show);

        // 예약 종료 시간 초과
        when(show.getReservationStart()).thenReturn(LocalDateTime.now().minusHours(3));
        when(show.getReservationEnd()).thenReturn(LocalDateTime.now().minusHours(1));

        // when / then
        assertThatThrownBy(() -> ticketCommandService.createTicket(userId, UserRole.USER, seatId))
                .isInstanceOf(CustomException.class)
                .hasMessage("예매 종료 시간 이후 입니다.");
    }

    @Test
    void 티켓을_삭제할_수_있다() {

        // given
        Ticket ticket = mock(Ticket.class);
        Show show = mock(Show.class);
        ShowDate showDate = mock(ShowDate.class);
        User user = mock(User.class);

        when(ticket.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(ticketRepository.findByTicketId(ticketId)).thenReturn(Optional.of(ticket));
        when(ticket.getShow()).thenReturn(show);
        when(showDateQueryService.getShowDateByShow(show)).thenReturn(showDate);
        when(showDate.getDate()).thenReturn(LocalDate.now().plusDays(1));

        // when
        Ticket result = ticketCommandService.deleteTicket(ticketId, userId);

        // then
        assertThat(result).isEqualTo(ticket);
        verify(ticket).updateTicketStatus(TicketStatus.TICKET_CANCELED);
        verify(ticket).updateTicketStatus(TicketStatus.TICKET_EXPIRED);
        verify(ticket).updateDeletedAt(any(LocalDateTime.class));

    }

    @Test
    void 본인이_예매하지_않은_티켓을_삭제할_시_예외를_던진다() {
        // given
        Ticket ticket = mock(Ticket.class);
        User user = mock(User.class);

        when(ticket.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(999L); // 다른 사용자 ID
        when(ticketRepository.findByTicketId(ticketId)).thenReturn(Optional.of(ticket));

        // when / then
        assertThatThrownBy(() -> ticketCommandService.deleteTicket(ticketId, userId))
                .isInstanceOf(CustomException.class)
                .hasMessage("예매자 본인만 취소할 수 있습니다.");
    }

}