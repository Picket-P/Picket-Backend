package com.example.picket.domain.images.entity;

import com.example.picket.common.entity.BaseEntity;
import com.example.picket.domain.images.dto.response.ImageResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column
    private Long showId;

    private ShowImage(String imageUrl, String fileFormat, Long showId) {
        this.imageUrl = imageUrl;
        this.fileFormat = fileFormat;
        this.showId = showId;
    }

    public static ShowImage toEntity(ImageResponse imageResponse,
                                     Long showId) {
        return new ShowImage(imageResponse.getImageUrl(), imageResponse.getFileFormat(), showId);
    }

    public void updateShow(Long showId) {
        this.showId = showId;
    }

}