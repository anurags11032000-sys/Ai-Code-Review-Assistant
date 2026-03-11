package com.aicodereviewassistant.repository;

import com.aicodereviewassistant.entity.AiSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiSuggestionRepository extends JpaRepository<AiSuggestion, Long> {
    List<AiSuggestion> findByReviewIdOrderByCreatedAtAsc(Long reviewId);
}
