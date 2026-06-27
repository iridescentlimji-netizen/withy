# Project Context (Cursor / 모바일 이어하기용)

> **새 Agent 채팅 시작 시:** 이 파일 + [`schedule-academy-plan.md`](schedule-academy-plan.md) 먼저 읽기.

## 프로젝트

- **이름:** withy (아이 스케줄 MVP)
- **Repo:** https://github.com/iridescentlimji-netizen/withy.git
- **구조:** `backend/` (Spring Boot 3.5) + `mobile/` (Expo SDK 54)

## 완료된 것

### Backend
- Flyway V1–V5 (users, oauth, families, children, schedules …) — **비즈니스 API는 아직 없음**
- **SSO:** Kakao / Naver / Google + JWT
- OAuth: HTTP redirect → bridge HTML → deep link → callback
- SSO security: JWT secret 검증, returnUri allowlist, provider 검증, 테스트 17개

### Mobile
- LoginScreen (3 SSO), SecureStore JWT
- HomeScreen: 로그인 사용자 + API health (목업 홈 UI 전)

### API (현재 동작)
- `GET /api/v1/health`
- `GET/POST /api/v1/auth/{kakao|naver|google}/url|redirect|callback`
- `GET /api/v1/auth/me`

## 다음 작업 (확정 계획)

→ **[`docs/schedule-academy-plan.md`](schedule-academy-plan.md)** 전체 참고

1. **V6** — `academies`, `schedule_series`, `schedules` 확장
2. Academy + Family/Child API
3. Schedule series engine + Schedule API
4. Home dashboard API
5. Mobile: 홈/일정/학원관리 UI

### 핵심 설계 (한 줄)
- 학원 테이블 O / 귀가 = PICKUP / 반복 = series+occurrence / 이전일정 UI = 하이브리드

## 로컬 실행

```bash
docker compose up -d
cd backend && ./gradlew bootRun   # .env 자동 로드
cd mobile && npm run ios:dev      # 시뮬레이터 (OAuth)
# 또는
cd mobile && npm run start:simulator
```

- 시뮬레이터: `mobile/.env` → `EXPO_PUBLIC_API_URL=http://localhost:8080`
- 실기기: Mac Wi-Fi IP + `OAUTH_ALLOWED_EXP_HOSTS` (`.env.example` 참고)

## docs 목록

| 파일 | 용도 |
|------|------|
| [project-context.md](project-context.md) | 이 파일 — 요약 & 진입점 |
| [schedule-academy-plan.md](schedule-academy-plan.md) | 일정/학원/귀가 설계 & 구현 계획 |
| [decisions.md](decisions.md) | 결정 로그 |
| [sso-setup-guide.md](sso-setup-guide.md) | Kakao/Naver/Google 콘솔 설정 |
| [database-erd.md](database-erd.md) | 현재 DB ERD (V5 기준, V6 전) |

## Cursor / 모바일 웹 팁

1. **같은 Git repo·branch** (`main`) clone 후 `git pull`
2. 새 채팅: *「docs/project-context.md 와 schedule-academy-plan.md 읽고 V6부터 구현해줘」*
3. `.env`는 Git 제외 — `.env.example` 복사
4. 채팅 기록은 기기 간 **동기화 안 됨** → 결정은 `docs/` + commit

## 커밋 이력 (참고)

- `ac9c42c` — Initial MVP
- `7447536` — Naver/Google SSO
- `101aa7d` — LoginScreen / OAuth bridge fix
- `910e88a` — SSO security hardening
