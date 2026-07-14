package com.daehanforeigner.capstone.domain.social_account.repository;

import com.daehanforeigner.capstone.domain.social_account.entity.Provider;
import com.daehanforeigner.capstone.domain.social_account.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(Provider provider, String providerUserId); // 소셜 로그인 시 소셜 계정 조회

    boolean existsByProviderAndProviderUserId(Provider provider, String providerUserId); // 소셜 계정 중복 확인
}
