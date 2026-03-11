package com.aicodereviewassistant.repository;

import com.aicodereviewassistant.entity.CodeReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {
    Page<CodeReview> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<CodeReview> findTopBySourceNameIgnoreCaseOrderByCreatedAtDesc(String sourceName);
    boolean existsBySourceNameIgnoreCaseAndSourceType(String sourceName, String sourceType);
}
