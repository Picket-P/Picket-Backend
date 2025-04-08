package com.example.picket.domain.seat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.Grade;
import com.example.picket.common.enums.SeatStatus;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.seat.dto.request.SeatUpdateRequest;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.repository.SeatRepository;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.service.ShowDateQueryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SeatCommandServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ShowDateQueryService showDateQueryService;

    @InjectMocks
    private SeatCommandService seatCommandService;

    private final AuthUser adminUser = AuthUser.toEntity(1L, UserRole.ADMIN);

    @Nested
    class 좌석_수정_테스트 {

        @Test
        void 좌석이_추가되는_경우_새로운_좌석_저장() {
            // given
            Long showDateId = 1L;
            Show show = Show.toEntity(1L, "테스트 공연", "image.png", Category.CONCERT, "설명",
                    "장소", LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), 1);
            LocalDateTime dateTime = LocalDateTime.now().plusDays(1);
            ShowDate showDate = ShowDate.toEntity(
                    dateTime.toLocalDate(),
                    dateTime.toLocalTime(),
                    dateTime.toLocalTime().plusHours(2),
                    100,  // totalSeatCount
                    0,    // reservedSeatCount
                    show
            );
            Grade grade = Grade.VIP;
            BigDecimal newPrice = BigDecimal.valueOf(10000);

            Seat existingSeat = Seat.toEntity(grade, 1, newPrice, showDate);

            given(showDateQueryService.getShowDate(showDateId)).willReturn(showDate);
            given(seatRepository.findAllByShowDateId(showDateId)).willReturn(List.of(existingSeat));
            given(seatRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            SeatUpdateRequest request = SeatUpdateRequest.toDto(grade.name(), 3, newPrice);

            // when
            List<Seat> updatedSeats = seatCommandService.updateSeats(adminUser, showDateId, List.of(request));

            // then
            assertThat(updatedSeats).hasSize(2); // 기존 좌석 1 + 추가된 2
            verify(seatRepository, times(2)).save(any());
        }

        @Test
        void 좌석이_감소되는_경우_예약되지_않은_좌석만_삭제() {
            // given
            Long showDateId = 1L;
            Show show = Show.toEntity(1L, "공연", "image.png", Category.CONCERT, "desc",
                    "장소", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 1);

            ShowDate showDate = ShowDate.toEntity(
                    LocalDate.now().plusDays(2),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    100, 0, show);

            BigDecimal price = BigDecimal.valueOf(10000);

            Seat seat1 = Seat.toEntity(Grade.VIP, 1, price, showDate);
            Seat seat2 = Seat.toEntity(Grade.VIP, 2, price, showDate);

            given(showDateQueryService.getShowDate(showDateId)).willReturn(showDate);
            given(seatRepository.findAllByShowDateId(showDateId)).willReturn(List.of(seat1, seat2));

            SeatUpdateRequest request = SeatUpdateRequest.toDto(Grade.VIP.name(), 1, price);

            // when
            List<Seat> updated = seatCommandService.updateSeats(adminUser, showDateId, List.of(request));

            // then
            verify(seatRepository, times(1)).deleteAll(any());
        }

        @Test
        void 예약시작_후_좌석_감소_불가() {
            // given
            Long showDateId = 1L;
            Show show = Show.toEntity(1L, "공연", "image.png", Category.CONCERT, "desc",
                    "장소", LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2), 1);

            ShowDate showDate = ShowDate.toEntity(
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    100, 0, show);

            BigDecimal price = BigDecimal.valueOf(10000);

            Seat seat1 = Seat.toEntity(Grade.VIP, 1, price, showDate);
            Seat seat2 = Seat.toEntity(Grade.VIP, 2, price, showDate);

            given(showDateQueryService.getShowDate(showDateId)).willReturn(showDate);
            given(seatRepository.findAllByShowDateId(showDateId)).willReturn(List.of(seat1, seat2));

            SeatUpdateRequest request = SeatUpdateRequest.toDto(Grade.VIP.name(), 1, price);

            // when & then
            assertThatThrownBy(() -> seatCommandService.updateSeats(adminUser, showDateId, List.of(request)))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("예매 시작 이후에는 좌석 수 감소가 불가능합니다");

            verify(seatRepository, never()).deleteAll(any());
        }

        @Test
        void 예매_시작_이후에는_좌석_수_감소_불가() {
            // given
            Show show = Show.toEntity(
                    1L, "공연", "image.png", Category.CONCERT, "desc",
                    "장소",
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(1),
                    1
            );

            ShowDate showDate = ShowDate.toEntity(
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    100, 0, show);

            BigDecimal price = BigDecimal.valueOf(10000);

            given(showDateQueryService.getShowDate(showDate.getId()))
                    .willReturn(showDate);

            Seat reservedSeat = Seat.toEntity(Grade.VIP, 1, price, showDate);
            ReflectionTestUtils.setField(reservedSeat, "seatStatus", SeatStatus.RESERVED);

            given(seatRepository.findAllByShowDateId(showDate.getId()))
                    .willReturn(List.of(reservedSeat));

            SeatUpdateRequest request = SeatUpdateRequest.toDto(Grade.VIP.name(), 0, price);

            // when & then
            assertThatThrownBy(() -> seatCommandService.updateSeats(adminUser, showDate.getId(), List.of(request)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("예매 시작 이후에는 좌석 수 감소가 불가능합니다. 삭제 API를 사용해주세요.");
        }

        @Test
        void 좌석_줄이기_요청시_예약된_좌석_때문에_실패() {
            // given
            Show show = Show.toEntity(
                    1L, "공연", "image.png", Category.CONCERT, "desc",
                    "장소",
                    LocalDateTime.now().plusDays(1),  // 예매 시작 전
                    LocalDateTime.now().plusDays(2),
                    1
            );

            ShowDate showDate = ShowDate.toEntity(
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    100, 0, show
            );

            BigDecimal price = BigDecimal.valueOf(10000);

            given(showDateQueryService.getShowDate(showDate.getId()))
                    .willReturn(showDate);

            // 현재 좌석 2개 중 1개는 예약된 상태 (줄일 수 없음)
            Seat reservedSeat = Seat.toEntity(Grade.VIP, 1, price, showDate);
            Seat availableSeat = Seat.toEntity(Grade.VIP, 2, price, showDate);

            ReflectionTestUtils.setField(reservedSeat, "seatStatus", SeatStatus.RESERVED);
            ReflectionTestUtils.setField(availableSeat, "seatStatus", SeatStatus.AVAILABLE);

            given(seatRepository.findAllByShowDateId(showDate.getId()))
                    .willReturn(List.of(reservedSeat, availableSeat));

            // 요청: 좌석 수 2개 → 0개로 줄이기 (2개 줄여야 하는데 예약된 좌석 때문에 불가)
            SeatUpdateRequest request = SeatUpdateRequest.toDto(Grade.VIP.name(), 0, price);

            // when & then
            assertThatThrownBy(() -> seatCommandService.updateSeats(adminUser, showDate.getId(), List.of(request)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("예약된 좌석이 있어 일부 좌석을 줄일 수 없습니다.");
        }
    }

    @Nested
    class 좌석_삭제_테스트 {

        @Test
        void 예약되지_않은_좌석은_삭제_가능() {
            // given
            Long seatId = 1L;
            ShowDate showDate = mock(ShowDate.class);
            Seat seat = Seat.toEntity(Grade.VIP, 1, BigDecimal.valueOf(10000), showDate);

            given(seatRepository.findById(seatId)).willReturn(Optional.of(seat));

            // when
            seatCommandService.deleteSeat(adminUser, seatId);

            // then
            verify(seatRepository).delete(seat);
        }

        @Test
        void 예약된_좌석은_삭제_불가() {
            // given
            Long seatId = 1L;
            ShowDate showDate = mock(ShowDate.class);
            Seat seat = Seat.toEntity(Grade.VIP, 1, BigDecimal.valueOf(10000), showDate);
            ReflectionTestUtils.setField(seat, "seatStatus", SeatStatus.RESERVED);// 좌석 상태를 RESERVED로 설정

            given(seatRepository.findById(seatId)).willReturn(Optional.of(seat));

            // when & then
            assertThatThrownBy(() -> seatCommandService.deleteSeat(adminUser, seatId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("이미 예약된 좌석은 삭제할 수 없습니다.");
        }
    }
}