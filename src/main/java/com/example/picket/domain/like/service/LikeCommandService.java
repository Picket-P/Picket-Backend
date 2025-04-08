package com.example.picket.domain.like.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.like.entity.Like;
import com.example.picket.domain.like.repository.LikeRepository;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.service.ShowQueryService;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserQueryService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeCommandService {

    private final LikeRepository likeRepository;
    private final UserQueryService userQueryService;
    private final ShowQueryService showQueryService;

    public void createLike(Long userId, Long showId) {
        User user = userQueryService.getUser(userId);

        Show show = showQueryService.getShow(showId);

        if (likeRepository.existsByUserIdAndShowId(user.getId(), show.getId())) {
            throw new CustomException(BAD_REQUEST, "이미 해당 좋아요를 눌렀습니다.");
        }

        Like like = Like.toEntity(show, user);

        likeRepository.save(like);
    }

    public void deleteLike(Long userId, Long showId, Long likeId) {
        Like like = likeRepository.findWithUserAndShowById(likeId)
                .orElseThrow(() -> new CustomException(NOT_FOUND, "해당 좋아요를 찾을 수 없습니다."));

        if (!Objects.equals(like.getUser().getId(), userId)) {
            throw new CustomException(BAD_REQUEST, "해당 좋아요를 누른 사용자와 요청한 사용자가 다릅니다.");
        }

        if (!Objects.equals(like.getShow().getId(), showId)) {
            throw new CustomException(BAD_REQUEST, "해당 좋아요가 눌린 공연과 요청된 공연이 다릅니다.");
        }

        likeRepository.delete(like);
    }
}
