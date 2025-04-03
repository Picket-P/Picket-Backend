package com.example.picket.domain.show.dto;

import com.example.picket.common.enums.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ShowCreateRequest {

    @NotNull
    private Long directorId;

    @NotBlank
    private String title;

    @NotBlank
    private String posterUrl;

    @NotNull
    private Category category;

    @NotBlank
    private String description;

    @NotBlank
    private String location;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reservationStart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reservationEnd; // Optional, null 이면 공연시작 전날 자정으로 set

    private Integer ticketsLimitPerUser; // null 이면 제한 없음

    @NotNull
    private List<ShowDateRequest> dates;

    public LocalDateTime getReservationEnd() {
        if (reservationEnd == null && !dates.isEmpty()) {
            return dates.get(0).getDate().atStartOfDay();
        }
        return reservationEnd;
    }
}
