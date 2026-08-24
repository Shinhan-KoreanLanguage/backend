package com.daehanforeigner.capstone.domain.user.repository;

import com.daehanforeigner.capstone.domain.user.entity.Status;
import com.daehanforeigner.capstone.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email); // 로그인 시 사용자 이메일로 회원 조회

    boolean existsByNickname(String nickname); // 프로필 수정 시 닉네임 중복 확인

    // 회원가입 중복 확인 — 존재 여부만이 아니라 탈퇴 회원인지도 구분해야 해서 엔티티를 가져온다
    Optional<User> findByNickname(String nickname);

    List<User> findAllByStatusAndDeletedAtBefore(Status status, LocalDateTime deletedAt); // 탈퇴한 회원 && 14일이 지난 회원 조회
}
