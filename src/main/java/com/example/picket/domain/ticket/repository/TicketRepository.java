package com.example.picket.domain.ticket.repository;

import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsBySeat(Seat seat);
}
