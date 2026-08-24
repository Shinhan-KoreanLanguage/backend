package com.daehanforeigner.capstone.domain.wrong_answer.service;

import com.daehanforeigner.capstone.domain.wrong_answer.dto.WrongAnswerStatResponseDTO;
import com.daehanforeigner.capstone.domain.wrong_answer.repository.WrongAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 오답(틀린 단어·문장) 통계 담당
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WrongAnswerStatService {

    private final WrongAnswerRepository wrongAnswerRepository;

    // 아직 재학습으로 통과하지 못한 오답을 틀린 횟수 많은 순으로 조회
    public List<WrongAnswerStatResponseDTO> getWrongAnswerStats(Long userId) {
        return wrongAnswerRepository.findUnsolvedOrderByWrongCountDesc(userId).stream()
                .map(WrongAnswerStatResponseDTO::from)
                .toList();
    }
}
