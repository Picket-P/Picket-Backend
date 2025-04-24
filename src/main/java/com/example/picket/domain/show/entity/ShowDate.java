package com.example.picket.domain.show.entity;

import com.example.picket.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
    private Integer totalSeatCount;

    @Column(nullable = false)
    private Integer reservedSeatCount = 0; // 예약된 좌석 수 (초기값 0)

    @Column(nullable = false)
    private Integer availableSeatCount; // 예매 가능한 좌석 수 (totalSeatCount - reservedSeatCount)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    public void updateCountOnBooking(int count) {
        this.availableSeatCount -= count;
        this.reservedSeatCount += count;
    }

    public void updateCountOnCancellation(int count) {
        this.reservedSeatCount -= count;
        this.availableSeatCount += count;
    }

    private ShowDate(LocalDate date, LocalTime startTime, LocalTime endTime, Integer totalSeatCount,
                     Integer reservedSeatCount, Show show) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalSeatCount = totalSeatCount;
        this.reservedSeatCount = reservedSeatCount != null ? reservedSeatCount : 0;
        this.availableSeatCount = totalSeatCount - this.reservedSeatCount;
        this.show = show;
    }

    public static ShowDate create(LocalDate date, LocalTime startTime, LocalTime endTime, Integer totalSeatCount,
                                  Integer reservedSeatCount, Show show) {
        return new ShowDate(date, startTime, endTime, totalSeatCount, reservedSeatCount, show);
    }

    // 예약 좌석 계산 로직
    @PrePersist
    @PreUpdate
    private void updateAvailableSeats() {
        this.availableSeatCount = this.totalSeatCount - this.reservedSeatCount;
    }

    public void increaseTotalSeatCount(int count) {
        this.totalSeatCount += count;
    }

    public void decreaseTotalSeatCount(int count) {
        this.totalSeatCount -= count;
    }

    public void reserveSeats(int count) {
        if (this.reservedSeatCount + count > this.totalSeatCount) {
            throw new IllegalStateException("예약 가능한 좌석 수를 초과했습니다.");
        }
        this.reservedSeatCount += count;
        this.availableSeatCount = this.totalSeatCount - this.reservedSeatCount;
    }

//    public void updateCountOnCancellation(int count) {
//        if (this.reservedSeatCount - count < 0) {
//            throw new IllegalStateException("취소할 수 있는 좌석 수가 부족합니다.");
//        }
//        this.reservedSeatCount -= count;
//        this.availableSeatCount = this.totalSeatCount - this.reservedSeatCount;
//    }
}
