package com.example.picket.domain.show.dto.request;

import com.example.picket.common.enums.Category;
import java.time.LocalDateTime;
import lombok.Getter;

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

