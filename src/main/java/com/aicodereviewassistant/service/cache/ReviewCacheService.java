package com.aicodereviewassistant.service.cache;

import com.aicodereviewassistant.dto.CodeReviewResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ReviewCacheService {

    @Cacheable(cacheNames = "reviewById", key = "#reviewId", unless = "#result == null")
    public CodeReviewResponse getCachedReview(Long reviewId) {
        return null;
    }

    @CachePut(cacheNames = "reviewById", key = "#review.reviewId()", unless = "#result == null")
    public CodeReviewResponse cacheReview(CodeReviewResponse review) {
        return review;
    }

    @CacheEvict(cacheNames = "reviewById", key = "#reviewId")
    public void evict(Long reviewId) {
    }
}
