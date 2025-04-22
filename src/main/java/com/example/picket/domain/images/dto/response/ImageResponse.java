package com.example.picket.domain.images.dto.response;

import com.example.picket.common.enums.ImageStatus;
import lombok.Getter;

@Getter
public class ImageResponse {
    private final String imageUrl;
    private final String fileFormat;
    private final ImageStatus imageStatus;

    private ImageResponse(String imageUrl, String fileFormat, ImageStatus imageStatus) {
        this.imageUrl = imageUrl;
        this.fileFormat = fileFormat;
        this.imageStatus = imageStatus;
    }

    public static ImageResponse toDto(String imageUrl, String fileFormat, ImageStatus imageStatus) {
        return new ImageResponse(imageUrl, fileFormat, imageStatus);
    }

}
