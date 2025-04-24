package com.example.picket.domain.user.entity;

import com.example.picket.common.entity.BaseEntity;
import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.OAuth;
import com.example.picket.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @Column
    private String profileUrl;

    @Column
    private String nickname;

    @Column
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;

    @Column
    private String oauthId;

    @Enumerated(EnumType.STRING)
    @Column
    private OAuth oauthProvider;

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

    private User(String email, UserRole userRole, String oauthId, OAuth oauthProvider) {
        this.email = email;
        this.userRole = userRole;
        this.oauthId = oauthId;
        this.oauthProvider = oauthProvider;

    }

    public static User create(String email, String password, UserRole userRole, String profileUrl, String nickname,
                              LocalDate birth, Gender gender) {
        return new User(email, password, userRole, profileUrl, nickname, birth, gender);
    }

    public static User createWithOAuth(String email, UserRole userRole, String oauthId, OAuth oauthProvider) {
        return new User(email, userRole, oauthId, oauthProvider);
    }

    public void update(String nickname, String profileUrl) {
        this.nickname = nickname;
        this.profileUrl = profileUrl;
    }

    public void oAuthSignup(String nickname, LocalDate birth, Gender gender) {
        this.nickname = nickname;
        this.birth = birth;
        this.gender = gender;
    }

    public void passwordUpdate(String encodePassword) {
        this.password = encodePassword;
    }
}
