package com.example.picket.domain.ticket.service;

import com.example.picket.common.enums.SeatStatus;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    // 티켓 성공 테스트
    @Test
    void 티켓을_성공적으로_생성할_수_있다() {
        // given
        Long userId = 1L;
        UserRole userRole = UserRole.USER;
        Long seatId = 1L;

        User user = mock(User.class);
        Show show = mock(Show.class);
        ShowDate showDate = mock(ShowDate.class);
        Seat seat = mock(Seat.class);
        Ticket ticket = mock(Ticket.class);

        when(userQueryService.getUser(userId)).thenReturn(user);
        when(seatQueryService.getSeat(seatId)).thenReturn(seat);

        when(seat.getShowDate()).thenReturn(showDate);
        when(showDate.getShow()).thenReturn(show);
        when(seat.getPrice()).thenReturn(BigDecimal.valueOf(100));
        when(seat.getSeatStatus()).thenReturn(SeatStatus.AVAILABLE);

        when(show.getReservationStart()).thenReturn(LocalDateTime.now().minusDays(1));
        when(show.getReservationEnd()).thenReturn(LocalDateTime.now().plusDays(1));
        when(show.getTicketsLimitPerUser()).thenReturn(5);

        when(ticketRepository.countTicketByUserAndShowWithTicketStatus(user, show, TicketStatus.TICKET_CREATED)).thenReturn(0);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        // when
        Ticket result = ticketCommandService.createTicket(userId, userRole, seatId);

        // then
        assertNotNull(result);
        verify(showDate).updateCountOnBooking();
        verify(seat).updateSeatStatus(SeatStatus.RESERVED);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void 티켓_생성_시_존재하지_않는_좌석에_대해_예매하려_할_경우_예외가_발생한다() {
        // given
        Long userId = 1L;
        UserRole userRole = UserRole.USER;
        Long nonExistentSeatId = 999L;

        when(seatQueryService.getSeat(nonExistentSeatId)).thenThrow(
                new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 Seat입니다.")
        );

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            ticketCommandService.createTicket(userId, userRole, nonExistentSeatId);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("존재하지 않는 Seat입니다"));

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void 티켓_생성_시_예매_시작_시간_이전일_경우_예외가_발생한다() {
        // given
        Long userId = 1L;
        UserRole userRole = UserRole.USER;
        Long seatId = 1L;

        User user = mock(User.class);
        Show show = mock(Show.class);
        ShowDate showDate = mock(ShowDate.class);
        Seat seat = mock(Seat.class);

        when(userQueryService.getUser(userId)).thenReturn(user);
        when(seatQueryService.getSeat(seatId)).thenReturn(seat);

        when(seat.getShowDate()).thenReturn(showDate);
        when(showDate.getShow()).thenReturn(show);

        when(show.getReservationStart()).thenReturn(LocalDateTime.now().plusHours(1));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            ticketCommandService.createTicket(userId, userRole, seatId);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("예매 시작 시간 전입니다"));

        verify(showDate, never()).updateCountOnBooking();
        verify(seat, never()).updateSeatStatus(any());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void 티켓_생성_시_예매_종료_시간_이후일_경우_예외가_발생한다() {
        // given
        Long userId = 1L;
        UserRole userRole = UserRole.USER;
        Long seatId = 1L;

        User user = mock(User.class);
        Show show = mock(Show.class);
        ShowDate showDate = mock(ShowDate.class);
        Seat seat = mock(Seat.class);

        when(userQueryService.getUser(userId)).thenReturn(user);
        when(seatQueryService.getSeat(seatId)).thenReturn(seat);

        when(seat.getShowDate()).thenReturn(showDate);
        when(showDate.getShow()).thenReturn(show);

        when(show.getReservationStart()).thenReturn(LocalDateTime.now().minusDays(2));
        when(show.getReservationEnd()).thenReturn(LocalDateTime.now().minusDays(1));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            ticketCommandService.createTicket(userId, userRole, seatId);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("예매 종료 시간 이후 입니다"));

        verify(showDate, never()).updateCountOnBooking();
        verify(seat, never()).updateSeatStatus(any());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void 티켓_생성_시_예매가능한_티켓_개수가_초과될_경우_예외가_발생한다() {
        // given
        Long userId = 1L;
        UserRole userRole = UserRole.USER;
        Long seatId = 1L;

        User user = mock(User.class);
        Show show = mock(Show.class);
        ShowDate showDate = mock(ShowDate.class);
        Seat seat = mock(Seat.class);

        when(userQueryService.getUser(userId)).thenReturn(user);
        when(seatQueryService.getSeat(seatId)).thenReturn(seat);

        when(seat.getShowDate()).thenReturn(showDate);
        when(showDate.getShow()).thenReturn(show);

        when(show.getReservationStart()).thenReturn(LocalDateTime.now().minusDays(1));
        when(show.getReservationEnd()).thenReturn(LocalDateTime.now().plusDays(1));
        when(show.getTicketsLimitPerUser()).thenReturn(5);

        when(ticketRepository.countTicketByUserAndShowWithTicketStatus(user, show, TicketStatus.TICKET_CREATED)).thenReturn(5); // Reached limit

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            ticketCommandService.createTicket(userId, userRole, seatId);
        });

        assertTrue(exception.getMessage().contains("예매 가능한 티켓 수를 초과합니다"));

        verify(showDate, never()).updateCountOnBooking();
        verify(seat, never()).updateSeatStatus(any());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void 티켓_생성_시_이미_예약된_좌석을_예매하려_할_경우_예외가_발생한다() {
        // given
        Long userId = 1L;
        UserRole userRole = UserRole.USER;
        Long seatId = 1L;

        User user = mock(User.class);
        Show show = mock(Show.class);
        ShowDate showDate = mock(ShowDate.class);
        Seat seat = mock(Seat.class);

        when(userQueryService.getUser(userId)).thenReturn(user);
        when(seatQueryService.getSeat(seatId)).thenReturn(seat);

        when(seat.getShowDate()).thenReturn(showDate);
        when(showDate.getShow()).thenReturn(show);

        when(show.getReservationStart()).thenReturn(LocalDateTime.now().minusDays(1));
        when(show.getReservationEnd()).thenReturn(LocalDateTime.now().plusDays(1));

        when(show.getTicketsLimitPerUser()).thenReturn(5);
        when(ticketRepository.countTicketByUserAndShowWithTicketStatus(user, show, TicketStatus.TICKET_CREATED)).thenReturn(0);

        when(seat.getSeatStatus()).thenReturn(SeatStatus.RESERVED);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            ticketCommandService.createTicket(userId, userRole, seatId);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertTrue(exception.getMessage().contains("이미 예매된 좌석입니다"));

        verify(showDate, never()).updateCountOnBooking();
        verify(seat, never()).updateSeatStatus(any());
        verify(ticketRepository, never()).save(any());
    }

    // 티켓 삭제 테스트

    @Test
    void 티켓을_성공적으로_삭제할_수_있다() {
        // given
        Long ticketId = 1L;
        Long userId = 1L;

        User user = mock(User.class);
        Show show = mock(Show.class);
        ShowDate showDate = mock(ShowDate.class);
        Seat seat = mock(Seat.class);
        Ticket ticket = mock(Ticket.class);

        when(user.getId()).thenReturn(userId);
        when(ticket.getUser()).thenReturn(user);
        when(ticket.getShow()).thenReturn(show);
        when(ticket.getSeat()).thenReturn(seat);
        when(seat.getId()).thenReturn(1L);
        when(ticket.getStatus()).thenReturn(TicketStatus.TICKET_CREATED);

        when(ticketRepository.findByTicketId(ticketId)).thenReturn(Optional.of(ticket));
        when(showDateQueryService.getShowDateByShow(show)).thenReturn(showDate);
        when(seatQueryService.getSeat(1L)).thenReturn(seat);

        when(showDate.getDate()).thenReturn(LocalDate.now().plusDays(1)); // Future show date

        // when
        Ticket result = ticketCommandService.deleteTicket(ticketId, userId);

        // then
        assertNotNull(result);

        verify(ticket).updateTicketStatus(TicketStatus.TICKET_CANCELED);
        verify(showDate).updateCountOnCancellation();
        verify(seat).updateSeatStatus(SeatStatus.AVAILABLE);
        verify(ticket).updateTicketStatus(TicketStatus.TICKET_EXPIRED);
        verify(ticket).updateDeletedAt(any(LocalDateTime.class));
    }

    @Test
    void 티켓_삭제_시_존재하지_않는_티켓을_삭제하려_할_경우_예외가_발생한다() {
        // given
        Long nonExistentTicketId = 999L;
        Long userId = 1L;

        when(ticketRepository.findByTicketId(nonExistentTicketId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            ticketCommandService.deleteTicket(nonExistentTicketId, userId);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("존재하지 않는 Ticket입니다"));

        verify(showDateQueryService, never()).getShowDateByShow(any());
        verify(seatQueryService, never()).getSeat(anyLong());
    }


    @Test
    void 티켓_삭제_시_공연_날짜_이후에_티켓을_삭제하려_할_경우_예외가_발생한다() {
        // given
        Long ticketId = 1L;
        Long userId = 1L;

        Show show = mock(Show.class);
        ShowDate showDate = mock(ShowDate.class);
        Seat seat = mock(Seat.class);
        Ticket ticket = mock(Ticket.class);

        when(ticket.getShow()).thenReturn(show);
        when(ticket.getSeat()).thenReturn(seat);
        when(seat.getId()).thenReturn(1L);

        when(ticketRepository.findByTicketId(ticketId)).thenReturn(Optional.of(ticket));
        when(showDateQueryService.getShowDateByShow(show)).thenReturn(showDate);
        when(seatQueryService.getSeat(1L)).thenReturn(seat);

        when(showDate.getDate()).thenReturn(LocalDate.now().minusDays(1)); // Past show date

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            ticketCommandService.deleteTicket(ticketId, userId);
        });

        assertTrue(exception.getMessage().contains("공연 시작 날짜 이전에만 취소 가능합니다"));

        verify(showDate, never()).updateCountOnCancellation();
        verify(seat, never()).updateSeatStatus(any());
        verify(ticket, never()).updateTicketStatus(any());
        verify(ticket, never()).updateDeletedAt(any());
    }

    @Test
    void 티켓_삭제_시_이미_TicketStatus가_EXPIRED인_티켓을_삭제시도할_경우_예외가_발생한다() {
        // Arrange
        Long ticketId = 1L;
        Long userId = 1L;

        Show show = mock(Show.class);
        ShowDate showDate = mock(ShowDate.class);
        Seat seat = mock(Seat.class);
        Ticket ticket = mock(Ticket.class);

        when(ticket.getShow()).thenReturn(show);
        when(ticket.getSeat()).thenReturn(seat);
        when(seat.getId()).thenReturn(1L);

        when(ticketRepository.findByTicketId(ticketId)).thenReturn(Optional.of(ticket));
        when(showDateQueryService.getShowDateByShow(show)).thenReturn(showDate);
        when(seatQueryService.getSeat(1L)).thenReturn(seat);

        when(showDate.getDate()).thenReturn(LocalDate.now().plusDays(1)); // Future show date
        when(ticket.getStatus()).thenReturn(TicketStatus.TICKET_EXPIRED); // Already expired

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            ticketCommandService.deleteTicket(ticketId, userId);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertTrue(exception.getMessage().contains("이미 취소된 티켓입니다"));

        verify(showDate, never()).updateCountOnCancellation();
        verify(seat, never()).updateSeatStatus(any());
        verify(ticket, never()).updateTicketStatus(any());
        verify(ticket, never()).updateDeletedAt(any());
    }

    @Test
    void 티켓_삭제_시_본인이_예매한_티켓이_아닐_경우_예외가_발생한다() {
        // given
        Long ticketId = 1L;
        Long userId = 1L;         // 요청하는 사용자 ID
        Long ticketOwnerId = 2L;  // 실제 티켓 소유자 ID (다른 사용자)

        User ticketOwner = mock(User.class);
        Show show = mock(Show.class);
        ShowDate showDate = mock(ShowDate.class);
        Seat seat = mock(Seat.class);
        Ticket ticket = mock(Ticket.class);

        // 티켓 소유자 설정 (요청자와 다른 사용자)
        when(ticketOwner.getId()).thenReturn(ticketOwnerId);

        when(ticket.getUser()).thenReturn(ticketOwner);
        when(ticket.getShow()).thenReturn(show);
        when(ticket.getSeat()).thenReturn(seat);
        when(seat.getId()).thenReturn(1L);

        when(ticketRepository.findByTicketId(ticketId)).thenReturn(Optional.of(ticket));
        when(showDateQueryService.getShowDateByShow(show)).thenReturn(showDate);
        when(seatQueryService.getSeat(1L)).thenReturn(seat);

        when(showDate.getDate()).thenReturn(LocalDate.now().plusDays(1));

        when(ticket.getStatus()).thenReturn(TicketStatus.TICKET_CREATED);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            ticketCommandService.deleteTicket(ticketId, userId);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertTrue(exception.getMessage().contains("예매자 본인만 취소할 수 있습니다"));

        verify(showDate, never()).updateCountOnCancellation();
        verify(seat, never()).updateSeatStatus(any());
        verify(ticket, never()).updateTicketStatus(any());
        verify(ticket, never()).updateDeletedAt(any());
    }


}