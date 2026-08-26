package com.daehanforeigner.capstone.domain.learning_content.entity;

import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 학습 콘텐츠의 언어별 번역.
// 컬럼을 언어별로 늘리는 대신 행으로 관리해서 언어가 추가돼도 스키마를 바꾸지 않는다
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "learning_content_translation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"content_id", "language"}))
public class LearningContentTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "translation_id")
    private Long translationId;

    // 번역에서 콘텐츠를 다시 조회할 일이 없다 (contentId 매핑에만 쓴다).
    // 기본값인 EAGER로 두면 번역을 읽을 때마다 콘텐츠 조회가 따라붙는다
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private LearningContent learningContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    private NativeLanguage language;

    @Column(name = "meaning") // 단어·문장의 뜻
    private String meaning;

    @Column(name = "pronunciation_guide") // 발음 도움말
    private String pronunciationGuide;

    @Column(name = "native_pronunciation") // 모국어 발음 표기 ex) sa-gwa
    private String nativePronunciation;

    public void update(String meaning, String pronunciationGuide, String nativePronunciation) {
        this.meaning = meaning;
        this.pronunciationGuide = pronunciationGuide;
        this.nativePronunciation = nativePronunciation;
    }
}
