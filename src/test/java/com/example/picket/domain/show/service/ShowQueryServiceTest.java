package com.example.picket.domain.show.service;

import com.example.picket.common.entity.BaseEntity;
import com.example.picket.common.enums.Category;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.ShowRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    ShowDateQueryService showDateQueryService;

    @InjectMocks
    ShowQueryService showQueryService;

    // 리플렉션
    private void setCreatedAt(Show show, LocalDateTime createdAt) throws Exception {
        Field field = BaseEntity.class.getDeclaredField("createdAt");
        field.setAccessible(true);
        field.set(show, createdAt);
    }

    @Nested
    class 공연_목록_조회_테스트 {

        @Test
        void 공연_목록_조회_카테고리_없이_성공() throws Exception {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reservationStart = now;
            LocalDateTime reservationEnd = now.plusDays(1);

            Show show1 = Show.toEntity(1L, "제목1", "포스터1.jpg",
                Category.MUSICAL, "내용1", "장소1",
                reservationStart, reservationEnd, 2
            );
            Show show2 = Show.toEntity(2L, "제목2", "포스터2.jpg",
                Category.CONCERT, "내용2", "장소2",
                reservationStart, reservationEnd, 2
            );
            setCreatedAt(show1, now);
            setCreatedAt(show2, now.minusDays(1));

            List<Show> mockShows = new ArrayList<>(List.of(show1, show2));
            given(showRepository.findAll()).willReturn(mockShows);

            // when
            List<Show> result = showQueryService.getShows(null, "createdAt", "desc");

            // then
            assertThat(result).hasSize(2);
            assertThat(result)
                .extracting("directorId", "title", "posterUrl", "category",
                    "description", "location", "reservationStart", "reservationEnd", "ticketsLimitPerUser"
                )
                .containsExactly(
                    tuple(1L, "제목1", "포스터1.jpg", Category.MUSICAL, "내용1", "장소1",
                        reservationStart, reservationEnd, 2
                    ),
                    tuple(2L, "제목2", "포스터2.jpg", Category.CONCERT, "내용2", "장소2",
                        reservationStart, reservationEnd, 2
                    )
                );
            verify(showRepository, times(1)).findAll();
        }

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

            List<Show> mockShows = new ArrayList<>(List.of(show));
            given(showRepository.findAllByCategoryAndIsDeletedFalse(Category.MUSICAL)).willReturn(mockShows);

            // when
            List<Show> result = showQueryService.getShows("musical", "createdAt", "desc");

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
            verify(showRepository, times(1)).findAllByCategoryAndIsDeletedFalse(Category.MUSICAL);
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

            List<Show> mockShows = new ArrayList<>(List.of(show));
            given(showRepository.findAllByCategoryAndIsDeletedFalse(Category.MUSICAL)).willReturn(mockShows);

            // when
            List<Show> result = showQueryService.getShows("musical", "reservationStart", "desc");

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
            verify(showRepository, times(1)).findAllByCategoryAndIsDeletedFalse(Category.MUSICAL);
        }

        @Test
        void 공연_목록_조회_유효하지_않은_카테고리_예외() throws Exception {
            // when & then
            assertThatThrownBy(() -> showQueryService.getShows("invalidCategory", "createdAt", "desc"))
                .isInstanceOf(CustomException.class)
                .hasMessage("유효하지 않은 카테고리입니다.");
        }

        @Test
        void 공연_목록_조회_유효하지_않은_order_예외() throws Exception {
            // when & then
            assertThatThrownBy(() -> showQueryService.getShows("MUSICAL", "createdAt", "invalidOrder"))
                .isInstanceOf(CustomException.class)
                .hasMessage("유효하지 않은 정렬 방식입니다. (asc, desc만 허용)");
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
                reservationStart, reservationEnd, 2
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

}