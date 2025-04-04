package com.example.picket.domain.like.dto.response;

import com.example.picket.common.enums.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LikeResponse {

    private final Long showId;
    private final String showTitle;
    private final Category category;
    private final String description;

    @Builder
    private LikeResponse(Long showId, String showTitle, Category category, String description) {
        this.showId = showId;
        this.showTitle = showTitle;
        this.category = category;
        this.description = description;
    }
}
