package com.example.picket.domain.show.service;

import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShowDateCommandService {

    private final ShowDateRepository showDateRepository;

    public void createShowDate(ShowDate showDate) {
        showDateRepository.save(showDate);
    }

}
