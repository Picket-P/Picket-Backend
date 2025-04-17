package com.example.picket.domain.ranking.entity;

import com.example.picket.common.enums.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PopularKeyword {
    private Category category;
    private Long keywordCount;
    private LocalDateTime createdAt;

    private PopularKeyword(Category category, Long keywordCount, LocalDateTime createdAt) {
        this.category = category;
        this.keywordCount = keywordCount;
        this.createdAt = createdAt;
    }

    public static PopularKeyword toEntity(Category category, Long keywordCount, LocalDateTime createdAt) {
        return new PopularKeyword(category, keywordCount, createdAt);
    }
}
