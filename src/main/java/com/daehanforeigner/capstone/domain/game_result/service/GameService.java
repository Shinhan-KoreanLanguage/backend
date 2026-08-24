package com.daehanforeigner.capstone.domain.game_result.service;

import com.daehanforeigner.capstone.domain.content_category.entity.ContentCategory;
import com.daehanforeigner.capstone.domain.content_category.repository.ContentCategoryRepository;
import com.daehanforeigner.capstone.domain.game_field_table.entity.GameFieldTable;
import com.daehanforeigner.capstone.domain.game_field_table.repository.GameFieldTableRepository;
import com.daehanforeigner.capstone.domain.game_result.dto.GameFinishResponseDTO;
import com.daehanforeigner.capstone.domain.game_result.dto.GameStartResponseDTO;
import com.daehanforeigner.capstone.domain.game_result.dto.WordSubmitResponseDTO;
import com.daehanforeigner.capstone.domain.game_result.entity.GameResult;
import com.daehanforeigner.capstone.domain.game_result.repository.GameResultRepository;
import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import com.daehanforeigner.capstone.domain.learning_content.entity.LearningContent;
import com.daehanforeigner.capstone.domain.learning_content.repository.LearningContentRepository;
import com.daehanforeigner.capstone.domain.ranking.service.RankingService;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.domain.user.repository.UserRepository;
import com.daehanforeigner.capstone.global.ai.PronunciationAiClient;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 시간 제한 발음 게임(1분 동안 정확히 발음한 단어 개수 겨루기) 담당
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    // 정확도가 이 값 이상이면 "정확히 말함"으로 카운트한다
    private static final double ACCURACY_THRESHOLD = 70.0;

    // 한 게임에 제시할 단어 수 상한 (카테고리에 이보다 적으면 있는 만큼만)
    private static final int WORDS_PER_GAME = 50;

    private final GameResultRepository gameResultRepository;

    private final GameFieldTableRepository gameFieldTableRepository;

    private final LearningContentRepository learningContentRepository;

    private final ContentCategoryRepository contentCategoryRepository;

    private final UserRepository userRepository;

    private final PronunciationAiClient aiClient;

    private final RankingService rankingService;

    // 게임 시작 — 카테고리 내 단어를 셔플해 내려주고 GameResult를 세션처럼 만든다
    @Transactional
    public GameStartResponseDTO startGame(Long userId, Long categoryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ContentCategory category = contentCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        List<LearningContent> words = new ArrayList<>(
                learningContentRepository.findAllByContentCategory_CategoryIdAndContentType(categoryId, ContentType.WORD));

        if (words.isEmpty()) {
            throw new CustomException(ErrorCode.NO_WORDS_AVAILABLE);
        }

        Collections.shuffle(words);
        List<LearningContent> selected = words.size() > WORDS_PER_GAME
                ? words.subList(0, WORDS_PER_GAME)
                : words;

        GameResult gameResult = gameResultRepository.save(GameResult.builder()
                .user(user)
                .category(category)
                .score(0)
                .isPassed(false)
                .playedAt(LocalDateTime.now())
                .build());

        return GameStartResponseDTO.of(gameResult.getGameResultId(), selected);
    }

    // 단어 하나 발음 제출 — AI 서버에 바로 스트리밍해서 분석하고, 파일은 저장하지 않는다 (다시 들을 필요 없는 데이터)
    @Transactional
    public WordSubmitResponseDTO submitWord(Long userId, Long gameResultId, Long wordId, MultipartFile audio) {
        GameResult gameResult = getOwnedGame(userId, gameResultId);

        if (gameResult.isPassed()) {
            throw new CustomException(ErrorCode.GAME_ALREADY_FINISHED);
        }
        if (audio == null || audio.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_FILE);
        }

        LearningContent word = learningContentRepository.findById(wordId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND));

        JsonNode result = aiClient.analyze(word.getText(), audio.getResource(), null);

        Double accuracy = nullableDouble(result, "final_accuracy");
        boolean isCorrect = accuracy != null && accuracy >= ACCURACY_THRESHOLD;
        int sequenceNo = gameFieldTableRepository.countByGameResult(gameResult) + 1;

        gameFieldTableRepository.save(GameFieldTable.builder()
                .gameResult(gameResult)
                .isSentence(false)
                .gameField(word.getText())
                .accuracy(accuracy)
                .isCorrect(isCorrect)
                .sequenceNo(sequenceNo)
                .build());

        return new WordSubmitResponseDTO(isCorrect, accuracy);
    }

    // 게임 종료 — 정확히 맞힌 단어 개수를 최종 점수로 집계하고 랭킹에 반영한다
    @Transactional
    public GameFinishResponseDTO finishGame(Long userId, Long gameResultId) {
        GameResult gameResult = getOwnedGame(userId, gameResultId);

        if (gameResult.isPassed()) {
            throw new CustomException(ErrorCode.GAME_ALREADY_FINISHED);
        }

        int score = gameFieldTableRepository.countByGameResultAndIsCorrectTrue(gameResult);
        gameResult.finish(score, true);

        boolean isNewBestScore = rankingService.recordGame(gameResult.getUser(), score);

        return new GameFinishResponseDTO(score, isNewBestScore);
    }

    // 본인 게임인지 확인 — 남의 게임은 존재 자체를 숨긴다 (ATTEMPT_NOT_FOUND와 동일한 패턴)
    private GameResult getOwnedGame(Long userId, Long gameResultId) {
        GameResult gameResult = gameResultRepository.findById(gameResultId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        if (!gameResult.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.GAME_NOT_FOUND);
        }
        return gameResult;
    }

    private Double nullableDouble(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return (value.isMissingNode() || value.isNull()) ? null : value.asDouble();
    }
}
