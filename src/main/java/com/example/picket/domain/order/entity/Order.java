package com.example.picket.domain.order.entity;

import com.example.picket.common.entity.BaseEntity;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "order")
    private List<Ticket> ticket = new ArrayList<>();

    private Order(User user, BigDecimal totalPrice, List<Ticket> ticket) {
        this.user = user;
        this.totalPrice = totalPrice;
        this.ticket = ticket;
    }

    public static Order toEntity(User user, BigDecimal totalPrice, List<Ticket> tickets) {
        Order order =  new Order(user, totalPrice, tickets);
        for (Ticket ticket : tickets) {
            ticket.setOrder(order);
        }
        return order;
    }
}
