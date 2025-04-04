package com.example.picket.domain.seat.entity;

import com.example.picket.common.enums.Grade;
import com.example.picket.domain.show.entity.ShowDate;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "seats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grade grade;

    @Column(nullable = false)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_date_id", nullable = false)
    private ShowDate showDate;

    @Builder
    private Seat(Integer seatNumber, Grade grade, BigDecimal price, ShowDate showDate) {
        this.seatNumber = seatNumber;
        this.grade = grade;
        this.price = price;
        this.showDate = showDate;
    }
}
