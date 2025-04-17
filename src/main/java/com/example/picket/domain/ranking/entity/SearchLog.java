package com.example.picket.domain.ranking.entity;

import com.example.picket.common.enums.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_logs")
@Getter
@NoArgsConstructor
public class SearchLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;

    private SearchLog(Category category) {
        this.category = category;
        this.searchedAt = LocalDateTime.now();
    }

    public static SearchLog toEntity(Category category) {
        return new SearchLog(category);
    }
}
