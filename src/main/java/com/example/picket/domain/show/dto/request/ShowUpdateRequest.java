package com.example.picket.domain.show.dto.request;

import com.example.picket.common.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShowUpdateRequest {

    @Schema(description = "제목", example = "수정한 공연 제목")
    @NotBlank(message = "공연 제목은 필수 입력 값입니다.")
    private String title;

    @Schema(description = "제목", example = "http://aws.s3/project/changePoster.jpg")
    @NotBlank(message = "포스터 URL은 필수 입력 값입니다.")
    private String posterUrl;

    @Schema(description = "카테고리", example = "CONCERT")
    @NotNull(message = "카테고리는 필수 입력 값입니다.")
    private Category category;

    @Schema(description = "공연 설명", example = "수정한 공연 설명입니다.")
    @NotBlank(message = "공연 설명은 필수 입력 값입니다.")
    private String description;

    @Schema(description = "공연 설명", example = "경기도 어딘가")
    @NotBlank(message = "공연 장소는 필수 입력 값입니다.")
    private String location;

    @Schema(description = "예매 시작일 (형식: yyyy-MM-dd'T'HH:mm:ss)", example = "2025-04-09T00:00:00.000Z")
    @NotNull(message = "예매 시작일은 필수 입력 값입니다.")
    private LocalDateTime reservationStart;

    @Schema(description = "예매 종료일 (형식: yyyy-MM-dd'T'HH:mm:ss, null이면 공연시작 전날 자정으로 설정)", example = "2025-05-09T00:00:00.000Z")
    private LocalDateTime reservationEnd;

    @Schema(description = "인당 최대 구매 가능 티켓수", example = "100")
    private Integer ticketsLimitPerUser;

}

