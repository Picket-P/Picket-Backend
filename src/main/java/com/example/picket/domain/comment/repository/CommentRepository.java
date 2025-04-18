package com.example.picket.domain.comment.repository;

import com.example.picket.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndShowIdAndUserId(Long commentId, Long showId, Long userId);
    Optional<Comment> findByIdAndShowId(Long commentId, Long showId);
    @EntityGraph(attributePaths = {"user", "show"})
    Page<Comment> findByShowIdAndDeletedAtIsNull(Long showId, Pageable pageable);
}
