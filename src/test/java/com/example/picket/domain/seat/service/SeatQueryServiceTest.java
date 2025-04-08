package com.example.picket.domain.seat.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.repository.SeatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatQueryServiceTest {

    @InjectMocks
    private SeatQueryService seatQueryService;

    @Mock
    private SeatRepository seatRepository;

    @Test
    void 좌석ID로_좌석_조회_성공() {
        // given
        Long seatId = 1L;
        Seat seat = mock(Seat.class);
        when(seatRepository.findByIdWithShowDateAndShow(seatId)).thenReturn(Optional.of(seat));

        // when
        Seat result = seatQueryService.findById(seatId);

        // then
        assertThat(result).isEqualTo(seat);
        verify(seatRepository).findByIdWithShowDateAndShow(seatId);
    }

    @Test
    void 좌석ID로_좌석_조회_실패_존재하지_않음() {
        // given
        Long seatId = 2L;
        when(seatRepository.findByIdWithShowDateAndShow(seatId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> seatQueryService.findById(seatId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.SEAT_NOT_FOUND.getMessage());

        verify(seatRepository).findByIdWithShowDateAndShow(seatId);
    }

    @Test
    void 공연날짜ID로_좌석_리스트_조회_성공() {
        // given
        Long showDateId = 10L;
        List<Seat> seats = List.of(mock(Seat.class), mock(Seat.class));
        when(seatRepository.findAllByShowDateId(showDateId)).thenReturn(seats);

        // when
        List<Seat> result = seatQueryService.getSeatsByShowDate(showDateId);

        // then
        assertThat(result).hasSize(2);
        verify(seatRepository).findAllByShowDateId(showDateId);
    }

    @Test
    void 공연날짜ID로_좌석_전체조회_성공() {
        // 동일한 내부 호출
        Long showDateId = 5L;
        List<Seat> seats = List.of(mock(Seat.class));
        when(seatRepository.findAllByShowDateId(showDateId)).thenReturn(seats);

        List<Seat> result = seatQueryService.findAllByShowDateId(showDateId);

        assertThat(result).isEqualTo(seats);
        verify(seatRepository).findAllByShowDateId(showDateId);
    }
}

