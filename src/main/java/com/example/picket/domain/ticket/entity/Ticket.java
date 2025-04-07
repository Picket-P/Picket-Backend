package com.example.picket.domain.ticket.entity;

import com.example.picket.common.entity.BaseEntity;
import com.example.picket.common.enums.TicketStatus;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    public void updateTicketStatus(TicketStatus ticketStatus) {
        this.status = ticketStatus;
    }

    private Ticket(User user, Show show, Seat seat, BigDecimal price, TicketStatus status) {
        this.user = user;
        this.show = show;
        this.seat = seat;
        this.price = price;
        this.status = status;
    }

    public static Ticket toEntity(User user, Show show, Seat seat, BigDecimal price, TicketStatus status) {
        return new Ticket(user, show, seat, price, status);
    }
}
