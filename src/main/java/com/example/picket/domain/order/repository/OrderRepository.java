package com.example.picket.domain.order.repository;

import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"user", "ticket"})
    Optional<Order> findByIdAndUser(Long orderId, User user);

    @EntityGraph(attributePaths = {"user", "ticket"})
    Page<Order> findByUser(User user, Pageable pageable);

}
