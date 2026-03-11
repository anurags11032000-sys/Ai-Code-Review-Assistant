package com.aicodereviewassistant.repository;

import com.aicodereviewassistant.entity.SourceRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SourceRepositoryRepository extends JpaRepository<SourceRepository, Long> {
    Optional<SourceRepository> findByRepoUrl(String repoUrl);
}
