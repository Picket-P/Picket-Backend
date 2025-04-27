package com.example.picket.domain.images.repository;

import com.example.picket.domain.images.entity.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {

    Optional<UserImage> findByImageUrl(String profileUrl);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserImage ui WHERE (ui.userId IS NULL AND ui.createdAt < :dayBefore) OR ui.deletedAt IS NOT NULL")
    void deletedOrphanShowImage(LocalDateTime dayBefore);

    @Query("SELECT u FROM UserImage u WHERE (u.userId IS NULL AND u.createdAt < :dayBefore) OR u.deletedAt IS NOT NULL")
    List<UserImage> findByOrphan(LocalDateTime dayBefore);
}
