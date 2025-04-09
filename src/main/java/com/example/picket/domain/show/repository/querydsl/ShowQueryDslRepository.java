package com.example.picket.domain.show.repository.querydsl;

import com.example.picket.common.enums.Category;
import com.example.picket.domain.show.dto.response.ShowDetailResponse;
import com.example.picket.domain.show.dto.response.ShowResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ShowQueryDslRepository {

    Page<ShowResponse> getShowsResponse(Category category, String sortBy, String order, Pageable pageable);

    Optional<ShowDetailResponse> getShowDetailResponseById(Long showId);

}
