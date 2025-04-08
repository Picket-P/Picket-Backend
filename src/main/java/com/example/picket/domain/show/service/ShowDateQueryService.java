package com.example.picket.domain.show.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowDateQueryService {

    private final ShowDateRepository showDateRepository;

    public ShowDate getShowDate(Long showDateId) {
        return showDateRepository.findById(showDateId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "공연 날짜를 찾을 수 없습니다."));
    }

    public ShowDate getShowDateByShow(Show show) {
        return showDateRepository.findShowDateByShow(show)
            .orElseThrow(() -> new CustomException(ErrorCode.SHOW_DATE_NOT_FOUND));
    }

    public List<ShowDate> getShowDatesByShowId(Long showId) {
        return showDateRepository.findAllByShowId(showId);
    }

}
