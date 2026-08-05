package com.daehanforeigner.capstone.domain.learning_content.entity;

import com.daehanforeigner.capstone.domain.content_category.entity.ContentCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "learning_content")
public class LearningContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Long contentId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ContentCategory contentCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type") // 음절, 단어, 문장
    private ContentType contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty") // 초급, 중급, 고급
    private Difficulty difficulty;

    @Column(name = "text") // 학습 텍스트
    private String text;

    @Column(name = "meaning") // 의미
    private String meaning;

    @Column(name = "example_sentence") // 예문
    private String exampleSentence;

    @Column(name = "pronunciation_guide") // 발음 가이드
    private String pronunciationGuide;

    // 학습 콘텐츠 정보 수정 처리 (controller단에서는 putMapping으로 처리)
    public void update(ContentCategory contentCategory, ContentType contentType, Difficulty difficulty, String text, String meaning, String exampleSentence, String pronunciationGuide) {
        this.contentCategory = contentCategory;
        this.contentType = contentType;
        this.difficulty = difficulty;
        this.text = text;
        this.meaning = meaning;
        this.exampleSentence = exampleSentence;
        this.pronunciationGuide = pronunciationGuide;
    }
}
