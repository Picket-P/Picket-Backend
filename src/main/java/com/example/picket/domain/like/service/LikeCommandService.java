package com.example.picket.domain.like.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.domain.like.entity.Like;
import com.example.picket.domain.like.repository.LikeRepository;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.ShowRepository;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeCommandService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ShowRepository showRepository;


    public void createLike(Long userId, Long showId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(
                ErrorCode.USER_NOT_FOUND));

        Show show = showRepository.findById(showId).orElseThrow(() -> new CustomException(ErrorCode.SHOW_NOT_FOUND));

        if (likeRepository.existsByUserIdAndShowId(user.getId(), show.getId())) {
            throw new CustomException(ErrorCode.LIKE_ALREADY_EXIST);
        }

        Like like = Like.toEntity(show, user);
        
        likeRepository.save(like);
    }

    public void deleteLike(Long userId, Long showId, Long likeId) {
        Like like = likeRepository.findWithUserAndShowById(likeId)
                .orElseThrow(() -> new CustomException(ErrorCode.LIKE_NOT_FOUND));

        if (!Objects.equals(like.getUser().getId(), userId)) {
            throw new CustomException(ErrorCode.LIKE_REQUEST_USER_MISMATCH);
        }

        if (!Objects.equals(like.getShow().getId(), showId)) {
            throw new CustomException(ErrorCode.LIKE_REQUEST_SHOW_MISMATCH);
        }

        likeRepository.delete(like);
    }
}
