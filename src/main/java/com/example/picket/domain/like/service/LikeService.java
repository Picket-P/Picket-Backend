package com.example.picket.domain.like.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.domain.like.dto.response.LikeResponse;
import com.example.picket.domain.like.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;

    public Page<LikeResponse> getLikes(AuthUser authUser, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return likeRepository.findLikesWithShowByUserId(authUser.getId(), pageable);
    }
}
