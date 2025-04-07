package com.example.picket.domain.show.dto;

import com.example.picket.domain.show.entity.Show;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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

    // 공연 + 날짜 리스트 기반 응답 객체 생성
    public static ShowResponse from(Show show, List<ShowDateResponse> showDateResponses) {
        ShowResponse response = show.toDto();
        response.setShowDates(showDateResponses);
        return response;
    }
}
