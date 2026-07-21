package com.daehanforeigner.capstone.domain.social_account.entity;

import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.global.entity.GlobalEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
// UniqueConstraint 사용을 통해 같은 소셜 계정이 두 번 연동되는 것을 DB 차원에서 차단
@Table(name = "social_account", uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_user_id"}))
public class SocialAccount extends GlobalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_id")
    private Long socialId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private Provider provider;

    // 소셜 계정에서 제공하는 고유 사용자 ID
    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;
}
