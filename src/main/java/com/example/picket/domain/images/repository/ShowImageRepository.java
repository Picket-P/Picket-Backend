package com.example.picket.domain.images.repository;

import com.example.picket.domain.images.entity.ShowImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShowImageRepository extends JpaRepository<ShowImage, Long> {

    Optional<ShowImage> findByImageUrl(String imageUrl);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ShowImage si WHERE (si.showId IS NULL AND si.createdAt < :dayBefore) OR si.deletedAt IS NOT NULL")
    void deletedOrphanShowImage(LocalDateTime dayBefore);

    @Query("SELECT s FROM ShowImage s WHERE (s.showId IS NULL AND s.createdAt < :dayBefore) OR s.deletedAt IS NOT NULL")
    List<ShowImage> findByOrphan(LocalDateTime dayBefore);

}
