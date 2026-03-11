package com.aicodereviewassistant.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ai_suggestions")
public class AiSuggestion extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id")
    private CodeReview review;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggestion_type", nullable = false)
    private SuggestionType suggestionType;

    @Column(name = "severity", nullable = false)
    private String severity;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "details", columnDefinition = "TEXT", nullable = false)
    private String details;

    @Column(name = "suggested_fix", columnDefinition = "TEXT")
    private String suggestedFix;

    public Long getId() {
        return id;
    }

    public CodeReview getReview() {
        return review;
    }

    public void setReview(CodeReview review) {
        this.review = review;
    }

    public SuggestionType getSuggestionType() {
        return suggestionType;
    }

    public void setSuggestionType(SuggestionType suggestionType) {
        this.suggestionType = suggestionType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getSuggestedFix() {
        return suggestedFix;
    }

    public void setSuggestedFix(String suggestedFix) {
        this.suggestedFix = suggestedFix;
    }
}
