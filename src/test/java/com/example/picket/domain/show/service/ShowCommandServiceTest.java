package com.example.picket.domain.show.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.Grade;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.seat.dto.request.SeatCreateRequest;
import com.example.picket.domain.seat.service.SeatCommandService;
import com.example.picket.domain.seat.service.SeatQueryService;
import com.example.picket.domain.show.dto.request.ShowCreateRequest;
import com.example.picket.domain.show.dto.request.ShowDateRequest;
import com.example.picket.domain.show.dto.request.ShowUpdateRequest;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowCommandServiceTest {

    @Mock
    ShowRepository showRepository;

    @Mock
    ShowDateCommandService showDateCommandService;

    @Mock
    ShowDateQueryService showDateQueryService;

    @Mock
    SeatQueryService seatQueryService;

    @Mock
    SeatCommandService seatCommandService;

    @InjectMocks
    ShowCommandService showCommandService;

    private AuthUser authUser;

    @BeforeEach
    void setUp() {
        authUser = AuthUser.toEntity(1L, UserRole.DIRECTOR);
    }

    // 리플렉션
    private void setCreatedAt(Show show, LocalDateTime createdAt) throws Exception {
        ReflectionTestUtils.setField(show, "createdAt", createdAt);
    }

    private void setShowId(Show show, Long id) {
        ReflectionTestUtils.setField(show, "id", id);
    }

    private Show createShow(LocalDateTime now) {
        return Show.toEntity(
            authUser.getId(),
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

    private Show createShow(LocalDateTime reservationStart, LocalDateTime reservationEnd) {
        return Show.toEntity(
            authUser.getId(),
            "원래 제목",
            "origin.jpg",
            Category.MUSICAL,
            "원래 설명",
            "원래 장소",
            reservationStart,
            reservationEnd,
            2
        );
    }

    private Show createShow(ShowCreateRequest request) {
        return Show.toEntity(
            authUser.getId(),
            request.getTitle(),
            request.getPosterUrl(),
            request.getCategory(),
            request.getDescription(),
            request.getLocation(),
            request.getReservationStart(),
            request.getReservationEnd(),
            request.getTicketsLimitPerUser()
        );
    }

    private ShowCreateRequest createShowCreateRequest(
        LocalDateTime reservationStart,
        LocalDateTime reservationEnd,
        LocalTime dateStartTime,
        LocalTime dateEndTime
    ) {
        return new ShowCreateRequest(
            "테스트 공연",
            "poster.jpg",
            Category.MUSICAL,
            "공연 설명",
            "서울",
            reservationStart,
            reservationEnd,
            2,
            List.of(
                new ShowDateRequest(
                    LocalDate.now().plusDays(1),
                    dateStartTime,
                    dateEndTime,
                    10,
                    List.of(new SeatCreateRequest(Grade.A, 5, BigDecimal.valueOf(50000)))
                )
            )
        );
    }

    private ShowUpdateRequest createShowUpdateRequest(LocalDateTime now) {
        return new ShowUpdateRequest(
            "새 제목",
            "new.jpg",
            Category.CONCERT,
            "새 설명",
            "새 장소",
            now.plusDays(3),
            now.plusDays(4),
            3
        );
    }

    @Nested
    class 공연_생성_테스트 {

        @Test
        void 공연_생성_성공() throws Exception {
            // given
            LocalDateTime now = LocalDateTime.now();
            ShowCreateRequest request = createShowCreateRequest(
                now.plusDays(1),
                now.plusDays(2),
                LocalTime.of(14, 0),
                LocalTime.of(16, 0)
            );

            Show show = createShow(request);
            given(showRepository.save(any(Show.class))).willReturn(show);

            // when
            Show result = showCommandService.createShow(authUser, request);

            // then
            assertThat(result)
                .extracting("directorId", "title", "posterUrl", "category",
                    "description", "location", "reservationStart", "reservationEnd", "ticketsLimitPerUser"
                )
                .containsExactly(
                    1L, "테스트 공연", "poster.jpg",
                    Category.MUSICAL, "공연 설명", "서울",
                    now.plusDays(1), now.plusDays(2), 2
                );
            verify(showRepository, times(1)).save(any(Show.class));
            verify(showDateCommandService, times(1)).createShowDate(any(ShowDate.class));
            verify(seatCommandService, times(1)).saveAll(argThat(seats -> seats.size() == 5));
        }

        @Test
        void 공연_생성_시작시간_종료시간_유효성_검사_실패() throws Exception {
            // given
            LocalDateTime now = LocalDateTime.now();
            ShowCreateRequest request = createShowCreateRequest(
                now.plusDays(1),
                now.plusDays(2),
                LocalTime.of(16, 0),
                LocalTime.of(14, 0)
            );

            // when & then
            assertThatThrownBy(() -> showCommandService.createShow(authUser, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("공연 시작 시간이 종료 시간보다 늦을 수 없습니다.");
            verify(showRepository, never()).save(any());
            verify(showDateCommandService, never()).createShowDate(any());
            verify(seatCommandService, never()).saveAll(any());
        }
    }

    @Nested
    class 공연_수정_테스트 {
        
        @Test
        void 공연_수정_성공() throws Exception {
            // given
            Long showId = 1L;
            LocalDateTime now = LocalDateTime.now();

            Show show = createShow(now);
            setShowId(show, showId);
            setCreatedAt(show, now);

            given(showRepository.findById(showId)).willReturn(Optional.of(show));
            given(showDateQueryService.getShowDatesByShowId(showId)).willReturn(Collections.emptyList());
            ShowUpdateRequest request = createShowUpdateRequest(now);

            // when
            Show result = showCommandService.updateShow(authUser, showId, request);

            // then
            assertThat(result)
                .extracting("title", "posterUrl", "category", "description", "location")
                .containsExactly("새 제목", "new.jpg", Category.CONCERT, "새 설명", "새 장소");

            verify(showRepository, times(1)).findById(showId);
        }
        
        @Test
        void 공연_수정_없는_공연ID_예외발생() throws Exception {
            // given
            Long showId = -1L;
            LocalDateTime now = LocalDateTime.now();

            Show show = createShow(now);
            setShowId(show, 1L);
            setCreatedAt(show, now);

            given(showRepository.findById(showId)).willReturn(Optional.empty());
            ShowUpdateRequest request = createShowUpdateRequest(now);

            // when & then
            assertThatThrownBy(() -> showCommandService.updateShow(authUser, showId, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 공연을 찾을 수 없습니다.");
        }

        @Test
        void 공연_수정_이미_삭제된_공연_수정시_예외발생() throws Exception {
            // given
            Long showId = 1L;
            LocalDateTime now = LocalDateTime.now();

            Show show = createShow(now);
            setShowId(show, showId);
            setCreatedAt(show, now);
            ReflectionTestUtils.setField(show, "deletedAt", now.plusDays(1));

            given(showRepository.findById(showId)).willReturn(Optional.of(show));
            ShowUpdateRequest request = createShowUpdateRequest(now);

            // when & then
            assertThatThrownBy(() -> showCommandService.updateShow(authUser, showId, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("삭제된 공연은 수정할 수 없습니다.");
        }

        @Test
        void 공연_수정_자신이_생성한_공연이_아닐시_예외발생() throws Exception {
            // given
            Long showId = 1L;
            LocalDateTime now = LocalDateTime.now();
            Show show = createShow(now);
            setShowId(show, showId);
            ReflectionTestUtils.setField(show, "directorId", 2L);

            given(showRepository.findById(showId)).willReturn(Optional.of(show));
            ShowUpdateRequest request = createShowUpdateRequest(now);

            // when & then
            assertThatThrownBy(() -> showCommandService.updateShow(authUser, showId, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 작업을 수행할 권한이 없습니다.");
        }
        
        @Test
        void 공연_수정_예매_시작_이후에는_공연_수정불가_예외발생() throws Exception {
            // given
            Long showId = 1L;
            LocalDateTime now = LocalDateTime.now();
            Show show = createShow(now.minusDays(3), now.minusDays(1));

            given(showRepository.findById(showId)).willReturn(Optional.of(show));
            ShowUpdateRequest request = createShowUpdateRequest(now);

            // when & then
            assertThatThrownBy(() -> showCommandService.updateShow(authUser, showId, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("예매 시작 이후에는 공연을 수정할 수 없습니다.");
        }

        @Test
        void 공연_수정_이미_종료된_공연_수정시_예외발생() throws Exception {
            // given
            Long showId = 1L;
            LocalDateTime now = LocalDateTime.now();
            Show show = createShow(now);
            setShowId(show, showId);

            ShowDate showDate = ShowDate.toEntity(LocalDate.now().minusDays(1),
                LocalTime.of(10, 0), LocalTime.of(12, 0), 100, 0, show);

            given(showRepository.findById(showId)).willReturn(Optional.of(show));
            given(showDateQueryService.getShowDatesByShowId(showId))
                .willReturn(Collections.singletonList(showDate));

            ShowUpdateRequest request = createShowUpdateRequest(now);

            // when & then
            assertThatThrownBy(() -> showCommandService.updateShow(authUser, showId, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("종료된 공연은 수정할 수 없습니다.");
            verify(showRepository, times(1)).findById(showId);
            verify(showDateQueryService, times(1)).getShowDatesByShowId(showId);
        }

    }

    @Nested
    class 공연_삭제_테스트 {

        @Test
        void 공연_삭제_성공() throws Exception {
            // given
            Long showId = 1L;
            LocalDateTime now = LocalDateTime.now();
            Show show = createShow(now);
            setShowId(show, showId);

            given(showRepository.findById(showId)).willReturn(Optional.of(show));

            // when
            showCommandService.deleteShow(authUser, showId);

            // then
            assertThat(show.getDeletedAt()).isNotNull();
            verify(showRepository, times(1)).findById(showId);
        }

        @Test
        void 공연_삭제_없는_공연ID_예외발생() throws Exception {
            // given
            Long showId = -1L;
            given(showRepository.findById(showId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> showCommandService.deleteShow(authUser, showId))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 공연을 찾을 수 없습니다.");
            verify(showRepository, times(1)).findById(showId);
        }

        @Test
        void 공연_삭제_자신이_생성한_공연이_아닐시_예외발생() throws Exception {
            // given
            Long showId = 1L;
            Long otherUserId = 2L;
            LocalDateTime now = LocalDateTime.now();
            Show show = createShow(now);
            setShowId(show, showId);
            ReflectionTestUtils.setField(show, "directorId", otherUserId);

            given(showRepository.findById(showId)).willReturn(Optional.of(show));

            // when & then
            assertThatThrownBy(() -> showCommandService.deleteShow(authUser, showId))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 작업을 수행할 권한이 없습니다.");
            verify(showRepository, times(1)).findById(showId);
        }

        @Test
        void 공연_삭제_예매_시작_후_예외발생() throws Exception {
            // given
            Long showId = 1L;
            LocalDateTime now = LocalDateTime.now();
            Show show = createShow(now.minusDays(3), now.minusDays(1));
            setShowId(show, showId);

            given(showRepository.findById(showId)).willReturn(Optional.of(show));

            // when & then
            assertThatThrownBy(() -> showCommandService.deleteShow(authUser, showId))
                .isInstanceOf(CustomException.class)
                .hasMessage("예매 시작 이후에는 공연을 삭제할 수 없습니다.");
            verify(showRepository, times(1)).findById(showId);
        }
    }

}