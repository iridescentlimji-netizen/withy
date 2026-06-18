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

## Next Steps

1. Naver / Google OAuth (동일 패턴)
2. Family / Child / Schedule API
3. Screen implementation from Figma
