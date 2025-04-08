package com.example.picket.domain.show.repository;

import com.example.picket.common.enums.Category;
import com.example.picket.domain.show.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long>, ShowQueryDslRepository {

    List<Show> findAllByCategoryAndDeletedAtIsNull(Category category);

}
