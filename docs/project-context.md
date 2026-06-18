# Project Context (Cursor / 모바일 이어하기용)

## 프로젝트

- **이름:** withy (아이 스케줄 MVP)
- **Repo:** https://github.com/iridescentlimji-netizen/withy.git
- **구조:** `backend/` (Spring Boot 3.5) + `mobile/` (Expo SDK 54)

## 완료된 것

- Flyway DB 9테이블 (users, oauth_links, families, children, schedules …)
- **SSO:** Kakao / Naver / Google + JWT
- OAuth redirect: HTTP 콘솔 URI → 백엔드 bridge HTML → `exp://.../oauth/{provider}` → 앱 callback
- 모바일: LoginScreen 3버튼, SecureStore JWT

## 로컬 실행

```bash
docker compose up -d
cd backend && ./gradlew bootRun   # .env 자동 로드
cd mobile && npm run start:simulator   # 시뮬레이터
```

## Cursor 컨텍스트 유지 팁

1. **같은 Git repo** clone (Cursor 웹/데스크톱 동일 브랜치)
2. **이 파일 + `docs/sso-setup-guide.md` + `README.md`** 먼저 읽히게 하기
3. 새 채팅 시작 시: "docs/project-context.md 읽고 이어서 …" 한 줄 요청
4. `.env`는 Git 제외 — 각 환경에서 `.env.example` 복사 후 키 입력
5. 채팅 기록은 기기 간 **자동 동기화되지 않음** → 중요 결정은 docs/ 또는 커밋 메시지에 남기기

## 다음 작업 후보

- Family / Child / Schedule API
- 초대 코드 (Redis) + MASTER 승인
- 아이 QR/PIN 페어링
