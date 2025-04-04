package com.example.picket.domain.show.dto;

import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class ShowResponse {

    private Long id;
    private Long directorId;
    private String title;
    private String posterUrl;
    private String category;
    private String description;
    private String location;
    private String reservationStart;
    private String reservationEnd;
    private Integer ticketsLimitPerUser;
    private List<ShowDateResponse> showDates;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;

    public ShowResponse(Show show, List<ShowDate> showDates) {
        this.id = show.getId();
        this.directorId = show.getDirectorId();
        this.title = show.getTitle();
        this.posterUrl = show.getPosterUrl();
        this.category = show.getCategory().name();
        this.description = show.getDescription();
        this.location = show.getLocation();
        this.reservationStart = show.getReservationStart().toString();
        this.reservationEnd = show.getReservationEnd().toString();
        this.ticketsLimitPerUser = show.getTicketsLimitPerUser();
        this.showDates = showDates.stream()
                .map(ShowDateResponse::from)
                .collect(Collectors.toList());

        this.createdAt = show.getCreatedAt();
        this.modifiedAt = show.getModifiedAt();
        this.deletedAt = show.getDeletedAt();
    }

    public static ShowResponse from(Show show, List<ShowDate> showDates) {
        return new ShowResponse(show, showDates);
    }
}