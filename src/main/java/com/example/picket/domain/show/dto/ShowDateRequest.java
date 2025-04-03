package com.example.picket.domain.show.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ShowDateRequest {

    @NotNull
    private LocalDate date;

    @NotNull
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @NotNull
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    @NotNull
    private Integer totalSeatCount;
}

