package com.example.picket.domain.show.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.example.picket.common.enums.Category;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowQueryService {

    private final ShowRepository showRepository;

    // 공연 목록 조회
    public List<Show> getShows(Category category, String sortBy, String order) {
        List<Show> shows = showRepository.findAllByCategoryAndDeletedAtIsNull(category); // 카테고리 필터링
        sortShows(shows, sortBy, order); // 정렬 처리

        return shows;
    }

    //공연 단건 조회
    public Show getShow(Long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new CustomException(NOT_FOUND, "해당 공연을 찾을 수 없습니다."));
    }

    // 공연 목록 조회, Show Ids
    public List<Show> getShowDatesByShowIds(List<Long> showIds) {
        return showRepository.findAllById(showIds);
    }

    // 카테고리 필터링 및 유효성 검사
    private List<Show> fetchShowsByCategory(String category) {
        if (category == null || category.isBlank()) {
            return showRepository.findAll();
        }

        try {
            Category categoryEnum = Category.valueOf(category.toUpperCase());
            return showRepository.findAllByCategoryAndDeletedAtIsNull(categoryEnum);
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
