package com.example.picket.domain.ranking.entity;

import com.example.picket.common.enums.ShowStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class HotShow {
    private Long showId;
    private String title;
    private int viewCount;
    private ShowStatus status;
    private LocalDateTime createdAt;

    private HotShow(Long showId, String title, int viewCount, ShowStatus status, LocalDateTime createdAt) {
        this.showId = showId;
        this.title = title;
        this.viewCount = viewCount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static HotShow toEntity(Long showId, String title, int viewCount, ShowStatus status, LocalDateTime createdAt) {
        return new HotShow(showId, title, viewCount, status, createdAt);
    }
}
