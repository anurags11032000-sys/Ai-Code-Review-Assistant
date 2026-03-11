package com.aicodereviewassistant.repository;

import com.aicodereviewassistant.entity.PullRequestReviewTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PullRequestReviewTargetRepository extends JpaRepository<PullRequestReviewTarget, Long> {
    Optional<PullRequestReviewTarget> findByRepositoryIdAndPrNumber(Long repositoryId, Integer prNumber);
}
