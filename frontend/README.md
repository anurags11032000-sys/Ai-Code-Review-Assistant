# AI Code Review Assistant Frontend

React + Vite frontend for the Spring Boot backend.

## Setup

1. Create env file:
```bash
cp .env.example .env
```

2. Install deps:
```bash
npm install
```

3. Run dev server:
```bash
npm run dev
```

Frontend runs on `http://localhost:5174`.

## Required backend setting
If backend runs with CORS enabled, set backend env:
```env
FRONTEND_URL=http://localhost:5174
```

## Features
- Upload source code and get AI review suggestions
- Explain code snippets
- Connect GitHub repository
- Analyze pull requests
- Browse previous reviews
