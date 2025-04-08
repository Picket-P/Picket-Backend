package com.example.picket.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentRequest {

    @Schema(description = "댓글 내용", example = "공연에대한 댓글입니다.")
    @NotBlank
    private String content;
}
