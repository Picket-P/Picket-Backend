package com.example.picket.domain.show.service;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.service.S3Service;
import com.example.picket.domain.images.dto.response.ImageResponse;
import com.example.picket.domain.images.entity.ShowImage;
import com.example.picket.domain.images.repository.ShowImageRepository;
import com.example.picket.domain.seat.dto.request.SeatCreateRequest;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.seat.service.SeatCommandService;
import com.example.picket.domain.seat.service.SeatQueryService;
import com.example.picket.domain.show.dto.request.ShowCreateRequest;
import com.example.picket.domain.show.dto.request.ShowDateRequest;
import com.example.picket.domain.show.dto.request.ShowUpdateRequest;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class ShowCommandService {

    private final ShowRepository showRepository;
    private final ShowDateCommandService showDateCommandService;
    private final ShowDateQueryService showDateQueryService;
    private final SeatCommandService seatCommandService;
    private final SeatQueryService seatQueryService;
    private final S3Service s3Service;
    private final ShowImageRepository showImageRepository;

    // 공연 생성
    @Transactional
    public Show createShow(@Auth AuthUser authUser, ShowCreateRequest request) {
        // 시작/종료 시간 유효성 검사
        validateShowTimes(request);

        // 공연 생성 및 저장
        Show show = Show.create(
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

        show = showRepository.save(show);

        // 포스터 Url 저장
        ShowImage showImage = showImageRepository.findByImageUrl(request.getPosterUrl())
                .orElseThrow(() -> new CustomException(NOT_FOUND, "해당 이미지를 찾을 수 없습니다. 다시 업로드 시도를 해주세요."));
        showImage.updateShow(show.getId());

        // 날짜별 공연 정보 및 좌석 생성
        List<ShowDate> showDates = new ArrayList<>();
        for (var dateRequest : request.getDates()) {
            validateSeatCount(dateRequest); // 좌석 수 검증

            ShowDate showDate = ShowDate.create(
                    dateRequest.getDate(),
                    dateRequest.getStartTime(),
                    dateRequest.getEndTime(),
                    dateRequest.getTotalSeatCount(),
                    0, // 예약 수 초기값
                    show
            );

            showDates.add(showDate);
        }
        showDateCommandService.createShowDatesJdbc(showDates);

        List<ShowDate> persistedShowDates = showDateQueryService.getShowDatesByShowId(show.getId());
        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < persistedShowDates.size(); i++) {
            ShowDate showDate = persistedShowDates.get(i);
            List<SeatCreateRequest> seatCreateRequests = request.getDates().get(i).getSeatCreateRequests();
            createSeatsForShowDate(showDate, seatCreateRequests, seats);
        }

        seatCommandService.createSeatsJdbc(seats);
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

        // 포스터 Url 저장
        ShowImage showImage = showImageRepository.findByImageUrl(request.getPosterUrl())
                .orElseThrow(() -> new CustomException(NOT_FOUND, "해당 이미지를 찾을 수 없습니다. 다시 업로드 시도를 해주세요."));
        showImage.updateShow(show.getId());

        if (show.getPosterUrl() != null) {
            deleteShowImage(show.getPosterUrl());
        }

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

        List<ShowDate> showDates = showDateQueryService.getShowDatesByShowId(showId);

        // SofeDelete 처리
        LocalDateTime deleteTime = LocalDateTime.now();

        show.updateDeletedAt(deleteTime);
        for (ShowDate showDate : showDates) {
            showDate.updateDeletedAt(deleteTime);

            List<Seat> seats = seatQueryService.getSeatsByShowDate(showDate.getId());
            seatCommandService.deleteAll(seats);
        }

        deleteShowImage(show.getPosterUrl());

    }

    // 공연 좌석 수 검증
    private void validateSeatCount(ShowDateRequest dateRequest) {
        int seatCount = 0;
        for (SeatCreateRequest seatCreateRequest : dateRequest.getSeatCreateRequests()) {
            seatCount += seatCreateRequest.getSeatCount();
        }

        if (!dateRequest.getTotalSeatCount().equals(seatCount)) {
            throw new CustomException(BAD_REQUEST, "총 좌석 수와 좌석 등급의 좌석 총합이 일치하지 않습니다.");
        }
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
    private void createSeatsForShowDate(ShowDate showDate, List<SeatCreateRequest> seatRequests, List<Seat> seats) {
        for (SeatCreateRequest seatRequest : seatRequests) {
            for (int i = 1; i <= seatRequest.getSeatCount(); i++) {
                seats.add(Seat.create(
                        seatRequest.getGrade(),
                        i,
                        seatRequest.getPrice(),
                        showDate
                ));
            }
        }
    }

    //이미지 업로드
    public String uploadImage(MultipartFile multipartFile) {
        ImageResponse imageResponse = s3Service.upload(multipartFile);
        ShowImage showImage = ShowImage.create(imageResponse, null);
        showImageRepository.save(showImage);
        return imageResponse.getImageUrl();
    }

    public void deleteShowImage(String imageUrl) {
        ShowImage showImage = showImageRepository.findByImageUrl(imageUrl)
                .orElseThrow(() -> new CustomException(NOT_FOUND, "해당 이미지 파일을 찾을 수 없습니다."));
        showImage.updateDeletedAt(LocalDateTime.now());
    }
}