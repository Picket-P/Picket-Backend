package com.example.picket.domain.show.service;

import static com.example.picket.common.exception.ErrorCode.SHOW_NOT_FOUND;

import com.example.picket.common.enums.Category;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowQueryService {

    private final ShowRepository showRepository;
    private final ShowDateQueryService showDateQueryService;

    // 공연 목록 조회
    public List<Show> getShows(String category, String sortBy, String order) {

        List<Show> shows = fetchShowsByCategory(category); // 카테고리 필터링
        sortShows(shows, sortBy, order); // 정렬 처리

        return shows;
    }

    //공연 단건 조회
    public Show getShowDetails(Long showId) {

        return showRepository.findById(showId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 공연을 찾을 수 없습니다."));
    }

    // 공연 날짜 조회
    public List<ShowDate> getShowDatesByShowId(Long showId) {
        return showDateQueryService.findAllByShowId(showId);
    }

    // 카테고리 필터링 및 유효성 검사
    private List<Show> fetchShowsByCategory(String category) {
        if (category == null || category.isBlank()) {
            return showRepository.findAll();
        }

        try {
            Category categoryEnum = Category.valueOf(category.toUpperCase());
            return showRepository.findAllByCategoryAndIsDeletedFalse(categoryEnum);
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
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 정렬 방식입니다. (asc, desc만 허용)");
        }

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        shows.sort(comparator);
    }

    public Show findById(Long id) {
        return showRepository.findById(id).orElseThrow(() -> new CustomException(SHOW_NOT_FOUND));
    }

    public List<Show> findAllById(List<Long> ids) {
        return showRepository.findAllById(ids);
    }
}
