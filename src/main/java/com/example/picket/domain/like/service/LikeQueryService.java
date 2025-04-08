package com.example.picket.domain.like.service;

import com.example.picket.domain.like.entity.Like;
import com.example.picket.domain.like.repository.LikeRepository;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.service.ShowQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeQueryService {

    private final LikeRepository likeRepository;
    private final ShowQueryService showQueryService;

    public List<Show> getLikes(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Like> likeList = likeRepository.findLikesByUserId(userId, pageable);
        List<Long> showId = likeList.stream().map(
                like -> {
                    return like.getShow().getId();
                }
        ).toList();

        return showQueryService.findAllById(showId);
    }
}
