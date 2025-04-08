package com.example.picket.domain.show.dto.response;

import com.example.picket.common.enums.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ShowDetailResponse {

    private final Long id;
    private final Long directorId;
    private final String title;
    private final String posterUrl;
    private final Category category;
    private final String description;
    private final String location;
    private final LocalDateTime reservationStart;
    private final LocalDateTime reservationEnd;
    private final Integer ticketsLimitPerUser;
    private final List<ShowDateDetailResponse> showDates;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime modifiedAt;

    private ShowDetailResponse(Long id, Long directorId, String title, String posterUrl, Category category,
                               String description, String location, LocalDateTime reservationStart, LocalDateTime reservationEnd, Integer ticketsLimitPerUser,
                               List<ShowDateDetailResponse> showDates, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.directorId = directorId;
        this.title = title;
        this.posterUrl = posterUrl;
        this.category = category;
        this.description = description;
        this.location = location;
        this.reservationStart = reservationStart;
        this.reservationEnd = reservationEnd;
        this.ticketsLimitPerUser = ticketsLimitPerUser;
        this.showDates = showDates;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static ShowDetailResponse toDto(Long id, Long directorId, String title, String posterUrl, Category category,
                                           String description, String location, LocalDateTime reservationStart, LocalDateTime reservationEnd, Integer ticketsLimitPerUser,
                                           List<ShowDateDetailResponse> showDates, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return new ShowDetailResponse(
            id,
            directorId,
            title,
            posterUrl,
            category,
            description,
            location,
            reservationStart,
            reservationEnd,
            ticketsLimitPerUser,
            showDates,
            createdAt,
            modifiedAt
        );
    }
}
