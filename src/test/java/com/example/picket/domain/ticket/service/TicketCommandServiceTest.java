package com.example.picket.domain.ticket.service;

import com.example.picket.common.enums.*;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketCommandServiceTest {

    @InjectMocks
    private TicketCommandService ticketCommandService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private SeatQueryService seatQueryService;

    private User user;
    private Show show;
    private Seat seat;

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

        seat = mock(Seat.class);
        lenient().when(seat.getPrice()).thenReturn(BigDecimal.valueOf(10000));
    }

    @Test
    void 티켓을_정상적으로_생성할_수_있다() {
        Long seatId = 1L;

        when(seatQueryService.getSeat(seatId)).thenReturn(seat);
        when(ticketRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Ticket> tickets = ticketCommandService.createTicket(user, show, List.of(seatId));

        assertEquals(1, tickets.size());
        assertEquals(BigDecimal.valueOf(10000), tickets.get(0).getPrice());
        verify(seat).updateSeatStatus(SeatStatus.RESERVED);
    }

    @Test
    void 티켓을_정상적으로_삭제할_수_있다() {
        Ticket ticket = mock(Ticket.class);
        when(ticket.getUser()).thenReturn(user);
        when(ticket.getStatus()).thenReturn(TicketStatus.TICKET_CREATED);
        when(ticket.getSeat()).thenReturn(seat);
        when(ticketRepository.findByTicketId(1L)).thenReturn(Optional.of(ticket));

        List<Ticket> result = ticketCommandService.deleteTicket(user, List.of(1L));

        assertEquals(1, result.size());
        verify(ticket).updateTicketStatus(TicketStatus.TICKET_CANCELED);
        verify(seat).updateSeatStatus(SeatStatus.AVAILABLE);
    }

    @Test
    void 티켓_삭제_시_존재하지_않는_티켓을_삭제하려_할_경우_예외가_발생한다() {
        when(ticketRepository.findByTicketId(anyLong())).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> ticketCommandService.deleteTicket(user, List.of(99L)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("존재하지 않는 티켓입니다.", exception.getMessage());
    }

    @Test
    void 티켓_삭제_시_본인이_예매하지_않은_티켓을_삭제하려_할_경우_예외가_발생한다() {
        User anotherUser = User.toEntity(
                "other@example.com", "pw", UserRole.USER, null, "other",
                LocalDate.of(1991, 2, 2), Gender.FEMALE
        );
        Ticket ticket = mock(Ticket.class);
        Seat seat = mock(Seat.class);

        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(anotherUser, "id", 2L);

        when(ticket.getUser()).thenReturn(anotherUser);
        when(ticket.getStatus()).thenReturn(TicketStatus.TICKET_CREATED);
        when(ticketRepository.findByTicketId(anyLong())).thenReturn(Optional.of(ticket));

        CustomException exception = assertThrows(CustomException.class,
                () -> ticketCommandService.deleteTicket(user, List.of(1L)));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("예매자 본인만 취소할 수 있습니다.", exception.getMessage());
    }

    @Test
    void 티켓_삭제_시_이미_삭제된_티켓을_삭제하려_할_경우_예외가_발생한다() {
        Ticket ticket = mock(Ticket.class);
        when(ticket.getStatus()).thenReturn(TicketStatus.TICKET_CANCELED);
        when(ticketRepository.findByTicketId(anyLong())).thenReturn(Optional.of(ticket));

        CustomException exception = assertThrows(CustomException.class,
                () -> ticketCommandService.deleteTicket(user, List.of(1L)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("이미 취소된 티켓입니다.", exception.getMessage());
    }




}