package com.example.picket.domain.like.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.ShowStatus;
import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.like.entity.Like;
import com.example.picket.domain.like.repository.LikeRepository;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.service.ShowQueryService;
import com.example.picket.domain.user.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class LikeQueryServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private ShowQueryService showQueryService;

    @InjectMocks
    private LikeQueryService likeQueryService;

    @Nested
    class 좋아요_조회_테스트 {

        @Test
        void 사용자가_좋아요를_누른_공연이_없을_경우_빈_리스트_반환() {
            // given
            Long userId = 1L;
            int page = 0;
            int size = 10;
            Pageable pageable = PageRequest.of(page, size);

            given(likeRepository.findLikesByUserId(userId, pageable)).willReturn(Collections.emptyList());

            // when
            List<Show> result = likeQueryService.getLikes(userId, page, size);

            // then
            assertThat(result).isEmpty();
            verify(likeRepository, times(1)).findLikesByUserId(userId, pageable);
            verify(showQueryService, times(1)).getShowDatesByShowIds(Collections.emptyList());
        }

        @Test
        void 사용자가_좋아요를_누른_공연이_있을_경우_공연_리스트_반환() {
            // given
            Long userId = 1L;
            int page = 0;
            int size = 10;

            User user = User.toEntity("test@example.com", "!Password1234", UserRole.USER, null, "nickname1",
                    LocalDate.of(2000, 12, 12), Gender.MALE);
            Show show1 = Show.toEntity(1L, "Show 1", "image1.png", Category.CONCERT, "desc1",
                    "location1", LocalDateTime.now(), LocalDateTime.now().plusDays(3), 3, 0L
                    , ShowStatus.RESERVATION_PENDING);
            Show show2 = Show.toEntity(2L, "Show 2", "image2.png", Category.MUSICAL, "desc2",
                    "location2", LocalDateTime.now(), LocalDateTime.now().plusDays(5), 5, 0L
                    , ShowStatus.RESERVATION_PENDING);

            Like like1 = Like.toEntity(show1, user);
            Like like2 = Like.toEntity(show2, user);

            List<Like> likeList = List.of(like1, like2);
            List<Show> showList = List.of(show1, show2);

            given(likeRepository.findLikesByUserId(any(), any())).willReturn(likeList);
            given(showQueryService.getShowDatesByShowIds(any())).willReturn(showList);

            // when
            List<Show> result = likeQueryService.getLikes(userId, page, size);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(show1, show2);
            verify(likeRepository, times(1)).findLikesByUserId(any(), any());
            verify(showQueryService, times(1)).getShowDatesByShowIds(any());
        }

        @Test
        void 페이지네이션_적용_확인() {
            // given
            Long userId = 1L;
            int page = 1;
            int size = 1; // 한 페이지에 1개만 조회

            User user = User.toEntity("test@example.com", "!Password1234", UserRole.USER, null, "nickname1",
                    LocalDate.of(2000, 12, 12), Gender.MALE);

            Show show1 = Show.toEntity(1L, "Show 1", "image1.png", Category.CONCERT, "desc1",
                    "location1", LocalDateTime.now(), LocalDateTime.now().plusDays(3), 3, 0L
                    , ShowStatus.RESERVATION_PENDING);

            Like like1 = Like.toEntity(show1, user);

            List<Like> likeList = List.of(like1); // 한 개만 반환
            List<Show> showList = List.of(show1);

            given(likeRepository.findLikesByUserId(any(), any())).willReturn(likeList);
            given(showQueryService.getShowDatesByShowIds(any())).willReturn(showList);

            // when
            List<Show> result = likeQueryService.getLikes(userId, page, size);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsExactly(show1);
            verify(likeRepository, times(1)).findLikesByUserId(any(), any());
            verify(showQueryService, times(1)).getShowDatesByShowIds(any());
        }
    }

}