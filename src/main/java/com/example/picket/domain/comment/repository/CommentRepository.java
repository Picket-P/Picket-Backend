package com.example.picket.domain.comment.repository;

import com.example.picket.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndShowIdAndUserId(Long commentId, Long showId, Long userId);
}
