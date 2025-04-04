package com.example.picket.domain.like.repository;

import com.example.picket.domain.like.dto.response.LikeResponse;
import com.example.picket.domain.like.entity.Like;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LikeRepository extends JpaRepository<Like, Long> {

    @Query("SELECT new com.example.picket.domain.like.dto.response.LikeResponse(s.id, s.title, s.category, s.description) " +
            "FROM Like l JOIN l.show s WHERE l.user.id = :id")
    Page<LikeResponse> findLikesWithShowByUserId(Long id, Pageable pageable);

    @EntityGraph(
            attributePaths = {
                    "user",
                    "show"
            }
    )
    Optional<Like> findWithUserAndShowById(Long likeId);

    boolean existsByUserIdAndShowId(Long userId, Long showId);
}
