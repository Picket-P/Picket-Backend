package com.example.picket.domain.show.repository;

import com.example.picket.domain.show.dto.response.ShowDetailResponse;

import java.util.Optional;

public interface ShowQueryDslRepository {

    Optional<ShowDetailResponse> getShowDetailResponseById(Long showId);

}
