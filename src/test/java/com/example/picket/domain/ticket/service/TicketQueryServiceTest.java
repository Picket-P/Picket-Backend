package com.example.picket.domain.ticket.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.ticket.entity.Ticket;
import com.example.picket.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TicketQueryServiceTest {

    @BeforeEach
    public void setUp() {
        User testUser = User.toEntity(
                "testUser@test.com",
                "Qwer@1234!",
                UserRole.USER,
                "anything",
                "testUser",
                LocalDate.of(2002, 2, 2),
                Gender.FEMALE);
        User testAdmin = User.toEntity(
                "testAdmin@test.com",
                "Qwer@1234!",
                UserRole.ADMIN,
                "anything",
                "testAdmin",
                LocalDate.of(2002, 2, 2),
                Gender.FEMALE);

    }

    @Test
    void getTickets() {
    }

    @Test
    void getTicket() {
    }
}