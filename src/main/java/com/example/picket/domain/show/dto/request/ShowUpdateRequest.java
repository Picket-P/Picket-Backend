package com.example.picket.domain.show.dto.request;

import com.example.picket.common.enums.Category;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShowUpdateRequest {

    @NotBlank(message = "공연 제목은 필수 입력 값입니다.")
    private String title;

    @NotBlank(message = "포스터 URL은 필수 입력 값입니다.")
    private String posterUrl;

    @NotNull(message = "카테고리는 필수 입력 값입니다.")
    private Category category;

    @NotBlank(message = "공연 설명은 필수 입력 값입니다.")
    private String description;

    @NotBlank(message = "공연 장소는 필수 입력 값입니다.")
    private String location;

    @NotNull(message = "예매 시작일은 필수 입력 값입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reservationStart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reservationEnd;

    private Integer ticketsLimitPerUser;

}

