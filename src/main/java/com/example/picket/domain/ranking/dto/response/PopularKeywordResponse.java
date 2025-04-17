package com.example.picket.domain.ranking.dto.response;

import com.example.picket.common.enums.Category;
import com.example.picket.domain.ranking.entity.PopularKeyword;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PopularKeywordResponse {
    private final Category category;
    private final int keywordCount;
    private final LocalDateTime createdAt;

    private PopularKeywordResponse(Category category, int keywordCount, LocalDateTime createdAt) {
        this.category = category;
        this.keywordCount = keywordCount;
        this.createdAt = createdAt;
    }

    public static PopularKeywordResponse toDto(PopularKeyword popularKeyword) {
        return new PopularKeywordResponse(popularKeyword.getCategory(), popularKeyword.getKeywordCount(), popularKeyword.getCreatedAt());
    }
}
