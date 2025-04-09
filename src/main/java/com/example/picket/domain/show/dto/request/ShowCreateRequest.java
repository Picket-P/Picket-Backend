package com.example.picket.domain.show.dto.request;

import com.example.picket.common.enums.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShowCreateRequest {

    @Schema(description = "제목", example = "공연 제목")
    @NotBlank(message = "공연 제목은 필수 입력 값입니다.")
    private String title;

    @Schema(description = "포스터 URL", example = "http://aws.s3/project/poster.jpg")
    @NotBlank(message = "포스터 URL은 필수 입력 값입니다.")
    private String posterUrl;

    @Schema(description = "카테고리", example = "MUSICAL")
    @NotNull(message = "카테고리는 필수 입력 값입니다.")
    private Category category;

    @Schema(description = "공연 설명", example = "공연 설명입니다.")
    @NotBlank(message = "공연 설명은 필수 입력 값입니다.")
    private String description;

    @Schema(description = "공연 장소", example = "서울시 어딘가")
    @NotBlank(message = "공연 장소는 필수 입력 값입니다.")
    private String location;

    @Schema(description = "예매 시작일 (형식: yyyy-MM-dd'T'HH:mm:ss)", example = "2025-05-01T00:00:00")
    @NotNull(message = "예매 시작일은 필수 입력 값입니다.")
    private LocalDateTime reservationStart;

    @Schema(description = "예매 종료일 (형식: yyyy-MM-dd'T'HH:mm:ss, null이면 공연시작 전날 자정으로 설정)", example = "2025-05-10T00:00:00")
    private LocalDateTime reservationEnd;

    @Schema(description = "인당 최대 구매 가능 티켓수", example = "2")
    private Integer ticketsLimitPerUser; // null 이면 제한 없음

    @Schema(description = "공연 날짜 리스트", example = "[{\"date\": \"2025-05-12\", \"startTime\": \"14:00:00\", \"endTime\": \"16:00:00\", \"totalSeatCount\": 100, \"seatCreateRequests\": [{\"grade\": \"VIP\", \"seatCount\": 100, \"price\": 50000}]}]")
    @NotNull(message = "공연 날짜 목록은 필수 입력 값입니다.")
    @Size(min = 1, message = "공연 날짜 목록은 최소 1개 이상이어야 합니다.")
    private List<ShowDateRequest> dates;

    // 예약 종료일 기본값 처리
    public LocalDateTime getReservationEnd() {
        if (reservationEnd == null && !dates.isEmpty()) {
            return dates.get(0).getDate().atStartOfDay();
        }
        return reservationEnd;
    }

    // 예약 종료일이 시작일보다 이전인지 검증
    @AssertTrue(message = "예매 종료일은 예매 시작일보다 같거나 이후여야 합니다.")
    public boolean isValidReservationPeriod() {
        return reservationEnd == null || reservationEnd.isAfter(reservationStart) || reservationEnd.isEqual(
                reservationStart);
    }

}
