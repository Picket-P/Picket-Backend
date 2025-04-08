package com.example.picket.domain.like.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.domain.like.entity.Like;
import com.example.picket.domain.like.repository.LikeRepository;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.service.ShowQueryService;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserQueryService;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LikeCommandServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private ShowQueryService showQueryService;

    @InjectMocks
    private LikeCommandService likeCommandService;

    @Nested
    class 좋아요_생성_테스트 {

        @Test
        void 좋아요_생성_시_해당_유저가_존재하지_않을_경우_실패() {
            // given
            Long userId = 1L;
            Long showId = 1L;
            given(userQueryService.findById(any())).willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatThrownBy(
                    () -> likeCommandService.createLike(userId, showId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("해당 유저를 찾을 수 없습니다.");
        }

        @Test
        void 좋아요_생성_시_해당_공연이_존재하지_않을_경우_실패() {
            // given
            Long userId = 1L;
            Long showId = 1L;
            User user = mock(User.class);
            given(userQueryService.findById(any())).willReturn(user);
            given(showQueryService.findById(any())).willThrow(new CustomException(ErrorCode.SHOW_NOT_FOUND));

            // when & then
            assertThatThrownBy(
                    () -> likeCommandService.createLike(userId, showId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("해당 공연을 찾을 수 없습니다.");
        }

        @Test
        void 좋아요_생성_시_이미_좋아요가_존재하는_경우_실패() {
            // given
            Long userId = 1L;
            Long showId = 1L;
            User user = mock(User.class);
            Show show = mock(Show.class);
            given(userQueryService.findById(any())).willReturn(user);
            given(showQueryService.findById(any())).willReturn(show);
            given(likeRepository.existsByUserIdAndShowId(any(), any())).willReturn(true);

            // when & then
            assertThatThrownBy(
                    () -> likeCommandService.createLike(userId, showId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("이미 해당 좋아요를 눌렀습니다.");
        }

        @Test
        void 좋아요_추가_성공() {
            // given
            Long userId = 1L;
            Long showId = 1L;
            User user = mock(User.class);
            Show show = mock(Show.class);
            given(userQueryService.findById(any())).willReturn(user);
            given(showQueryService.findById(any())).willReturn(show);
            given(likeRepository.existsByUserIdAndShowId(any(), any())).willReturn(false);

            // when
            likeCommandService.createLike(userId, showId);

            // then
            verify(userQueryService, times(1)).findById(any());
            verify(showQueryService, times(1)).findById(any());
            verify(likeRepository, times(1)).existsByUserIdAndShowId(any(), any());
            verify(likeRepository, times(1)).save(any());
        }
    }

    @Nested
    class 좋아요_취소_테스트 {

        @Test
        void 좋아요_취소_시_해당_좋아요가_존재하지_않을_경우_실패() {
            // given
            Long userId = 1L;
            Long showId = 1L;
            Long likeId = 1L;
            given(likeRepository.findWithUserAndShowById(any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> likeCommandService.deleteLike(userId, showId, likeId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("해당 좋아요를 찾을 수 없습니다.");
        }

        @Test
        void 좋아요_취소_시_해당_좋아요를_누른_유저와_요청한_유저가_다를_경우_실패() {
            // given
            Long userId = 1L;
            Long showId = 1L;
            Long likeId = 1L;
            User user = mock(User.class);
            Like like = mock(Like.class);

            given(likeRepository.findWithUserAndShowById(any())).willReturn(Optional.of(like));
            given(like.getUser()).willReturn(user);
            given(user.getId()).willReturn(2L);

            // when & then
            assertThatThrownBy(
                    () -> likeCommandService.deleteLike(userId, showId, likeId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("해당 좋아요를 누른 사용자와 요청한 사용자가 다릅니다.");
        }

        @Test
        void 좋아요_취소_시_해당_좋아요가_눌린_공연과_요청한_공연이_다를_경우_실패() {
            // given
            Long userId = 1L;
            Long showId = 1L;
            Long likeId = 1L;
            User user = mock(User.class);
            Show show = mock(Show.class);
            Like like = mock(Like.class);

            given(likeRepository.findWithUserAndShowById(any())).willReturn(Optional.of(like));
            given(like.getUser()).willReturn(user);
            given(user.getId()).willReturn(1L);
            given(like.getShow()).willReturn(show);
            given(show.getId()).willReturn(2L);

            // when & then
            assertThatThrownBy(
                    () -> likeCommandService.deleteLike(userId, showId, likeId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("해당 좋아요가 눌린 공연과 요청된 공연이 다릅니다.");
        }

        @Test
        void 좋아요_삭제_성공() {
            // given
            Long userId = 1L;
            Long showId = 1L;
            Long likeId = 1L;
            User user = mock(User.class);
            Show show = mock(Show.class);
            Like like = mock(Like.class);

            given(likeRepository.findWithUserAndShowById(any())).willReturn(Optional.of(like));
            given(like.getUser()).willReturn(user);
            given(user.getId()).willReturn(1L);
            given(like.getShow()).willReturn(show);
            given(show.getId()).willReturn(1L);

            // when
            likeCommandService.deleteLike(userId, showId, likeId);

            // then
            verify(likeRepository, times(1)).findWithUserAndShowById(any());
            verify(likeRepository, times(1)).delete(any());
            verify(like.getUser(), times(1)).getId();
            verify(like.getShow(), times(1)).getId();
        }
    }
}


