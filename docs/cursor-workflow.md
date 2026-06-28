# Cursor AI 워크플로 — PC·모바일 공용

> **목표:** 집 PC, 회사 PC, iPad/모바일 Cursor 등 **어느 환경에서든** 같은 repo를 열고 Agent가 맥락 없이 작업을 이어갈 수 있게 한다.

---

## 1. 저장소 준비 (최초 1회)

```bash
git clone https://github.com/iridescentlimji-netizen/withy.git
cd withy   # 또는 kid-schedule-app 로컬 폴더명
cp .env.example .env
cd mobile && cp .env.example .env 2>/dev/null; cd ..
docker compose up -d
```

- OAuth 키는 `.env` / `mobile/.env`에만 (Git 미포함)
- [sso-setup-guide.md](sso-setup-guide.md) 참고

---

## 2. Cursor에서 프로젝트 열기

| 환경 | 방법 |
|------|------|
| **Mac/PC** | Cursor → Open Folder → repo 루트 |
| **원격/다른 PC** | clone 후 동일 |
| **모바일 Cursor** | GitHub 연동 또는 clone → repo 루트를 workspace로 |

**Workspace root = repo 루트** (`backend/`, `mobile/`, `docs/`가 보이는 위치)

---

## 3. Agent 채팅 시작 (필수 프롬프트)

새 채팅마다 아래 중 하나로 시작:

```
docs/project-context.md 와 docs/status-and-todo.md 를 읽고 작업해줘.
[구체적 요청]
```

또는:

```
withy 프로젝트야. AGENTS.md → docs/project-context.md → docs/status-and-todo.md 순으로 읽고
[구체적 요청]
```

**규칙:** `.cursor/rules/project.mdc`가 `docs/` 우선을 강제한다. 예전 채팅/plan 파일만 보고 작업하지 말 것.

---

## 4. 로컬 개발 환경

### Backend (모든 PC 동일)

```bash
cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS
./gradlew bootRun
```

- Health: http://localhost:8080/api/v1/health
- 테스트: `./gradlew test`

### Mobile

```bash
cd mobile
npm install
npm run start:simulator    # iOS 시뮬레이터 + localhost API
# 또는
npm start                  # Expo Go QR
```

| 실행 대상 | `mobile/.env` |
|-----------|----------------|
| iOS Simulator | `EXPO_PUBLIC_API_URL=http://localhost:8080` |
| Android Emulator | `http://10.0.2.2:8080` |
| 실기기 (Expo Go) | `http://{개발_Mac_ WiFi_IP}:8080` |

실기기 OAuth redirect는 Mac IP 기준 — [sso-setup-guide.md](sso-setup-guide.md)

---

## 5. PC vs 모바일 Cursor 역할 나누기

| 작업 | 추천 환경 |
|------|-----------|
| Backend Java, Flyway, 테스트 | PC (Java 21 + Docker) |
| Mobile UI, Expo | PC (시뮬레이터) 또는 Mac |
| docs 수정, 이슈 triage, PR 리뷰 | PC / 모바일 Cursor 모두 가능 |
| 실기기 SSO·초대 코드 QA | 실기기 + Mac에서 backend 실행 |

모바일 Cursor만 있는 경우:
- **docs/README 수정, TODO 정리, 이슈 분석** → 가능
- **bootRun / Expo 실행** → 해당 기기에 Java·Docker·Node 없으면 PC에서 실행 후 API URL만 맞추기

---

## 6. 작업 후 docs 갱신 (필수)

기능 추가·버그 fix·TODO 완료 시 **같은 PR/커밋에서** 업데이트:

1. `docs/status-and-todo.md` — 체크박스·미테스트 표
2. `docs/project-context.md` — API·완료 목록 (큰 변경만)
3. `docs/decisions.md` — 제품/아키텍처 결정 추가
4. 필요 시 `docs/schedule-academy-plan.md` Phase 상태

Agent에게:

```
방금 [기능] 구현했어. docs/status-and-todo.md 와 project-context.md 반영해줘.
```

---

## 7. 커밋·푸시

```bash
git status
git add backend/ mobile/ docs/ AGENTS.md README.md
git commit -m "설명"
git push origin main
```

- `.env`, `mobile/.env` **절대 add 하지 않기**
- Flyway migration 추가 시 기존 DB는 `bootRun`으로 V{n} 적용

---

## 8. 문서 맵 (quick reference)

```
docs/
├── project-context.md      ← Agent 1순위
├── status-and-todo.md      ← TODO / 미테스트
├── cursor-workflow.md      ← 이 파일
├── schedule-academy-plan.md
├── mobile-screens.md
├── decisions.md
├── sso-setup-guide.md
└── database-erd.md
AGENTS.md                   ← 1페이지 요약
README.md                   ← 사람용 Quick Start
.cursor/rules/project.mdc   ← Cursor alwaysApply 규칙
```
