package com.daehanforeigner.capstone.domain.wrong_answer.entity;

import com.daehanforeigner.capstone.domain.learning_content.entity.ContentType;
import lombok.Getter;

import java.util.List;

// 오답 노트 화면 상단의 탭.
// 화면은 "단어 / 문장" 둘로만 나누는데 콘텐츠 유형은 음절까지 셋이라, 그 대응을 여기서 정의한다.
// 음절을 단어에 포함시키는 것은 학습자 입장에서 둘 다 낱말 연습이기 때문 (홈 대시보드와 동일한 기준)
@Getter
public enum WrongAnswerTab {

    ALL(List.of(ContentType.SYLLABLE, ContentType.WORD, ContentType.SENTENCE)),
    WORD(List.of(ContentType.SYLLABLE, ContentType.WORD)),
    SENTENCE(List.of(ContentType.SENTENCE));

    private final List<ContentType> contentTypes;

    WrongAnswerTab(List<ContentType> contentTypes) {
        this.contentTypes = contentTypes;
    }
}
