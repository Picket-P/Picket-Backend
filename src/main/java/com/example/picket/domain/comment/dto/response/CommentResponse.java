package com.example.picket.domain.comment.dto.response;

import com.example.picket.domain.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {

    private final Long userId;
    private final String nickname;
    private final Long id;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final Boolean isTicketBuyer;

    @Builder
    public CommentResponse(Long userId,
                           String nickname,
                           Long id,
                           String content,
                           LocalDateTime createdAt,
                           LocalDateTime modifiedAt,
                           Boolean isTicketBuyer) {
        this.userId = userId;
        this.nickname = nickname;
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.isTicketBuyer = isTicketBuyer;
    }


    public static CommentResponse from (Comment comment, Boolean isTicketBuyer) {
        return CommentResponse.builder()
                .userId(comment.getUser().getId())
                .nickname(comment.getUser().getNickname())
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .modifiedAt(comment.getModifiedAt())
                .isTicketBuyer(isTicketBuyer)
                .build();
    }
}
