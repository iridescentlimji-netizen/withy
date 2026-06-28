# Project Context (Cursor / PC·모바일 공용)

> **새 Agent 채팅 첫 메시지 예시:**  
> `docs/project-context.md`와 `docs/status-and-todo.md`를 읽고, [작업 내용]을 진행해줘.

## 프로젝트

| 항목 | 값 |
|------|-----|
| 이름 | withy (아이 스케줄 MVP) |
| Repo | https://github.com/iridescentlimji-netizen/withy.git |
| 구조 | `backend/` Spring Boot 3.5 + `mobile/` Expo SDK 54 |
| DB | PostgreSQL + Flyway (V1–V7) |
| Cache | Redis (OAuth state, 초대 코드 24h TTL) |

## Cursor Agent 진입 순서

1. **[project-context.md](project-context.md)** — 이 파일 (요약)
2. **[status-and-todo.md](status-and-todo.md)** — 진행 상태·TODO·미테스트 목록 (**작업 전 필수**)
3. **[cursor-workflow.md](cursor-workflow.md)** — PC/모바일에서 Cursor로 개발하는 방법
4. **[schedule-academy-plan.md](schedule-academy-plan.md)** — 일정/학원/귀가 설계
5. **[decisions.md](decisions.md)** — 제품·아키텍처 결정 로그
6. **[mobile-screens.md](mobile-screens.md)** — Figma ↔ 화면 ↔ API 매핑
7. **[sso-setup-guide.md](sso-setup-guide.md)** — OAuth 콘솔 설정
8. **[database-erd.md](database-erd.md)** — ERD·Redis 키
9. **[../AGENTS.md](../AGENTS.md)** — Agent용 한 페이지 요약

`.cursor/plans/` 등 채팅 산출물보다 **`docs/`가 진실의 원천**이다.

---

## 완료된 기능 (2026-06-28 기준)

### Backend

- **Flyway V1–V7** — users, oauth, families, children, schedules, academies, schedule_series, 중복 일정 정리·유니크 인덱스
- **SSO** — Kakao / Naver / Google → JWT (Bearer)
- **OAuth 계정 연결** — 동일 사용자에 여러 provider 링크 (`OAuthAccountLinkService`)
- **Family / Child** — 목록·생성
- **Academy** — CRUD + 검색
- **Schedule** — 1회·반복 생성, 일별 grouped list, calendar, 단건 조회/수정/취소
- **반복 materialize** — `ScheduleSeriesMaterializer` (12주 horizon + 스크롤 시 롤링 생성, series 잠금)
- **취소 scope** — `OCCURRENCE` / `FUTURE` (`FUTURE` 시 `effectiveUntil` 갱신)
- **Series 수정 API** — `PUT .../schedules/series/{seriesId}`
- **가족 초대** — 초대 코드(Redis), 가입 요청, 마스터 승인/거절
- **Home** — now/next/todayCount + 오늘 PICKUP 요약
- **테스트** — 27개 (`./gradlew test`)

### Mobile

- LoginScreen (3 SSO), SecureStore JWT
- 가족 bootstrap / **초대 코드로 참여**
- FamilyContext (active family, load error retry)
- 탭: **홈 / 일정 / 학원관리 / 마이**
- 일정 주간 캘린더, 목록, **⋯ 메뉴 → 수정/삭제** (`ScheduleEditScreen`)
- 일정 등록 (1회·반복·학원·귀가)
- 학원 찾기 모달, 학원 CRUD, 아이 등록
- **마이** — 닉네임, OAuth 연결, 가족 초대·승인 (마스터)

---

## 주요 API

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/health` | 헬스체크 |
| * | `/api/v1/auth/*` | OAuth 로그인·연결·`/me` |
| GET/POST | `/api/v1/families` | 가족 |
| GET/POST | `/api/v1/families/{id}/children` | 아이 |
| * | `/api/v1/families/{id}/academies` | 학원 CRUD |
| POST/GET | `/api/v1/families/{id}/schedules` | 일정 |
| GET | `/api/v1/families/{id}/schedules/calendar` | 월별 count |
| PUT | `/api/v1/families/{id}/schedules/{scheduleId}` | occurrence 수정 |
| POST | `/api/v1/families/{id}/schedules/{scheduleId}/cancel` | 취소 `{ scope: OCCURRENCE \| FUTURE }` |
| PUT | `/api/v1/families/{id}/schedules/series/{seriesId}` | 반복 series 수정 |
| POST | `/api/v1/families/{id}/invite-codes` | 초대 코드 (마스터) |
| POST | `/api/v1/join-requests` | 가입 요청 |
| GET | `/api/v1/families/{id}/join-requests` | 승인 대기 목록 |
| POST | `/api/v1/families/{id}/join-requests/{id}/approve\|reject` | 승인/거절 |
| GET | `/api/v1/home?familyId=` | 홈 대시보드 |

---

## 로컬 실행

```bash
# 1) 인프라
docker compose up -d
cp .env.example .env   # 루트 — OAuth 키 등

# 2) 백엔드
cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew bootRun

# 3) 모바일
cd mobile
cp .env.example .env   # 있으면
npm install
npm run start:simulator   # iOS 시뮬레이터
```

| 환경 | `mobile/.env` |
|------|----------------|
| iOS 시뮬레이터 | `EXPO_PUBLIC_API_URL=http://localhost:8080` |
| 실기기 (Expo Go) | `EXPO_PUBLIC_API_URL=http://{Mac_WiFi_IP}:8080` |

- 백엔드: http://localhost:8080/api/v1/health
- Flyway V7 적용 필요 시 `./gradlew bootRun` 한 번 (중복 일정 정리 포함)
- 8080 점유 시: `lsof -i :8080` → `kill <PID>`

---

## 자주 건드리는 파일

| 영역 | 경로 |
|------|------|
| 일정 API | `backend/.../schedule/ScheduleService.java`, `ScheduleSeriesMaterializer.java` |
| 일정 UI | `mobile/src/screens/ScheduleScreen.js`, `ScheduleEditScreen.js` |
| OAuth | `backend/.../auth/`, `mobile/src/services/auth.js` |
| 가족 초대 | `backend/.../family/FamilyInviteService.java`, `FamilyJoinService.java` |
| 마이 | `mobile/src/screens/MyScreen.js`, `FamilySetupScreen.js` |
| 마이그레이션 | `backend/src/main/resources/db/migration/` |

---

## Git / Secret

- **커밋 금지:** `.env`, `mobile/.env` (키·JWT secret)
- **참고:** `.env.example`, `mobile/.env.example` (있을 경우)

---

## 다음 작업

상세 TODO·미테스트 항목 → **[status-and-todo.md](status-and-todo.md)**
