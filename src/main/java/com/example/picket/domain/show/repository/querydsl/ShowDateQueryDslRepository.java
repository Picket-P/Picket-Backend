package com.example.picket.domain.show.repository.querydsl;

import com.example.picket.domain.show.dto.response.ShowDateDetailResponse;

import java.util.List;

public interface ShowDateQueryDslRepository {

    List<ShowDateDetailResponse> getShowDateDetailResponseById(Long showId);

}
