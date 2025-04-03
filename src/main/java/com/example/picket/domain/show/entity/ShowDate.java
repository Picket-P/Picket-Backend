package com.example.picket.domain.show.entity;

import com.example.picket.common.entity.BaseEntity;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Entity
@Table(name = "show_dates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowDate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer seatCount;

    @Column(nullable = false)
    private Integer remainCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    public void discountRemainCount() {
        if(this.remainCount <= 0) {
            throw new CustomException(ErrorCode.SEAT_NO_AVAILABLE);
        } else {
            this.remainCount -= 1;
        }
    }

    @Builder
    private ShowDate(LocalDate date, LocalTime startTime, LocalTime endTime, Integer seatCount,
                     Integer remainCount, Show show) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.seatCount = seatCount;
        this.remainCount = remainCount;
        this.show = show;
    }
}
