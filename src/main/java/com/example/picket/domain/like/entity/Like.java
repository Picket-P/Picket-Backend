package com.example.picket.domain.like.entity;

import com.example.picket.common.entity.BaseEntity;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Like(Show show, User user) {
        this.show = show;
        this.user = user;
    }

    public static Like create(Show show, User user) {
        return new Like(show, user);
    }
}
