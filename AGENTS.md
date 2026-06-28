# Agent Guide (withy / kid-schedule-app)

AI Agent(Cursor 등)가 **어느 기기·어느 채팅에서든** context 없이 작업할 때의 진입점.

## Read first (순서)

1. [`docs/project-context.md`](docs/project-context.md) — 프로젝트 요약, API, 실행
2. [`docs/status-and-todo.md`](docs/status-and-todo.md) — **TODO, 미테스트, 알려진 이슈**
3. [`docs/cursor-workflow.md`](docs/cursor-workflow.md) — PC/모바일 Cursor 워크플로
4. [`docs/schedule-academy-plan.md`](docs/schedule-academy-plan.md) — 일정/학원/귀가 설계
5. [`docs/decisions.md`](docs/decisions.md) — 결정 로그

필요 시: `docs/mobile-screens.md`, `docs/sso-setup-guide.md`, `docs/database-erd.md`

## Stack

- Backend: Java 21, Spring Boot 3.5, PostgreSQL, Redis, Flyway (V7)
- Mobile: Expo SDK 54, React Native (JavaScript)
- Auth: Kakao/Naver/Google OAuth → JWT (Bearer)

## Do not commit

- `.env`, `mobile/.env` (secrets)

## Typical next tasks (2026-06-28)

See [`docs/status-and-todo.md`](docs/status-and-todo.md). 우선순위 예:

1. 반복 **series 수정 UI** (API 있음)
2. 가족 초대 **E2E** + integration test
3. 완료 일정 **peek UI**
4. 일정 검색, 월 picker, DROPOFF, JWT refresh

## 채팅 시작 템플릿

```
docs/project-context.md 와 docs/status-and-todo.md 를 읽고 [작업] 해줘.
```
