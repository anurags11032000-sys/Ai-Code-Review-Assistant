CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    github_username VARCHAR(255) UNIQUE,
    github_access_token TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE source_repositories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    repo_url VARCHAR(500) NOT NULL UNIQUE,
    default_branch VARCHAR(100) NOT NULL,
    owner_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE pull_requests (
    id BIGSERIAL PRIMARY KEY,
    pr_number INT NOT NULL,
    title VARCHAR(500) NOT NULL,
    base_branch VARCHAR(100) NOT NULL,
    head_branch VARCHAR(100) NOT NULL,
    author VARCHAR(255) NOT NULL,
    state VARCHAR(50) NOT NULL,
    repository_id BIGINT NOT NULL REFERENCES source_repositories(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE(repository_id, pr_number)
);

CREATE TABLE code_reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    repository_id BIGINT REFERENCES source_repositories(id),
    pull_request_id BIGINT REFERENCES pull_requests(id),
    source_name VARCHAR(500) NOT NULL,
    source_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    overall_summary TEXT,
    risk_score INT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE ai_suggestions (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL REFERENCES code_reviews(id),
    suggestion_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    file_path VARCHAR(500),
    line_number INT,
    title VARCHAR(255) NOT NULL,
    details TEXT NOT NULL,
    suggested_fix TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_reviews_user_id_created_at ON code_reviews(user_id, created_at DESC);
CREATE INDEX idx_suggestions_review_id ON ai_suggestions(review_id);
