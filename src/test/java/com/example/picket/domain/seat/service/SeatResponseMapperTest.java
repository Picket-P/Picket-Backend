package com.example.picket.domain.seat.service;

import com.example.picket.common.enums.Grade;
import com.example.picket.common.enums.SeatStatus;
import com.example.picket.domain.seat.dto.response.SeatDetailResponse;
import com.example.picket.domain.seat.dto.response.SeatGroupByGradeResponse;
import com.example.picket.domain.seat.entity.Seat;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SeatResponseMapperTest {

    private final SeatResponseMapper seatResponseMapper = new SeatResponseMapper();

    @Test
    void 좌석리스트를_등급별로_그룹핑_성공() {
        // given
        Grade vip = Grade.VIP;
        Grade r = Grade.R;

        Seat seat1 = mock(Seat.class);
        Seat seat2 = mock(Seat.class);
        Seat seat3 = mock(Seat.class);

        when(seat1.getGrade()).thenReturn(vip);
        when(seat2.getGrade()).thenReturn(vip);
        when(seat3.getGrade()).thenReturn(r);

        when(seat1.getPrice()).thenReturn(new BigDecimal("100000"));
        when(seat2.getPrice()).thenReturn(new BigDecimal("100000"));
        when(seat3.getPrice()).thenReturn(new BigDecimal("80000"));

        when(seat1.getId()).thenReturn(1L);
        when(seat2.getId()).thenReturn(2L);
        when(seat3.getId()).thenReturn(3L);

        when(seat1.getSeatNumber()).thenReturn(1);
        when(seat2.getSeatNumber()).thenReturn(2);
        when(seat3.getSeatNumber()).thenReturn(1);

        when(seat1.getSeatStatus()).thenReturn(SeatStatus.AVAILABLE);
        when(seat2.getSeatStatus()).thenReturn(SeatStatus.AVAILABLE);
        when(seat3.getSeatStatus()).thenReturn(SeatStatus.AVAILABLE);

        List<Seat> seats = List.of(seat1, seat2, seat3);

        // when
        List<SeatGroupByGradeResponse> result = seatResponseMapper.toGroupByGradeResponses(seats);

        // then
        assertThat(result).hasSize(2);

        SeatGroupByGradeResponse vipGroup = result.stream()
                .filter(res -> res.getGrade().equals(vip))
                .findFirst()
                .orElseThrow();

        assertThat(vipGroup.getPrice()).isEqualTo(new BigDecimal("100000"));

        assertThat(vipGroup.getSeats())
                .extracting(SeatDetailResponse::getId, SeatDetailResponse::getSeatNumber)
                .containsExactlyInAnyOrder(
                        tuple(1L, 1),
                        tuple(2L, 2)
                );

        SeatGroupByGradeResponse rGroup = result.stream()
                .filter(res -> res.getGrade().equals(r))
                .findFirst()
                .orElseThrow();

        assertThat(rGroup.getPrice()).isEqualTo(new BigDecimal("80000"));

        assertThat(rGroup.getSeats())
                .extracting(SeatDetailResponse::getId, SeatDetailResponse::getSeatNumber)
                .containsExactly(
                        tuple(3L, 1)
                );
    }

}
