package com.daehanforeigner.capstone.domain.ranking.service;

import com.daehanforeigner.capstone.domain.ranking.dto.MyRankingResponseDTO;
import com.daehanforeigner.capstone.domain.ranking.dto.RankingResponseDTO;
import com.daehanforeigner.capstone.domain.ranking.entity.Ranking;
import com.daehanforeigner.capstone.domain.ranking.repository.RankingRepository;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.domain.user.repository.UserRepository;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

// 발음 게임 랭킹(TOP10 · 개인 순위 · 누적 단어 수) 담당
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

    private static final int TOP_N = 10;

    private final RankingRepository rankingRepository;

    private final UserRepository userRepository;

    // 게임 종료 시 호출 — 없으면 새로 만들고, 있으면 기록 갱신. 신기록 여부를 반환한다
    @Transactional
    public boolean recordGame(User user, int score) {
        Ranking ranking = rankingRepository.findByUser(user)
                .orElseGet(() -> rankingRepository.save(
                        Ranking.builder()
                                .user(user)
                                .bestScore(0)
                                .spokenWordCount(0)
                                .build()));

        return ranking.recordGame(score);
    }

    public List<RankingResponseDTO> getTop10() {
        List<Ranking> rankings = rankingRepository.findAllByOrderByBestScoreDescRankIdAsc(PageRequest.of(0, TOP_N));

        return IntStream.range(0, rankings.size())
                .mapToObj(i -> RankingResponseDTO.of(i + 1, rankings.get(i)))
                .toList();
    }

    public MyRankingResponseDTO getMyRanking(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return rankingRepository.findByUser(user)
                .map(ranking -> {
                    // 나보다 점수가 높은 사람 수 + 1 = 내 순위
                    int rank = (int) rankingRepository.countByBestScoreGreaterThan(ranking.getBestScore()) + 1;
                    return new MyRankingResponseDTO(true, rank, ranking.getBestScore(), ranking.getSpokenWordCount());
                })
                .orElseGet(MyRankingResponseDTO::noRecord);
    }
}
