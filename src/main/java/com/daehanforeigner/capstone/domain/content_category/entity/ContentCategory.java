package com.daehanforeigner.capstone.domain.content_category.entity;

import com.daehanforeigner.capstone.global.entity.GlobalEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "content_category")
public class ContentCategory extends GlobalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private CategoryType type;

    @Column(name = "name") // 카테고리 이름 ex) 학습, 드라마 대사, K문화, 신조어 등
    private String name;

    @Column(name = "description") // 카테고리 설명
    private String description;
}
