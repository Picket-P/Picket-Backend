package com.example.picket.domain.seat.entity;

import com.example.picket.common.entity.BaseEntity;
import com.example.picket.common.enums.Grade;
import com.example.picket.common.enums.SeatStatus;
import com.example.picket.domain.show.entity.ShowDate;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "seats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus seatStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grade grade;

    @Column(nullable = false)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_date_id", nullable = false)
    private ShowDate showDate;

    private Seat(Grade grade, Integer seatNumber, BigDecimal price, SeatStatus seatStatus, ShowDate showDate) {
        this.grade = grade;
        this.seatNumber = seatNumber;
        this.price = price;
        this.seatStatus = seatStatus;
        this.showDate = showDate;
    }

    public static Seat toEntity(Grade grade, int seatNumber, BigDecimal price, ShowDate showDate) {
        return new Seat(grade, seatNumber, price, SeatStatus.AVAILABLE, showDate);
    }

    public void updatePrice(BigDecimal price) {
        this.price = price;
    }

    public void updateSeatStatus(SeatStatus seatStatus) {
        this.seatStatus = seatStatus;
    }
}
