package com.daehanforeigner.capstone.domain.user.repository;

import com.daehanforeigner.capstone.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email); // 로그인 시 사용자 이메일로 회원 조회

    boolean existsByEmail(String email); // 회원가입 시 이메일 중복 확인
}
