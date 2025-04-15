package com.example.picket.domain.like.repository;

import com.example.picket.domain.like.entity.Like;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    @Query("SELECT l.show.id, l.show.title, COUNT(l) as likeCount " +
            "FROM Like l GROUP BY l.show.id, l.show.title " +
            "ORDER BY likeCount DESC")
    List<Object[]> findTop10ShowsByLikeCount();
}
