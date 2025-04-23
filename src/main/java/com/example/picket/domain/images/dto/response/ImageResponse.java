package com.example.picket.domain.images.dto.response;

import lombok.Getter;

@Getter
public class ImageResponse {
    private final String imageUrl;
    private final String fileFormat;

    private ImageResponse(String imageUrl, String fileFormat) {
        this.imageUrl = imageUrl;
        this.fileFormat = fileFormat;
    }

    public static ImageResponse toDto(String imageUrl, String fileFormat) {
        return new ImageResponse(imageUrl, fileFormat);
    }

}
