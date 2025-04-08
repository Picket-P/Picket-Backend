package com.example.picket.domain.show.service;

import com.example.picket.common.enums.Category;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShowDateQueryServiceTest {

    @Mock
    ShowDateRepository showDateRepository;

    @InjectMocks
    ShowDateQueryService showDateQueryService;

    @Nested
    class 공연_날짜_단건_조회_테스트 {
        
        @Test
        void 공연_날짜_단건_조회_성공() throws Exception {
            // given
            Long showId = 1L;
            Long showDateId = 1L;

            Show show = createShow(LocalDateTime.now());
            setShowId(show, showId);
            ShowDate showDate = createShowDate(
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                show
            );
            setShowDateId(showDate, showDateId);

            given(showDateRepository.findById(showDateId)).willReturn(Optional.of(showDate));
            // when
            ShowDate result = showDateQueryService.getShowDate(showDateId);

            // then
            assertThat(result).isEqualTo(showDate);
            verify(showDateRepository, times(1)).findById(showDateId);
        }
        
        @Test
        void 공연_날짜_단건_조회_없는_공연_날짜ID_예외() throws Exception {
            // given
            Long showId = 1L;
            Long showDateId = 1L;

            given(showDateRepository.findById(showDateId)).willReturn(Optional.empty());
            
            // when & then
            assertThatThrownBy(() -> showDateQueryService.getShowDate(showDateId))
                .isInstanceOf(CustomException.class)
                .hasMessage("공연 날짜를 찾을 수 없습니다.");
            verify(showDateRepository, times(1)).findById(showDateId);
            
        }
    }

    @Nested
    class 공연_날짜_단건_조회_테스트_show_객체 {

        @Test
        void 공연_날짜_단건_조회_성공() throws Exception {
            // given
            LocalDate now = LocalDate.now();

            Long showId = 1L;

            Show show = createShow(LocalDateTime.now());
            setShowId(show, showId);

            ShowDate showDate = createShowDate(
                now,
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                show
            );

            given(showDateRepository.findShowDateByShow(show))
                .willReturn(Optional.of(showDate));

            // when
            ShowDate result = showDateQueryService.getShowDateByShow(show);

            // then
            assertThat(result)
                .extracting("date", "startTime", "endTime", "totalSeatCount", "reservedSeatCount")
                .containsExactly(now, LocalTime.of(10, 0), LocalTime.of(14, 0), 100, 0);
            verify(showDateRepository, times(1)).findShowDateByShow(show);
        }

        @Test
        void 공연_날짜_단건_조회_없는_공연_날짜_by_공연_예외() throws Exception {
            // given
            LocalDate now = LocalDate.now();

            Long showId = 1L;

            Show show = createShow(LocalDateTime.now());
            setShowId(show, showId);

            given(showDateRepository.findShowDateByShow(show))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> showDateQueryService.getShowDateByShow(show))
                .isInstanceOf(CustomException.class)
                .hasMessage("존재하지 않는 ShowDate입니다.");
            verify(showDateRepository, times(1)).findShowDateByShow(show);
        }
    }

    @Nested
    class 공연_날짜_목록_조회_테스트_showId {
        
        @Test
        void 공연_날짜_목록_조회_성공() throws Exception {
            // given
            LocalDate now = LocalDate.now();

            Long showId = 1L;

            Show show = createShow(LocalDateTime.now());
            setShowId(show, showId);


            ShowDate showDate1 = createShowDate(
                now,
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                show
            );
            ShowDate showDate2 = createShowDate(
                now.plusDays(1),
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                show
            );

            given(showDateRepository.findAllByShowId(showId))
                .willReturn(List.of(showDate1, showDate2));

            // when
            List<ShowDate> result = showDateQueryService.getShowDatesByShowId(showId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result)
                .extracting("date", "startTime", "endTime", "totalSeatCount", "reservedSeatCount")
                .containsExactly(
                    tuple(now, LocalTime.of(10, 0), LocalTime.of(14, 0), 100, 0),
                    tuple(now.plusDays(1), LocalTime.of(10, 0), LocalTime.of(14, 0), 100, 0)
                );
            verify(showDateRepository, times(1)).findAllByShowId(showId);
        }

        @Test
        void 공연_날짜_목록_조회_성공_빈_배열() throws Exception {
            // given
            LocalDate now = LocalDate.now();

            Long showId = 1L;

            Show show = createShow(LocalDateTime.now());
            setShowId(show, showId);

            given(showDateRepository.findAllByShowId(showId))
                .willReturn(List.of());

            // when
            List<ShowDate> result = showDateQueryService.getShowDatesByShowId(showId);

            // then
            assertThat(result).isEmpty();
            verify(showDateRepository, times(1)).findAllByShowId(showId);
        }
    }

    private Show createShow(LocalDateTime now) {
        return Show.toEntity(
            1L,
            "원래 제목",
            "origin.jpg",
            Category.MUSICAL,
            "원래 설명",
            "원래 장소",
            now.plusDays(1),
            now.plusDays(2),
            2
        );
    }

    private ShowDate createShowDate(
        LocalDate now,
        LocalTime startTime,
        LocalTime endTime,
        Show show
    ) {
        return ShowDate.toEntity(
            now,
            startTime,
            endTime,
            100,
            0,
            show
        );
    }

    private void setShowId(Show show, Long showId) {
        ReflectionTestUtils.setField(show, "id", showId);
    }

    private void setShowDateId(ShowDate showDate, Long showDateId) {
        ReflectionTestUtils.setField(showDate, "id", showDateId);
    }

}