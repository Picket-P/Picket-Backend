package com.example.picket.domain.ticket.repository;

import com.example.picket.common.enums.ShowStatus;
import com.example.picket.common.enums.TicketStatus;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsBySeat(Seat seat);

    @EntityGraph(attributePaths = {"user", "show", "seat"})
    @Query("select t from Ticket t where t.user.id = :userId")
    Page<Ticket> findByUser(@Param("userId")Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "show", "seat"})
    @Query("select t from Ticket t where t.id = :ticketId")
    Optional<Ticket> findByTicketId(@Param("ticketId") Long ticketId);

    @Query("SELECT DISTINCT t.user.id FROM Ticket t WHERE t.user.id IN :userIds AND t.show.id = :showId AND t.status <> :status")
    List<Long> findUserIdsWithValidTicket(@Param("userIds") List<Long> userIds,
                                          @Param("showId") Long showId,
                                          @Param("status") TicketStatus status);

    @Query("select count(t) from Ticket t where t.user = :user and t.show = :show and t.status = :ticketStatus")
    int countTicketByUserAndShowWithTicketStatus(
            @Param("user") User user,
            @Param("show") Show show,
            @Param("ticketStatus") TicketStatus status);

    @Modifying
    @Query("UPDATE Ticket t SET t.status = :newStatus WHERE t.show.id = :showId AND t.status = :currentStatus")
    int updateTicketStatusByShowId(@Param("showId") Long showId,
                                   @Param("currentStatus") TicketStatus currentStatus,
                                   @Param("newStatus") TicketStatus newStatus);
}
