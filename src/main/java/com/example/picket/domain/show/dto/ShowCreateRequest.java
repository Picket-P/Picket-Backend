package com.example.picket.domain.show.dto;

import com.example.picket.common.enums.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ShowCreateRequest {

    @NotNull
    private Long directorId;

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
    private LocalDateTime reservationEnd; // Optional, null 이면 공연시작 전날 자정으로 set

    private Integer ticketsLimitPerUser; // null 이면 제한 없음

    @NotNull(message = "공연 날짜 목록은 필수 입력 값입니다.")
    @Size(min = 1, message = "공연 날짜 목록은 최소 1개 이상이어야 합니다.")
    private List<ShowDateRequest> dates;

    public LocalDateTime getReservationEnd() {
        if (reservationEnd == null && !dates.isEmpty()) {
            return dates.get(0).getDate().atStartOfDay();
        }
        return reservationEnd;
    }

    @AssertTrue(message = "예매 종료일은 예매 시작일보다 같거나 이후여야 합니다.")
    public boolean isValidReservationPeriod() {
        return reservationStart == null || reservationEnd == null || !reservationEnd.isBefore(reservationStart);
    }
}
