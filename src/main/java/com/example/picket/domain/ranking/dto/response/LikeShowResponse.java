package com.example.picket.domain.ranking.dto.response;

import com.example.picket.common.enums.ShowStatus;
import com.example.picket.domain.ranking.entity.LikeShow;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LikeShowResponse {
    private final Long showId;
    private final String title;
    private final Long likeCount;
    private final ShowStatus status;
    private final LocalDateTime createdAt;

    private LikeShowResponse(Long showId, String title, Long likeCount, ShowStatus status, LocalDateTime createdAt) {
        this.showId = showId;
        this.title = title;
        this.likeCount = likeCount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static LikeShowResponse toDto(LikeShow likeShow) {
        return new LikeShowResponse(likeShow.getShowId(), likeShow.getTitle(), likeShow.getLikeCount(), likeShow.getStatus(), likeShow.getCreatedAt());
    }
}
