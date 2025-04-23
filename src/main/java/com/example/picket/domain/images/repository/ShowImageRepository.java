package com.example.picket.domain.images.repository;

import com.example.picket.domain.images.entity.ShowImage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ShowImageRepository extends JpaRepository<ShowImage, Long> {

    Optional<ShowImage> findByImageUrl(String imageUrl);

    List<ShowImage> findByShowIdIsNullAndCreatedAtBefore(LocalDateTime dayBefore);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ShowImage si WHERE si.showId IS NULL AND si.createdAt < :dayBefore")
    void deleteByShowIdIsNullAndCreatedAtBefore(LocalDateTime dayBefore);
}
