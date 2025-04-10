package com.example.picket.domain.show.service;

import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import com.example.picket.domain.show.repository.jdbc.ShowDateJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

}

