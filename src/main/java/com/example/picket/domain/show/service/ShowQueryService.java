package com.example.picket.domain.show.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Category;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.show.dto.response.ShowDetailResponse;
import com.example.picket.domain.show.dto.response.ShowResponse;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowQueryService {


    private final ShowRepository showRepository;
    private final ShowViewCountService showViewCountService;

    // 공연 목록 조회
    public Page<ShowResponse> getShows(Category category, String sortBy, String order, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        return showRepository.getShowsResponse(category, sortBy, order, pageable);
    }

    public List<Show> getShows(Category category, String sortBy, String order) {
        List<Show> shows = fetchShowsByCategory(category); // 카테고리 필터링
        sortShows(shows, sortBy, order); // 정렬 처리
        return shows;
    }

    //공연 단건 조회
    public ShowDetailResponse getShow(AuthUser authUser, Long showId) {
        ShowDetailResponse response = showRepository.getShowDetailResponseById(showId)
            .orElseThrow(() -> new CustomException(NOT_FOUND, "해당 공연을 찾을 수 없습니다."));
        showViewCountService.incrementViewCount(authUser, showId);
        return response;
    }

    public Show getShow(Long showId) {
        return showRepository.findById(showId)
            .orElseThrow(() -> new CustomException(NOT_FOUND, "해당 공연을 찾을 수 없습니다."));
    }

    // 공연 목록 조회, Show Ids
    public List<Show> getShowDatesByShowIds(List<Long> showIds) {
        return showRepository.findAllById(showIds);
    }

    // 카테고리 필터링 및 유효성 검사
    private List<Show> fetchShowsByCategory(Category category) {
        if (category == null) {
            return showRepository.findAll();
        }

        try {
            return showRepository.findAllByCategoryAndDeletedAtIsNull(category);
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 카테고리입니다.");
        }
    }

    // 정렬 조건에 따라 공연 정렬
    private void sortShows(List<Show> shows, String sortBy, String order) {
        Comparator<Show> comparator;

        if ("reservationStart".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Show::getReservationStart);
        } else {
            comparator = Comparator.comparing(Show::getCreatedAt); // 기본 정렬: 생성일
        }

        if (!"asc".equalsIgnoreCase(order) && !"desc".equalsIgnoreCase(order) && order != null) {
            throw new CustomException(BAD_REQUEST, "유효하지 않은 정렬 방식입니다. (asc, desc만 허용)");
        }

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        shows.sort(comparator);
    }


}
