package com.daehanforeigner.capstone.domain.content_category.repository;

import com.daehanforeigner.capstone.domain.content_category.entity.ContentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentCategoryRepository extends JpaRepository<ContentCategory, Long> {
}
