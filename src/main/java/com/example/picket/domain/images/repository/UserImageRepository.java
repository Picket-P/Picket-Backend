package com.example.picket.domain.images.repository;

import com.example.picket.domain.images.entity.UserImage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {

    Optional<UserImage> findByImageUrl(String profileUrl);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserImage ui WHERE ui.userId IS NULL AND ui.createdAt < :dayBefore")
    void deleteByUserIdIsNullAndCreatedAtBefore(LocalDateTime dayBefore);

    List<UserImage> findByUserIdIsNullAndCreatedAtBefore(LocalDateTime dayBefore);
}
