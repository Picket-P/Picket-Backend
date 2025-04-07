package com.example.picket.domain.user.entity;

import com.example.picket.common.entity.BaseEntity;
import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @Column
    private String profileUrl;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    private User(String email, String password, UserRole userRole, String profileUrl, String nickname,
                 LocalDate birth, Gender gender) {
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.profileUrl = profileUrl;
        this.nickname = nickname;
        this.birth = birth;
        this.gender = gender;
    }

    public static User toEntity(String email, String password, UserRole userRole, String profileUrl, String nickname,
                                LocalDate birth, Gender gender) {
        return new User(email, password, userRole, profileUrl, nickname, birth, gender);
    }
}
