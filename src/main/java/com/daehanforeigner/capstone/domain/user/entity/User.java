package com.daehanforeigner.capstone.domain.user.entity;

import com.daehanforeigner.capstone.global.entity.GlobalEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User extends GlobalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") // PK 유저 ID
    private Long userId;

    @Column(name = "email") // 이메일
    private String email;

    @Column(name = "password") // 비밀번호 (소셜은 null)
    private String password;

    @Column(name = "nickname") // 닉네임
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "native_language") // 모국어
    private NativeLangauge nativeLanguage;

    @Column(name = "profile_image_url") // 프로필 이미지 URL
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role") // 권한 (USER, ADMIN)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status") // 상태 (ACTIVE, WITHDRAWN)
    private Status status;

    @Column(name = "refresh_token") // 리프레시 토큰
    private String refreshToken;
}
