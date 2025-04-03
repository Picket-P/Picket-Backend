package com.example.picket.domain.show.repository;

import com.example.picket.domain.show.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowRepository extends JpaRepository<Show, Long> {
}
