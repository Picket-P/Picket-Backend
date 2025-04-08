package com.example.picket.domain.show.service;

import com.example.picket.domain.seat.service.SeatCommandService;
import com.example.picket.domain.seat.service.SeatQueryService;
import com.example.picket.domain.show.repository.ShowRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ShowCommandServiceTest {

    @Mock
    ShowRepository showRepository;

    @Mock
    ShowDateCommandService showDateCommandService;

    @Mock
    ShowDateQueryService showDateQueryService;

    @Mock
    SeatQueryService seatQueryService;

    @Mock
    SeatCommandService seatCommandService;

    @InjectMocks
    ShowCommandService showCommandService;

    @Nested
    class 공연_생성_테스트 {



    }

}