package com.example.picket.domain.show.service;

import com.example.picket.domain.show.dto.ShowCreateRequest;
import com.example.picket.domain.show.dto.ShowResponse;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import com.example.picket.domain.show.repository.ShowRepository;
import com.example.picket.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowDateRepository showDateRepository;

    @Transactional
    public ShowResponse createShow(ShowCreateRequest request) {

        User director = new User();  // 빈 User 객체 생성
        director.setId(request.getDirectorId());

        Show show = showRepository.save(
                Show.builder()
                        .title(request.getTitle())
                        .posterUrl(request.getPosterUrl())
                        .category(request.getCategory())
                        .description(request.getDescription())
                        .location(request.getLocation())
                        .reservationStart(request.getReservationStart())
                        .reservationEnd(request.getReservationEnd())
                        .ticketsLimitPerUser(request.getTicketsLimitPerUser())
                        .user(director)
                        .build()
        );

        List<ShowDate> showDates = request.getDates().stream()
                .map(dateRequest -> ShowDate.builder()
                        .date(dateRequest.getDate())
                        .startTime(dateRequest.getStartTime())
                        .endTime(dateRequest.getEndTime())
                        .totalSeatCount(dateRequest.getTotalSeatCount())
                        .reservedSeatCount(0)
                        .show(show)
                        .build()
                ).toList();

        showDateRepository.saveAll(showDates);

        return ShowResponse.from(show, showDates);
    }
}