package com.example.picket.domain.show.repository;

import com.example.picket.common.enums.Category;
import com.example.picket.domain.show.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findAllByCategoryAndIsDeletedFalse(Category category);
}
