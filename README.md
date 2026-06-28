# Kid Schedule App (MVP)

맞벌이 부부와 공동 양육자를 위한 아이 스케줄 관리 앱.

## Tech Stack

| Layer | Stack |
|-------|-------|
| Mobile | React Native + Expo SDK 54 (JavaScript) |
| Backend | Java 21 + Spring Boot 3.5 |
| Database | PostgreSQL (Supabase 예정) |
| Cache | Redis (초대 코드 TTL 24h) |
| Deploy | Render.com (예정) |

## Project Structure

```
kid-schedule-app/
├── mobile/          # Expo React Native app
├── backend/         # Spring Boot API
├── docker-compose.yml
└── .env.example
```

## Prerequisites

- Java 21 (`/usr/libexec/java_home -v 21`)
- Node.js LTS (nvm 권장)
- Docker Desktop (로컬 PostgreSQL / Redis)

## Quick Start

### 1. Infrastructure

```bash
docker compose up -d
cp .env.example .env
```

### 2. Backend

```bash
cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew bootRun
```

- Health: http://localhost:8080/actuator/health
- API ping: http://localhost:8080/api/v1/health

### 3. Mobile

```bash
cd mobile
npm install
npm start
```

Expo Go 앱(App Store, SDK 54)으로 QR 코드를 스캔하거나 `i` / `a` 로 시뮬레이터를 실행합니다.

> iPhone: **설정 → Expo Go → 로컬 네트워크** 허용 필요

## Security Principles (MVP)

- SSO only (Kakao / Naver / Google) — no password storage
- Minimal PII: child nickname + birth year only
- Roles: `MASTER`, `FAMILY`, `HELPER` with dynamic `can_edit`
- Invite codes: Redis, 8 chars, 24h TTL

## Kakao OAuth Setup (local)

1. [Kakao Developers](https://developers.kakao.com/)에서 앱 생성
2. **플랫폼** → iOS/Android 번들 ID 또는 Web 등록 (Expo Go 테스트는 Redirect URI만 맞으면 동작)
3. **카카오 로그인** 활성화
4. **Redirect URI** 등록 (REST API 키, `http://`만 가능):
   - 실기기: `http://{Mac_LAN_IP}:8080/api/v1/auth/kakao/redirect`
   - 시뮬레이터: `http://localhost:8080/api/v1/auth/kakao/redirect`
5. **REST API 키**를 `.env`의 `KAKAO_REST_API_KEY`에 설정
6. `KAKAO_REDIRECT_URI`를 콘솔 등록값과 동일하게 설정
7. Client Secret을 쓰는 앱 타입이면 `KAKAO_CLIENT_SECRET`도 설정

백엔드 실행 전 Redis가 필요합니다 (`docker compose up -d`).

### Auth API

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/auth/kakao/url` | 카카오 authorize URL + state |
| GET | `/api/v1/auth/kakao/redirect` | 카카오 OAuth redirect (code/state 수신) |
| POST | `/api/v1/auth/kakao/callback` | code/state → JWT 발급 |
| GET | `/api/v1/auth/me` | Bearer JWT로 현재 사용자 |

## Documentation

| Doc | Purpose |
|-----|---------|
| [`docs/project-context.md`](docs/project-context.md) | **Cursor 진입점** — 상태 요약, API, 실행 |
| [`docs/status-and-todo.md`](docs/status-and-todo.md) | **TODO·미테스트·QA 체크리스트** |
| [`docs/cursor-workflow.md`](docs/cursor-workflow.md) | PC/모바일 Cursor 워크플로 |
| [`docs/schedule-academy-plan.md`](docs/schedule-academy-plan.md) | 일정/학원/귀가 설계 |
| [`docs/mobile-screens.md`](docs/mobile-screens.md) | Figma ↔ 화면 ↔ API |
| [`docs/decisions.md`](docs/decisions.md) | 제품·아키텍처 결정 로그 |
| [`docs/sso-setup-guide.md`](docs/sso-setup-guide.md) | SSO 콘솔 설정 |
| [`docs/database-erd.md`](docs/database-erd.md) | ERD |
| [`AGENTS.md`](AGENTS.md) | AI Agent 1페이지 요약 |

**Cursor 새 채팅:** *「docs/project-context.md 와 docs/status-and-todo.md 를 읽고 [작업] 해줘」*

## Current status (2026-06-28)

MVP 코어 + P0/P1 (롤링 반복, 일정 수정/삭제, 가족 초대, OAuth 연결) 구현 완료.  
다음 작업 → [`docs/status-and-todo.md`](docs/status-and-todo.md)
