package com.daehanforeigner.capstone.domain.user.entity;

import com.daehanforeigner.capstone.global.entity.GlobalEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
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
    private NativeLanguage nativeLanguage;

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

    @Column(name = "deleted_at") // 탈퇴일시 (소프트 딜리트 14일 후 자동 삭제)
    private LocalDateTime deletedAt;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // 프로필 정보 수정 처리
    public void updateProfile(String nickname, String profileImageUrl, NativeLanguage nativeLanguage) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (nativeLanguage != null) {
            this.nativeLanguage = nativeLanguage;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    // 비밀번호 변경 처리
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    // 회원 탈퇴 처리 (소프트 딜리트)
    public void withdraw() {
        this.status = Status.WITHDRAWN;
        this.deletedAt = LocalDateTime.now();
        this.refreshToken = null;
    }
}
