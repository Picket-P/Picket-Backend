package com.example.picket.domain.show.service;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.domain.show.dto.ShowCreateRequest;
import com.example.picket.domain.show.dto.ShowResponse;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import com.example.picket.domain.show.repository.ShowRepository;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowCommandService {

    private final ShowRepository showRepository;
    private final ShowDateRepository showDateRepository;

    @Transactional
    public ShowResponse createShow(@Auth AuthUser authUser, ShowCreateRequest request) {

        validateShowTimes(request); // 공연 시간 검증

        Show show = showRepository.save(
                Show.builder()
                        .directorId(authUser.getId())
                        .title(request.getTitle())
                        .posterUrl(request.getPosterUrl())
                        .category(request.getCategory())
                        .description(request.getDescription())
                        .location(request.getLocation())
                        .reservationStart(request.getReservationStart())
                        .reservationEnd(request.getReservationEnd())
                        .ticketsLimitPerUser(request.getTicketsLimitPerUser())
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

    // 공연 시간 검증
    private void validateShowTimes(ShowCreateRequest request) {
        request.getDates().forEach(dateRequest -> {
            if (dateRequest.getStartTime().isAfter(dateRequest.getEndTime())) {
                throw new CustomException(ErrorCode.SHOW_TIME_INVALID);
            }
        });
    }
}