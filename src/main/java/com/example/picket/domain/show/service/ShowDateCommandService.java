package com.example.picket.domain.show.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import com.example.picket.domain.show.repository.jdbc.ShowDateJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class ShowDateCommandService {

    private final ShowDateRepository showDateRepository;
    private final ShowDateJdbcRepository showDateJdbcRepository;

    public void createShowDate(ShowDate showDate) {
        showDateRepository.save(showDate);
    }

    public void createShowDatesJdbc(List<ShowDate> showDates) {
        showDateJdbcRepository.saveAllJdbc(showDates);
    }

    public void countUpdate(Long showDateId, int count) {
        ShowDate foundShowDate = showDateRepository.findById(showDateId)
                .orElseThrow(() -> new CustomException(NOT_FOUND, "공연 날짜를 찾을 수 없습니다."));
        foundShowDate.updateCountOnBooking(count);
    }

}

