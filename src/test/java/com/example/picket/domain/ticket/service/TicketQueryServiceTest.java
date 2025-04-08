package com.example.picket.domain.ticket.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.ticket.repository.TicketRepository;
import com.example.picket.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketQueryServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketQueryService ticketQueryService;

    @Test
    void 티켓을_다건_조회할_수_있다() {

        // given
        Long userId = 1L;
        int size = 10;
        int page = 1;

        Ticket ticket = mock(Ticket.class);
        Page<Ticket> ticketPage = new PageImpl<>(List.of(ticket));

        when(ticketRepository.findByUser(eq(userId), any(Pageable.class)))
                .thenReturn(ticketPage);

        // when
        Page<Ticket> result = ticketQueryService.getTickets(userId, size, page);

        // then
        assertThat(result.getContent()).contains(ticket);
        verify(ticketRepository).findByUser(eq(userId), any(Pageable.class));

    }

    @Test
    void 티켓을_단건_조회할_수_있다() {

        // given
        Long userId = 1L;
        Long ticketId = 10L;

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(userId);

        Ticket ticket = mock(Ticket.class);
        when(ticket.getUser()).thenReturn(mockUser);

        when(ticketRepository.findByTicketId(ticketId)).thenReturn(Optional.of(ticket));

        // when
        Ticket result = ticketQueryService.getTicket(userId, ticketId);

        // then
        assertThat(result).isEqualTo(ticket);

    }

    @Test
    void 존재하지_않는_티켓을_조회_시_예외를_던진다() {
        // given
        Long userId = 1L;
        Long ticketId = 999L;

        when(ticketRepository.findByTicketId(ticketId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ticketQueryService.getTicket(userId, ticketId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TICKET_NOT_FOUND.getMessage());
    }

    @Test
    void 본인이_예매하지_않은_티켓을_조회_시_예외를_던진다() {

        // given
        Long userId = 1L;
        Long ticketId = 123L;

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(999L); // 다른 유저

        Ticket ticket = mock(Ticket.class);
        when(ticket.getUser()).thenReturn(mockUser);

        when(ticketRepository.findByTicketId(ticketId)).thenReturn(Optional.of(ticket));

        // when & then
        assertThatThrownBy(() -> ticketQueryService.getTicket(userId, ticketId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TICKET_ACCESS_DENIED.getMessage());

    }
}