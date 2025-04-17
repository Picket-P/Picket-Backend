package com.example.picket.domain.show.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.enums.ShowStatus;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.show.dto.response.ShowDateDetailResponse;
import com.example.picket.domain.show.dto.response.ShowDateResponse;
import com.example.picket.domain.show.dto.response.ShowDetailResponse;
import com.example.picket.domain.show.dto.response.ShowResponse;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.ShowRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowQueryServiceTest {

    @Mock
    ShowRepository showRepository;

    @Mock
    ShowViewCountService showViewCountService;

    @InjectMocks
    ShowQueryService showQueryService;

    @Nested
    class 공연_목록_조회_테스트 {

        @Test
        void 공연_목록_조회_카테고리_필터링_성공() throws Exception {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reservationStart = now;
            LocalDateTime reservationEnd = now.plusDays(1);

            Show show = Show.toEntity(1L, "제목1", "포스터1.jpg",
                Category.MUSICAL, "내용1", "장소1",
                reservationStart, reservationEnd, 2, ShowStatus.RESERVATION_PENDING
            );

            setCreatedAt(show, now);

            List<Show> mockShows = new ArrayList<>(List.of(show));
            given(showRepository.findAllByCategoryAndDeletedAtIsNull(Category.MUSICAL)).willReturn(mockShows);

            // when
            List<Show> result = showQueryService.getShows(Category.MUSICAL, "createdAt", "desc");

            // then
            assertThat(result).hasSize(1);
            assertThat(result)
                .extracting("directorId", "title", "posterUrl", "category",
                    "description", "location", "reservationStart", "reservationEnd", "ticketsLimitPerUser"
                )
                .containsExactly(
                    tuple(1L, "제목1", "포스터1.jpg", Category.MUSICAL, "내용1", "장소1",
                        reservationStart, reservationEnd, 2
                    )
                );
            verify(showRepository, times(1)).findAllByCategoryAndDeletedAtIsNull(Category.MUSICAL);
        }

        @Test
        void 공연_목록_조회_정렬_조건_변경_성공() throws Exception {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reservationStart = now;
            LocalDateTime reservationEnd = now.plusDays(1);

            Show show = Show.toEntity(1L, "제목1", "포스터1.jpg",
                Category.MUSICAL, "내용1", "장소1",
                reservationStart, reservationEnd, 2, ShowStatus.RESERVATION_PENDING
            );
            setCreatedAt(show, now);

            List<Show> mockShows = new ArrayList<>(List.of(show));
            given(showRepository.findAllByCategoryAndDeletedAtIsNull(Category.MUSICAL)).willReturn(mockShows);

            // when
            List<Show> result = showQueryService.getShows(Category.MUSICAL, "reservationStart", "desc");

            // then
            assertThat(result).hasSize(1);
            assertThat(result)
                .extracting("directorId", "title", "posterUrl", "category",
                    "description", "location", "reservationStart", "reservationEnd", "ticketsLimitPerUser"
                )
                .containsExactly(
                    tuple(1L, "제목1", "포스터1.jpg", Category.MUSICAL, "내용1", "장소1",
                        reservationStart, reservationEnd, 2
                    )
                );
            verify(showRepository, times(1)).findAllByCategoryAndDeletedAtIsNull(Category.MUSICAL);
        }

        @Test
        void 공연_목록_조회_유효하지_않은_order_예외() throws Exception {
            // when & then
            assertThatThrownBy(() -> showQueryService.getShows(Category.MUSICAL, "createdAt", "invalidOrder"))
                .isInstanceOf(CustomException.class)
                .hasMessage("유효하지 않은 정렬 방식입니다. (asc, desc만 허용)");
        }

    }

    @Nested
    class 공연_목록_조회_QueryDSL_테스트 {

        @Test
        void 공연_목록_조회_카테고리_필터링_성공() throws Exception {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reservationStart = now;
            LocalDateTime reservationEnd = now.plusDays(1);

            Show show = Show.toEntity(1L, "제목1", "포스터1.jpg",
                Category.MUSICAL, "내용1", "장소1",
                reservationStart, reservationEnd, 2
            );
            setCreatedAt(show, now);
            setModifiedAt(show, now);

            List<ShowDateResponse> showDateResponses = new ArrayList<>();
            ShowResponse showResponse = ShowResponse.toDto(show, showDateResponses);

            List<ShowResponse> mockShows = new ArrayList<>(List.of(showResponse));
            Page<ShowResponse> mockPage = new PageImpl<>(mockShows, PageRequest.of(0, 10), 1);

            given(showRepository.getShowsResponse(Category.MUSICAL, "createdAt", "desc", PageRequest.of(0, 10)))
                .willReturn(mockPage);

            // when
            Page<ShowResponse> result = showQueryService.getShows(Category.MUSICAL, "createdAt", "desc", 1, 10);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent())
                .extracting("directorId", "title", "posterUrl", "category",
                    "description", "location", "reservationStart", "reservationEnd", "ticketsLimitPerUser"
                )
                .containsExactly(
                    tuple(1L, "제목1", "포스터1.jpg", Category.MUSICAL.name(), "내용1", "장소1",
                        reservationStart.toString(), reservationEnd.toString(), 2
                    )
                );
            verify(showRepository, times(1))
                .getShowsResponse(Category.MUSICAL, "createdAt", "desc", PageRequest.of(0, 10));
        }

        @Test
        void 공연_목록_조회_정렬_조건_변경_성공() throws Exception {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reservationStart = now;
            LocalDateTime reservationEnd = now.plusDays(1);

            Show show = Show.toEntity(1L, "제목1", "포스터1.jpg",
                Category.MUSICAL, "내용1", "장소1",
                reservationStart, reservationEnd, 2
            );
            setCreatedAt(show, now);
            setModifiedAt(show, now);

            List<ShowDateResponse> showDateResponses = new ArrayList<>();
            ShowResponse showResponse = ShowResponse.toDto(show, showDateResponses);

            List<ShowResponse> mockShows = new ArrayList<>(List.of(showResponse));
            Page<ShowResponse> mockPage = new PageImpl<>(mockShows, PageRequest.of(0, 10), 1);

            given(showRepository.getShowsResponse(Category.MUSICAL, "reservationStart", "desc", PageRequest.of(0, 10)))
                .willReturn(mockPage);

            // when
            Page<ShowResponse> result = showQueryService.getShows(Category.MUSICAL, "reservationStart", "desc", 1, 10);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent())
                .extracting("directorId", "title", "posterUrl", "category",
                    "description", "location", "reservationStart", "reservationEnd", "ticketsLimitPerUser"
                )
                .containsExactly(
                    tuple(1L, "제목1", "포스터1.jpg", Category.MUSICAL.name(), "내용1", "장소1",
                        reservationStart.toString(), reservationEnd.toString(), 2
                    )
                );
            verify(showRepository, times(1))
                .getShowsResponse(Category.MUSICAL, "reservationStart", "desc", PageRequest.of(0, 10));
        }

        @Test
        void 공연_목록_조회_유효하지_않은_order_예외() throws Exception {
            // given
            given(showRepository.getShowsResponse(Category.MUSICAL, "createdAt", "invalidOrder", PageRequest.of(0, 10)))
                .willThrow(new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 정렬 방식입니다. (asc, desc만 허용)"));

            // when & then
            assertThatThrownBy(() -> showQueryService.getShows(Category.MUSICAL, "createdAt", "invalidOrder", 1, 10))
                .isInstanceOf(CustomException.class)
                .hasMessage("유효하지 않은 정렬 방식입니다. (asc, desc만 허용)");
        }

        @Test
        void 공연_목록_조회_페이지_번호_음수_처리() throws Exception {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reservationStart = now;
            LocalDateTime reservationEnd = now.plusDays(1);

            Show show = Show.toEntity(1L, "제목1", "포스터1.jpg",
                Category.MUSICAL, "내용1", "장소1",
                reservationStart, reservationEnd, 2
            );
            setCreatedAt(show, now);
            setModifiedAt(show, now);

            List<ShowDateResponse> showDateResponses = new ArrayList<>();
            ShowResponse showResponse = ShowResponse.toDto(show, showDateResponses);

            List<ShowResponse> mockShows = new ArrayList<>(List.of(showResponse));
            Page<ShowResponse> mockPage = new PageImpl<>(mockShows, PageRequest.of(0, 10), 1);

            given(showRepository.getShowsResponse(Category.MUSICAL, "createdAt", "desc", PageRequest.of(0, 10)))
                .willReturn(mockPage);

            // when
            Page<ShowResponse> result = showQueryService.getShows(Category.MUSICAL, "createdAt", "desc", -1, 10);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(showRepository, times(1))
                .getShowsResponse(Category.MUSICAL, "createdAt", "desc", PageRequest.of(0, 10));
        }
    }

    @Nested
    class 공연_단건_조회_테스트 {

        @Test
        void 공연_단건_조회_성공() throws Exception {
            // given
            Long showId = 1L;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reservationStart = now;
            LocalDateTime reservationEnd = now.plusDays(1);

            Show show = Show.toEntity(1L, "제목1", "포스터1.jpg",
                Category.MUSICAL, "내용1", "장소1",
                reservationStart, reservationEnd, 2, ShowStatus.RESERVATION_PENDING
            );
            setCreatedAt(show, now);
            given(showRepository.findById(showId)).willReturn(Optional.of(show));

            // when
            Show result = showQueryService.getShow(showId);

            // then
            assertThat(result)
                .extracting("directorId", "title", "posterUrl", "category",
                    "description", "location", "reservationStart", "reservationEnd", "ticketsLimitPerUser"
                )
                .containsExactly(1L, "제목1", "포스터1.jpg", Category.MUSICAL, "내용1", "장소1",
                    reservationStart, reservationEnd, 2
                );
            verify(showRepository, times(1)).findById(showId);
        }

        @Test
        void 공연_단건_조회_찾을_수_없는_공연_ID_예외() throws Exception {
            // given
            Long showId = -1L;
            given(showRepository.findById(showId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> showQueryService.getShow(showId))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 공연을 찾을 수 없습니다.");

        }
    }

    @Nested
    class 공연_단건_조회_QueryDSL_테스트 {

        @Test
        void 로그인_사용자_조회_성공() throws Exception {
            // given
            Long showId = 1L;
            Long userId = 1L;
            AuthUser authUser = AuthUser.toEntity(userId, UserRole.USER);

            LocalDateTime now = LocalDateTime.now();
            List<ShowDateDetailResponse> showDates = new ArrayList<>();
            ShowDetailResponse response = ShowDetailResponse.toDto(
                showId, 1L, "제목1", "포스터1.jpg", Category.MUSICAL, "내용1", "장소1",
                now, now.plusDays(1), 2, 0, showDates, now, now
            );

            given(showRepository.getShowDetailResponseById(showId)).willReturn(Optional.of(response));
            given(showViewCountService.incrementViewCount(authUser, showId))
                .willReturn(CompletableFuture.completedFuture(1));

            // when
            ShowDetailResponse result = showQueryService.getShow(authUser, showId);

            // then
            assertThat(result.getViewCount()).isEqualTo(1);
            assertThat(result.getTitle()).isEqualTo("제목1");
            verify(showRepository, times(1)).getShowDetailResponseById(showId);
            verify(showViewCountService, times(1)).incrementViewCount(authUser, showId);
        }

        @Test
        void 비로그인_사용자_조회_성공() throws Exception {
            // given
            Long showId = 1L;
            AuthUser authUser = null;

            LocalDateTime now = LocalDateTime.now();
            List<ShowDateDetailResponse> showDates = new ArrayList<>();
            ShowDetailResponse response = ShowDetailResponse.toDto(
                showId, 1L, "제목1", "포스터1.jpg", Category.MUSICAL, "내용1", "장소1",
                now, now.plusDays(1), 2, 0, showDates, now, now
            );

            given(showRepository.getShowDetailResponseById(showId)).willReturn(Optional.of(response));
            given(showViewCountService.incrementViewCount(authUser, showId))
                .willReturn(CompletableFuture.completedFuture(null));

            // when
            ShowDetailResponse result = showQueryService.getShow(authUser, showId);

            // then
            assertThat(result.getViewCount()).isEqualTo(0);
            assertThat(result.getTitle()).isEqualTo("제목1");
            verify(showRepository, times(1)).getShowDetailResponseById(showId);
            verify(showViewCountService, times(1)).incrementViewCount(authUser, showId);
        }

        @Test
        void 공연_단건_조회_찾을_수_없는_공연_ID_예외() throws Exception {
            // given
            Long showId = -1L;
            AuthUser authUser = AuthUser.toEntity(1L, UserRole.USER);

            given(showRepository.getShowDetailResponseById(showId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> showQueryService.getShow(authUser, showId))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 공연을 찾을 수 없습니다.")
                .satisfies(e -> assertThat(((CustomException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
            verify(showRepository, times(1)).getShowDetailResponseById(showId);
            verify(showViewCountService, times(0)).incrementViewCount(any(), any());
        }

    }

    // 리플렉션
    private void setCreatedAt(Show show, LocalDateTime createdAt) {
        ReflectionTestUtils.setField(show, "createdAt", createdAt);
    }

    private void setModifiedAt(Show show, LocalDateTime modifiedAt) {
        ReflectionTestUtils.setField(show, "modifiedAt", modifiedAt);
    }

}