package com.example.picket.domain.show.dto.response;

import com.example.picket.domain.show.entity.Show;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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

    private ShowResponse(Long id, Long directorId, String title, String posterUrl, String category, String description,
                         String location, String reservationStart, String reservationEnd, Integer ticketsLimitPerUser,
                         List<ShowDateResponse> showDates, LocalDateTime createdAt, LocalDateTime modifiedAt,
                         LocalDateTime deletedAt) {
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
        this.deletedAt = deletedAt;
    }

    // 공연 + 날짜 리스트 기반 응답 객체 생성
    public static ShowResponse toDto(Show show, List<ShowDateResponse> showDateResponses) {
        return new ShowResponse(
                show.getId(),
                show.getDirectorId(),
                show.getTitle(),
                show.getPosterUrl(),
                show.getCategory().name(),
                show.getDescription(),
                show.getLocation(),
                show.getReservationStart().toString(),
                show.getReservationEnd().toString(),
                show.getTicketsLimitPerUser(),
                showDateResponses,
                show.getCreatedAt(),
                show.getModifiedAt(),
                show.getDeletedAt()
        );
    }
}
