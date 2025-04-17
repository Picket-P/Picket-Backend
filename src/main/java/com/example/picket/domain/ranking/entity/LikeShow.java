package com.example.picket.domain.ranking.entity;

import com.example.picket.common.enums.ShowStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
public class LikeShow {
    private Long showId;
    private String title;
    private int likeCount;
    private ShowStatus status;
    private LocalDateTime createdAt;

    private LikeShow(Long showId, String title, int likeCount, ShowStatus status, LocalDateTime createdAt) {
        this.showId = showId;
        this.title = title;
        this.likeCount = likeCount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static LikeShow toEntity(Long showId, String title, int likeCount, ShowStatus status, LocalDateTime createdAt) {
        LikeShow show = new LikeShow();
        show.showId = showId;
        show.title = title;
        show.likeCount = likeCount;
        show.status = status;
        show.createdAt = createdAt;
        return show;
    }
}
