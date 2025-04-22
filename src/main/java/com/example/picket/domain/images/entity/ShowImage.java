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
@Table(name = "show_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowImage extends BaseEntity {

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
    private Long showId;

    private ShowImage(String imageUrl, String fileFormat, ImageStatus imageStatus, Long showId) {
        this.imageUrl = imageUrl;
        this.fileFormat = fileFormat;
        this.imageStatus = imageStatus;
        this.showId = showId;
    }

    public static ShowImage toEntity(ImageResponse imageResponse,
                                     Long showId) {
        return new ShowImage(imageResponse.getImageUrl(), imageResponse.getFileFormat(), imageResponse.getImageStatus(),
                showId);
    }

    public void updateShow(Long showId) {
        this.showId = showId;
        this.imageStatus = ImageStatus.CREATED;
    }
}