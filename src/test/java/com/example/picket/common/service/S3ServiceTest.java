package com.example.picket.common.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.images.dto.response.ImageResponse;
import jakarta.servlet.ServletInputStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    S3Client s3Client;

    @Mock
    MultipartFile multipartFile;

    @InjectMocks
    S3Service s3Service;

    private static final String VALID_CONTENT_TYPE = "image/jpeg";
    private static final long MAX_FILE_SIZE = 8 * 1024 * 1024;
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    @Nested
    class 이미지_업로드_테스트 {

        @Test
        void 이미지_업로드_시_확장자가_null일_경우_실패() {
            // given
            given(multipartFile.getContentType()).willReturn(null);

            // when & then
            assertThatThrownBy(() -> s3Service.upload(multipartFile))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("지원되지 않는 파일 형식입니다. 허용 Content-Type: " + ALLOWED_CONTENT_TYPES);
        }

        @Test
        void 이미지_업로드_시_확장자가_지원되는_타입이_아닐_경우_실패() {
            // given
            given(multipartFile.getContentType()).willReturn(null);

            // when & then
            assertThatThrownBy(() -> s3Service.upload(multipartFile))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("지원되지 않는 파일 형식입니다. 허용 Content-Type: " + ALLOWED_CONTENT_TYPES);
        }

        @Test
        void 이미지_업로드_시_파일_크기가_8MB보다_클_경우_실패() {
            // given
            given(multipartFile.getContentType()).willReturn("image/jpeg");
            given(multipartFile.getSize()).willReturn((long) (10 * 1024 * 1024));
            // when & then
            assertThatThrownBy(() -> s3Service.upload(multipartFile))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("파일 크기가 8MB를 초과했습니다. 최대 크기: " + MAX_FILE_SIZE / (1024 * 1024) + "MB");
        }

        @Test
        void 이미지_업로드_시_파일_업로드_중_SdkServiceException_발생할_경우_실패() throws IOException {
            // given
            BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpeg", baos);
            byte[] imageBytes = baos.toByteArray();

            MultipartFile spyFile = Mockito.spy(MultipartFile.class);
            ServletInputStream servletInputStream = new DelegatingServletInputStream(
                    new ByteArrayInputStream(imageBytes));
            doReturn("image/jpeg").when(spyFile).getContentType();
            doReturn(servletInputStream).when(spyFile).getInputStream();

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(SdkServiceException.builder().message("S3 upload failed").build());

            // when & then
            assertThatThrownBy(() -> s3Service.upload(spyFile))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("S3 업로드 중 오류가 발생했습니다");
            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
            verify(spyFile, times(1)).getInputStream();
        }

        @Test
        void 이미지_업로드_시_파일_업로드_중_IOException_발생할_경우_실패() throws IOException {
            // given
            given(multipartFile.getContentType()).willReturn("image/jpeg");
            given(multipartFile.getSize()).willReturn((long) (5 * 1024 * 1024));
            when(multipartFile.getInputStream()).thenThrow(new IOException("Input stream error"));

            // when & then
            assertThatThrownBy(() -> s3Service.upload(multipartFile))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("파일 업로드 중 서버 오류가 발생했습니다: Input stream error");
        }

        @Test
        void 이미지_업로드_성공() throws IOException {
            // given
            BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpeg", baos);
            byte[] imageBytes = baos.toByteArray();

            MultipartFile spyFile = Mockito.spy(MultipartFile.class);
            ServletInputStream servletInputStream = new DelegatingServletInputStream(
                    new ByteArrayInputStream(imageBytes));
            doReturn("image/jpeg").when(spyFile).getContentType();
            doReturn(servletInputStream).when(spyFile).getInputStream();

            // when
            ImageResponse imageResponse = s3Service.upload(spyFile);

            // then
            assertThat(imageResponse).isNotNull();
            assertThat(imageResponse.getImageUrl()).startsWith(
                    String.format("https://null.s3.null.amazonaws.com/images/"));
            assertThat(imageResponse.getFileFormat()).isEqualTo(VALID_CONTENT_TYPE);
            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
            verify(spyFile, times(1)).getInputStream();
        }

    }

    @Nested
    class 이미지_삭제_테스트 {

        @Test
        void 이미지_삭제_시_파일_삭제_중_SdkServiceException_발생할_경우_실패() {
            // given
            String imageUrl = "images/test-image.jpg";
            doThrow(SdkServiceException.builder().message("S3 delete failed").build())
                    .when(s3Client).deleteObject(any(DeleteObjectRequest.class));

            // when & then
            assertThatThrownBy(() -> s3Service.delete(imageUrl))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("파일 삭제 중 오류가 발생했습니다");
            verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
        }

        @Test
        void 이미지_삭제_성공() {
            // given
            String imageUrl = "images/test-image.jpg";
            when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(any(DeleteObjectResponse.class));

            // when
            s3Service.delete(imageUrl);

            // then
            verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
        }

    }
}