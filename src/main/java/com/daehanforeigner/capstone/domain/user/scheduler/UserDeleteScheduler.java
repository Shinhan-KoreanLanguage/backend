package com.daehanforeigner.capstone.domain.user.scheduler;

import com.daehanforeigner.capstone.domain.social_account.repository.SocialAccountRepository;
import com.daehanforeigner.capstone.domain.user.entity.Status;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeleteScheduler {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    // 매일 00:00시에 실행되도록 스케줄러 설정
    @Scheduled(cron = "0 0 0 * * *") // (cron: 초 분 시 일 월 요일) 매일 00:00시에 실행
    @Transactional
    public void deleteExpiredWithdrawnUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(14); // 14일 전 날짜 계산

        List<User> targets = userRepository.findAllByStatusAndDeletedAtBefore(Status.WITHDRAWN, threshold);

        for (User user : targets) {
            socialAccountRepository.deleteAllByUser(user); // 연관된 소셜 계정 삭제

            userRepository.delete(user); // 사용자 삭제 (하드 딜리트)
        }
            log.info("만료된 탈퇴 회원 {}명 DB 삭제 완료", targets.size());
    }
}
