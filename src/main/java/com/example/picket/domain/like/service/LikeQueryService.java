package com.example.picket.domain.like.service;

import com.example.picket.domain.like.dto.response.LikeResponse;
import com.example.picket.domain.like.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeQueryService {

    private final LikeRepository likeRepository;

    public Page<LikeResponse> getLikes(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return likeRepository.findLikesWithShowByUserId(userId, pageable);
    }
}
