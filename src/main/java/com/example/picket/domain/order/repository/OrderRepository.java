package com.example.picket.domain.order.repository;

import com.example.picket.domain.order.entity.Order;
import com.example.picket.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "user, ticket")
    Optional<Order> findByUser(User user);

}
