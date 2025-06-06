package com.example.picket.domain.comment.service;

import com.example.picket.domain.comment.entity.Comment;
import com.example.picket.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public Page<Comment> getComments(Long showId, Pageable pageable) {
        return commentRepository.findByShowIdAndDeletedAtIsNull(showId, pageable);
    }
}
