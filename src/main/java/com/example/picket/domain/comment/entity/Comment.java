package com.example.picket.domain.comment.entity;

import com.example.picket.common.entity.BaseEntity;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Comment(String content, Show show, User user) {
        this.content = content;
        this.show = show;
        this.user = user;
    }

    public static Comment create(String content, Show show, User user) {
        return new Comment(content, show, user);
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
