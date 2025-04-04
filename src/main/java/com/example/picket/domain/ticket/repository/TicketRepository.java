package com.example.picket.domain.ticket.repository;

import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsBySeat(Seat seat);

    @EntityGraph(attributePaths = {"user", "show", "seat"})
    @Query("select t from Ticket t where t.user.id = :userId")
    Page<Ticket> findByUser(@Param("userId")Long userId, Pageable pageable);
}
