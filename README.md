# AI Code Review Assistant (Backend)

Production-style Spring Boot backend for AI-assisted source code review using Spring AI, PostgreSQL, Redis, and GitHub API integration.

## 1. Tech Stack
- Java 17
- Spring Boot 3
- Spring AI
- PostgreSQL
- Redis
- Maven
- Docker
- GitHub REST API

## 2. Core Capabilities
- Upload source code and analyze with AI
- Connect GitHub repository and analyze Pull Requests
- Detect bugs, security vulnerabilities, bad practices
- Generate inline-style review comments and refactor suggestions
- Explain complex code snippets
- Summarize pull requests and provide merge risk guidance
- Persist historical reviews and suggestions

## 3. Architecture
- `controller`: REST endpoints
- `service`: business orchestration
- `service.ai`: LLM prompt + response parsing layer
- `repository`: JPA data access
- `entity`: domain model
- `dto`: API request/response contracts
- `integration.github`: external GitHub client
- `exception`: centralized error handling
- `config/security`: infra and security setup

## 4. Project Structure

```text
ai-code-review-assistant
├── src/main/java/com/aicodereviewassistant
│   ├── config
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── exception
│   ├── integration/github
│   ├── mapper
│   ├── repository
│   ├── security
│   ├── service
│   │   ├── ai
│   │   ├── cache
│   │   └── impl
│   └── util
├── src/main/resources
│   ├── application.yml
│   ├── db/migration
│   └── prompts
├── Dockerfile
├── docker-compose.yml
├── .env.example
└── pom.xml
```

## 5. Database Schema
Tables:
- `users`
- `source_repositories`
- `pull_requests`
- `code_reviews`
- `ai_suggestions`

Managed by Flyway migrations.

## 6. REST APIs
### Upload code and analyze
`POST /api/v1/reviews/upload`

### Analyze GitHub PR
`POST /api/v1/github/analyze-pr`

### Connect GitHub repository
`POST /api/v1/github/connect-repository`

### Get review suggestions
`GET /api/v1/reviews/{reviewId}`

### Explain code
`POST /api/v1/reviews/explain`

### List previous reviews
`GET /api/v1/reviews?userId={id}&page=0&size=10`

### Pull request summary
`GET /api/v1/reviews/{reviewId}/summary`

## 7. Spring AI Configuration
Configured in `application.yml`:
- `spring.ai.google.genai.api-key`
- `spring.ai.google.genai.chat.options.model`

Prompt templates:
- `prompts/code-review-system-prompt.txt`
- `prompts/code-explanation-system-prompt.txt`
- `prompts/pr-summary-system-prompt.txt`

## 8. Security Best Practices
- Optional internal API key for non-GET routes (`X-API-KEY`)
- GitHub tokens stored per user (in production: encrypt with KMS/Vault)
- Validate all input with Bean Validation
- Centralized exception handling
- Disable Open Session in View
- Restrict actuator exposure

## 9. Scalability Improvements
- Redis caching for hot review lookups
- Stateless app nodes for horizontal scaling
- Async review processing with queue (future extension: Kafka/RabbitMQ)
- Token and prompt usage tracking for cost controls
- Batch PR file analysis for very large pull requests
- Add vector store + semantic chunking for large repositories

## 10. Run Locally

### Start dependencies
```bash
docker compose up -d
```

### Run app
```bash
mvn spring-boot:run
```

The app starts at `http://localhost:8080`.

## 11. Example Requests

### Upload code
```bash
curl -X POST http://localhost:8080/api/v1/reviews/upload \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "OrderService.java",
    "language": "java",
    "sourceCode": "public class A {}",
    "userEmail": "demo@aicodereview.local"
  }'
```

### Explain code
```bash
curl -X POST http://localhost:8080/api/v1/reviews/explain \
  -H "Content-Type: application/json" \
  -d '{
    "code": "for(int i=0;i<n;i++) sum+=arr[i];",
    "language": "java",
    "context": "performance critical path"
  }'
```
