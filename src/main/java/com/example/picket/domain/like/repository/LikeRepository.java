package com.example.picket.domain.like.repository;

import com.example.picket.common.enums.ShowStatus;
import com.example.picket.domain.like.entity.Like;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT s.id, s.title, COUNT(l.id), s.status " +
            "FROM Show s LEFT JOIN Like l ON s.id = l.show.id " +
            "WHERE s.status IN :statuses " +
            "GROUP BY s.id, s.title, s.status " +
            "ORDER BY COUNT(l.id) DESC")
    List<Object[]> findTop10ShowsByLikeCountAndStatusIn(@Param("statuses") ShowStatus[] statuses, Pageable pageable);
}
