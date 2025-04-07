package com.example.picket.domain.show.dto;

import com.example.picket.common.enums.Category;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ShowUpdateRequest {
    private String title;
    private String posterUrl;
    private Category category;
    private String description;
    private String location;
    private LocalDateTime reservationStart;
    private LocalDateTime reservationEnd;
    private Integer ticketsLimitPerUser;
}

