package com.example.picket.domain.show.entity;

import com.example.picket.common.entity.BaseEntity;
import com.example.picket.common.enums.Category;
import com.example.picket.domain.show.dto.ShowUpdateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "shows")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Show extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "director_id", nullable = false)
    private Long directorId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String posterUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime reservationStart;

    @Column(nullable = false)
    private LocalDateTime reservationEnd;

    @Column
    private Integer ticketsLimitPerUser; // null 이면 제한 없음

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Builder
    private Show(Long directorId, String title, String posterUrl, Category category, String description, String location,
                 LocalDateTime reservationStart, LocalDateTime reservationEnd, Integer ticketsLimitPerUser, boolean isDeleted) {
        this.directorId = directorId;
        this.title = title;
        this.posterUrl = posterUrl;
        this.category = category;
        this.description = description;
        this.location = location;
        this.reservationStart = reservationStart;
        this.reservationEnd = reservationEnd;
        this.ticketsLimitPerUser = ticketsLimitPerUser;
        this.isDeleted = isDeleted;
    }

    @PrePersist
    private void prePersist() {
        if (reservationEnd == null) { // reservationEnd가 null 이면 예매종료날짜가 공연시작날짜 전날 자정으로 set
            reservationEnd = reservationStart.toLocalDate().minusDays(1).atTime(LocalTime.MIDNIGHT);
        }
    }

    public void update(ShowUpdateRequest request) {
        if (request.getTitle() != null) this.title = request.getTitle();
        if (request.getPosterUrl() != null) this.posterUrl = request.getPosterUrl();
        if (request.getCategory() != null) this.category = request.getCategory();
        if (request.getDescription() != null) this.description = request.getDescription();
        if (request.getLocation() != null) this.location = request.getLocation();
        if (request.getReservationStart() != null) this.reservationStart = request.getReservationStart();
        if (request.getReservationEnd() != null) this.reservationEnd = request.getReservationEnd();
        if (request.getTicketsLimitPerUser() != null) this.ticketsLimitPerUser = request.getTicketsLimitPerUser();
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}
