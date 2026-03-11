package com.aicodereviewassistant.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pull_requests")
public class PullRequestReviewTarget extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pr_number", nullable = false)
    private Integer prNumber;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "base_branch", nullable = false)
    private String baseBranch;

    @Column(name = "head_branch", nullable = false)
    private String headBranch;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "state", nullable = false)
    private String state;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id")
    private SourceRepository repository;

    public Long getId() {
        return id;
    }

    public Integer getPrNumber() {
        return prNumber;
    }

    public void setPrNumber(Integer prNumber) {
        this.prNumber = prNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBaseBranch() {
        return baseBranch;
    }

    public void setBaseBranch(String baseBranch) {
        this.baseBranch = baseBranch;
    }

    public String getHeadBranch() {
        return headBranch;
    }

    public void setHeadBranch(String headBranch) {
        this.headBranch = headBranch;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public SourceRepository getRepository() {
        return repository;
    }

    public void setRepository(SourceRepository repository) {
        this.repository = repository;
    }
}
