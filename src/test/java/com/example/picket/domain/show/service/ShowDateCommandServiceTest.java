package com.example.picket.domain.show.service;

import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.ShowStatus;
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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShowDateCommandServiceTest {

    @Mock
    ShowDateRepository showDateRepository;

    @InjectMocks
    ShowDateCommandService showDateCommandService;

    @Nested
    class 공연_날짜_생성_테스트 {

        @Test
        void 공연_날짜_생성_성공() throws Exception {
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

            given(showDateRepository.save(showDate)).willReturn(showDate);

            // when
            showDateCommandService.createShowDate(showDate);

            // then
            verify(showDateRepository, times(1)).save(showDate);
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
                2,
                0L,
                ShowStatus.RESERVATION_PENDING
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