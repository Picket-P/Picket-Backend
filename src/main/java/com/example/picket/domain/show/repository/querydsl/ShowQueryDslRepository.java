package com.example.picket.domain.show.repository.querydsl;

import com.example.picket.common.enums.Category;
import com.example.picket.domain.show.dto.response.ShowDetailResponse;
import com.example.picket.domain.show.dto.response.ShowResponse;

import java.util.List;
import java.util.Optional;

public interface ShowQueryDslRepository {

    List<ShowResponse> getShowsResponse(Category category, String sortBy, String order);

    Optional<ShowDetailResponse> getShowDetailResponseById(Long showId);

}
