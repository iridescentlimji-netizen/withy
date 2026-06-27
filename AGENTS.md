# Agent Guide (withy / kid-schedule-app)

AI Agent(Cursor 등)가 **컨텍스트 없이** 작업할 때의 진입점.

## Read first

1. [`docs/project-context.md`](docs/project-context.md) — 프로젝트 요약, 완료/미완, 실행 방법
2. [`docs/schedule-academy-plan.md`](docs/schedule-academy-plan.md) — **현재 구현할** 일정/학원/귀가 설계
3. [`docs/decisions.md`](docs/decisions.md) — 제품·아키텍처 결정 로그

## Stack

- Backend: Java 21, Spring Boot 3.5, PostgreSQL, Redis, Flyway
- Mobile: Expo SDK 54, React Native
- Auth: Kakao/Naver/Google OAuth → JWT (Bearer)

## Do not commit

- `.env`, `mobile/.env` (secrets)

## Typical next task

Implement **Phase 1** from `schedule-academy-plan.md`: Flyway V6 + Academy API.
