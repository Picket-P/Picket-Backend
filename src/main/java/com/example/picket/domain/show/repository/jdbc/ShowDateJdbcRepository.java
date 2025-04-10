package com.example.picket.domain.show.repository.jdbc;

import com.example.picket.domain.show.entity.ShowDate;

import java.util.List;

public interface ShowDateJdbcRepository {

    void saveAllJdbc(List<ShowDate> showDates);

}
