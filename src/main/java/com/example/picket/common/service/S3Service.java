package com.example.picket.common.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.images.dto.response.ImageResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );
    private static final long MAX_FILE_SIZE = 8 * 1024 * 1024;

    private final S3Client s3Client;

    @Getter
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public ImageResponse upload(MultipartFile multipartFile) {

        // 파일 null 체크
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new CustomException(BAD_REQUEST, "업로드할 파일이 없습니다.");
        }

        // 이미지 확장자 검증
        String contentType = multipartFile.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomException(BAD_REQUEST, "지원되지 않는 파일 형식입니다. 허용 Content-Type: " + ALLOWED_CONTENT_TYPES);
        }

        // 파일 크기 검증
        long contentLength = multipartFile.getSize();
        if (contentLength > MAX_FILE_SIZE) {
            throw new CustomException(BAD_REQUEST,
                    "파일 크기가 8MB를 초과했습니다. 최대 크기: " + MAX_FILE_SIZE / (1024 * 1024) + "MB");
        }

        // 고유한 파일 이름 생성
        String key = "images/" + UUID.randomUUID();

        try {
            // S3 업로드 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            // InputStream을 사용하여 S3에 업로드
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(), contentLength));
            log.info("파일 업로드 성공: bucket={}, key={}", bucket, key);
            return ImageResponse.of(getPublicUrl(key), contentType);
        } catch (IOException e) {
            log.error("파일 업로드 실패: bucket={}, key={}, cause={}", bucket, key, e.getMessage(), e);
            throw new CustomException(INTERNAL_SERVER_ERROR, "파일 업로드 중 서버 오류가 발생했습니다: " + e.getMessage());
        } catch (SdkServiceException e) {
            log.error("파일 업로드 실패: bucket={}, key={}, cause={}", bucket, key, e.getMessage(), e);
            throw new CustomException(INTERNAL_SERVER_ERROR, "S3 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public void delete(String imageUrl) {
        try {
            String key = extractKeyFromUrl(imageUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("파일 삭제 성공: bucket={}, key={}", bucket, key);
        } catch (SdkServiceException e) {
            log.error("파일 삭제 실패: bucket={}, key={}, cause={}", bucket, imageUrl, e.getMessage(), e);
            throw new CustomException(INTERNAL_SERVER_ERROR, "파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String getPublicUrl(String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fileName);
    }

    private String extractKeyFromUrl(String imageUrl) {
        // URL에서 key 부분만 추출
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);
        if (imageUrl.startsWith(prefix)) {
            return imageUrl.substring(prefix.length());
        }
        // 예상치 못한 URL 형식일 경우 예외 처리
        throw new CustomException(BAD_REQUEST, "잘못된 이미지 URL 형식입니다: " + imageUrl);
    }
}