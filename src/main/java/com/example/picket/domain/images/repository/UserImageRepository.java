package com.example.picket.domain.images.repository;

import com.example.picket.domain.images.entity.UserImage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {

    Optional<UserImage> findByImageUrl(String profileUrl);
}
