package com.example.picket.domain.ranking.dto.response;

import com.example.picket.common.enums.ShowStatus;
import com.example.picket.domain.ranking.entity.HotShow;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class HotShowResponse {
    private final Long showId;
    private final String title;
    private final int viewCount;
    private final ShowStatus status;
    private final LocalDateTime createdAt;

    private HotShowResponse(Long showId, String title, int viewCount, ShowStatus status, LocalDateTime createdAt) {
        this.showId = showId;
        this.title = title;
        this.viewCount = viewCount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static HotShowResponse toDto(HotShow hotShow) {
        return new HotShowResponse(hotShow.getShowId(), hotShow.getTitle(), hotShow.getViewCount(), hotShow.getStatus(), hotShow.getCreatedAt());
    }
}
