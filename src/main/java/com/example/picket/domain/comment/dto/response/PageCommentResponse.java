package com.example.picket.domain.comment.dto.response;

import com.example.picket.domain.comment.entity.Comment;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
public class PageCommentResponse {

    private final List<CommentResponse> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean last;

    public PageCommentResponse(List<CommentResponse> content, int page, int size,
                                long totalElements, int totalPages, boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }

    public static PageCommentResponse toDto(Page<Comment> comments, Function<Comment, Boolean> ticketChecker) {
        List<CommentResponse> responseList = comments.getContent().stream()
                .map(comment -> CommentResponse.toDto(comment, ticketChecker.apply(comment)))
                .toList();

        return new PageCommentResponse(
                responseList, comments.getNumber(), comments.getSize(), comments.getTotalElements(), comments.getTotalPages(), comments.isLast()
        );
    }
}
