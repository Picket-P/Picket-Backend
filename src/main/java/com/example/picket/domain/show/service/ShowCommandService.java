package com.example.picket.domain.show.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.seat.dto.request.SeatCreateRequest;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.service.SeatCommandService;
import com.example.picket.domain.show.dto.request.ShowCreateRequest;
import com.example.picket.domain.show.dto.request.ShowUpdateRequest;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShowCommandService {

    private final ShowRepository showRepository;
    private final ShowDateCommandService showDateCommandService;
    private final ShowDateQueryService showDateQueryService;
    private final SeatCommandService seatCommandService;

    // 공연 생성
    @Transactional
    public Show createShow(@Auth AuthUser authUser, ShowCreateRequest request) {

        // 시작/종료 시간 유효성 검사
        validateShowTimes(request);

        // 공연 생성 및 저장
        Show show = Show.toEntity(
                authUser.getId(),
                request.getTitle(),
                request.getPosterUrl(),
                request.getCategory(),
                request.getDescription(),
                request.getLocation(),
                request.getReservationStart(),
                request.getReservationEnd(),
                request.getTicketsLimitPerUser()
        );
        showRepository.save(show);

        // 날짜별 공연 정보 및 좌석 생성
        for (var dateRequest : request.getDates()) {
            ShowDate showDate = ShowDate.toEntity(
                    dateRequest.getDate(),
                    dateRequest.getStartTime(),
                    dateRequest.getEndTime(),
                    dateRequest.getTotalSeatCount(),
                    0, // 예약 수 초기값
                    show
            );

            showDateCommandService.createShowDate(showDate);
            createSeatsForShowDate(showDate, dateRequest.getSeatCreateRequests());
        }

        return show;
    }

    // 공연 수정
    @Transactional
    public Show updateShow(@Auth AuthUser authUser, Long showId, ShowUpdateRequest request) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new CustomException(NOT_FOUND, "해당 공연을 찾을 수 없습니다."));

        if (show.getDeletedAt() != null) {  // 소프트 delete 된 상태라면 수정 불가능
            throw new CustomException(FORBIDDEN, "삭제된 공연은 수정할 수 없습니다.");
        }

        validateOwnership(authUser, show);  // 소유자 확인
        validateUpdatable(show);            // 수정 가능 상태 확인

        show.update(request); // Entity 내 update 로직 실행
        return show;
    }

    // 공연 삭제 (soft delete)
    @Transactional
    public void deleteShow(@Auth AuthUser authUser, Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new CustomException(NOT_FOUND, "해당 공연을 찾을 수 없습니다."));

        validateOwnership(authUser, show);

        // 예매 시작 이후 삭제 불가
        if (show.getReservationStart().isBefore(LocalDateTime.now())) {
            throw new CustomException(BAD_REQUEST, "예매 시작 이후에는 공연을 삭제할 수 없습니다.");
        }

        // SofeDelete 처리
        show.softDelete();
    }

    // 공연 작성자 검증
    private void validateOwnership(AuthUser user, Show show) {
        if (!show.getDirectorId().equals(user.getId())) {
            throw new CustomException(FORBIDDEN, "해당 작업을 수행할 권한이 없습니다.");
        }
    }

    // 공연 수정 가능 상태 검증
    private void validateUpdatable(Show show) {
        // 예매 시작 이후 수정 불가
        if (show.getReservationStart().isBefore(LocalDateTime.now())) {
            throw new CustomException(BAD_REQUEST, "예매 시작 이후에는 공연을 수정할 수 없습니다.");
        }

        // 공연 종료 여부 확인
        boolean hasEnded = showDateQueryService.getShowDatesByShowId(show.getId()).stream()
                .anyMatch(sd -> sd.getDate().atTime(sd.getEndTime()).isBefore(LocalDateTime.now()));

        if (hasEnded) {
            throw new CustomException(BAD_REQUEST, "종료된 공연은 수정할 수 없습니다.");
        }
    }

    // 공연 시작/종료 시간 유효성 검사
    private void validateShowTimes(ShowCreateRequest request) {
        request.getDates().forEach(dateRequest -> {
            if (dateRequest.getStartTime().isAfter(dateRequest.getEndTime())) {
                throw new CustomException(BAD_REQUEST, "공연 시작 시간이 종료 시간보다 늦을 수 없습니다.");
            }
        });
    }

    // 날짜별 좌석 생성
    private void createSeatsForShowDate(ShowDate showDate, List<SeatCreateRequest> seatRequests) {
        List<Seat> seats = new ArrayList<>();

        for (SeatCreateRequest seatRequest : seatRequests) {
            for (int i = 1; i <= seatRequest.getSeatCount(); i++) {
                seats.add(Seat.toEntity(
                        seatRequest.getGrade(),
                        i,
                        seatRequest.getPrice(),
                        showDate
                ));
            }
        }

        seatCommandService.saveAll(seats);
    }
}