package com.example.picket.domain.images.entity;

import com.example.picket.common.entity.BaseEntity;
import com.example.picket.domain.images.dto.response.ImageResponse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String fileFormat;

    @Column
    private Long userId;

    private UserImage(String imageUrl, String fileFormat, Long userId) {
        this.imageUrl = imageUrl;
        this.fileFormat = fileFormat;
        this.userId = userId;
    }

    public static UserImage create(ImageResponse imageResponse, Long userId) {
        return new UserImage(imageResponse.getImageUrl(), imageResponse.getFileFormat(), userId);
    }

    public void updateUser(Long userId) {
        this.userId = userId;
    }

}