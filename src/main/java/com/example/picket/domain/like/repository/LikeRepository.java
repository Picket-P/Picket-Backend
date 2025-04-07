package com.example.picket.domain.like.repository;

import com.example.picket.domain.like.entity.Like;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {

    @EntityGraph(
            attributePaths = {
                    "show"
            }
    )
    List<Like> findLikesByUserId(Long id, Pageable pageable);

    @EntityGraph(
            attributePaths = {
                    "user",
                    "show"
            }
    )
    Optional<Like> findWithUserAndShowById(Long likeId);

    boolean existsByUserIdAndShowId(Long userId, Long showId);
}
