package com.example.picket.domain.images.entity;

import com.example.picket.common.entity.BaseEntity;
import com.example.picket.common.enums.ImageStatus;
import com.example.picket.domain.images.dto.response.ImageResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageStatus imageStatus;

    @Column
    private Long userId;

    private UserImage(String imageUrl, String fileFormat, ImageStatus imageStatus, Long userId) {
        this.imageUrl = imageUrl;
        this.fileFormat = fileFormat;
        this.imageStatus = imageStatus;
        this.userId = userId;
    }

    public static UserImage toEntity(ImageResponse imageResponse, Long userId) {
        return new UserImage(imageResponse.getImageUrl(), imageResponse.getFileFormat(), imageResponse.getImageStatus(),
                userId);
    }

    public void updateUser(Long userId) {
        this.userId = userId;
        this.imageStatus = ImageStatus.CREATED;
    }
}