package com.example.picket.domain.images.repository;

import com.example.picket.domain.images.entity.ShowImage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowImageRepository extends JpaRepository<ShowImage, Long> {

    Optional<ShowImage> findByImageUrl(String imageUrl);
}
