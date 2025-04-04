package com.example.picket.domain.show.service;

import com.example.picket.common.enums.Category;
import com.example.picket.domain.show.dto.ShowResponse;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import com.example.picket.domain.show.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowQueryService {

    private final ShowRepository showRepository;
    private final ShowDateRepository showDateRepository;

    public List<ShowResponse> getShows(String category, String sortBy, String order) {

        List<Show> shows;

        // 카테고리 미지정일 경우 기본 정렬
        if (category != null && !category.isBlank()) {
            shows = showRepository.findAllByCategory(Category.valueOf(category.toUpperCase()));
        } else {
            shows = showRepository.findAll();
        }

        List<ShowResponse> responses = shows.stream()
                .map(show -> {
                    List<ShowDate> showDates = showDateRepository.findAllByShowId(show.getId());
                    return ShowResponse.from(show, showDates);
                })
                .collect(Collectors.toList());

//        // 정렬 기준 정의
//        Comparator<ShowResponse> comparator;
//
//        switch (sortBy != null ? sortBy.toLowerCase() : "createdat") {
//            case "reservationstart":
//                comparator = Comparator.comparing(r -> r.getShowDates().get(0).getStartTime());
//                break;
//            case "views":
//                comparator = Comparator.comparing(ShowResponse::getViews); // 추후 views 필드 추가 필요
//                break;
//            case "likes":
//                comparator = Comparator.comparing(ShowResponse::getLikes); // 추후 likes 필드 추가 필요
//                break;
//            case "reservation":
//                comparator = Comparator.comparing(ShowResponse::getReservationCount); // 추후 예약 수 필드 추가 필요
//                break;
//            case "createdat":
//            default:
//                comparator = Comparator.comparing(ShowResponse::getCreatedAt);
//        }

        // 정렬 기준 정의
        Comparator<ShowResponse> comparator = Comparator.comparing(ShowResponse::getCreatedAt);

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        responses.sort(comparator);
        return responses;
    }
}

