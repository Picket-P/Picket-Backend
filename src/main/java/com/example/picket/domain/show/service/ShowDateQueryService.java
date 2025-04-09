package com.example.picket.domain.show.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.show.dto.response.ShowDateDetailResponse;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowDateQueryService {

    private final ShowDateRepository showDateRepository;

    public ShowDate getShowDate(Long showDateId) {
        return showDateRepository.findById(showDateId)
                .orElseThrow(() -> new CustomException(NOT_FOUND, "공연 날짜를 찾을 수 없습니다."));
    }

    public ShowDate getShowDateByShow(Show show) {
        return showDateRepository.findShowDateByShow(show)
                .orElseThrow(() -> new CustomException(NOT_FOUND, "존재하지 않는 ShowDate입니다."));
    }

    public List<ShowDate> getShowDatesByShowId(Long showId) {
        return showDateRepository.findAllByShowId(showId);
    }

    public List<ShowDateDetailResponse> getShowDateDetailResponsesByShowId(Long showId) {
        return showDateRepository.getShowDateDetailResponseById(showId);
    }

}
