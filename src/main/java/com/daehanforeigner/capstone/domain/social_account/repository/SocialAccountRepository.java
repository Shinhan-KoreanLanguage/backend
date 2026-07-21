package com.daehanforeigner.capstone.domain.social_account.repository;

import com.daehanforeigner.capstone.domain.social_account.entity.Provider;
import com.daehanforeigner.capstone.domain.social_account.entity.SocialAccount;
import com.daehanforeigner.capstone.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(Provider provider, String providerUserId); // 소셜 로그인 시 소셜 계정 조회

    boolean existsByProviderAndProviderUserId(Provider provider, String providerUserId); // 소셜 계정 중복 확인

    void deleteAllByUser(User user); // user 테이블 지우기 전 social_account 테이블 부터 삭제해야 FK 충돌 안남
}
