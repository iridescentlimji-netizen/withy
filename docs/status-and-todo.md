# 진행 상태 · TODO · 미테스트 목록

> **마지막 갱신:** 2026-06-28  
> Agent는 작업 착수 전 이 파일과 [project-context.md](project-context.md)를 함께 읽을 것.

---

## 요약

| 구분 | 상태 |
|------|------|
| MVP 코어 (로그인·가족·일정·학원·홈) | ✅ 구현 완료 |
| P0 롤링 반복 materialize + 중복 방지 | ✅ 구현 완료 (V7 migration) |
| P1 일정 수정/삭제 (⋯ 메뉴) | ✅ 구현 완료 |
| P1 가족 초대/참여/승인 | ✅ 구현 완료 |
| OAuth 계정 연결 | ✅ 구현 완료 (실기기 검증 부족) |
| 백엔드 자동 테스트 | ✅ 27 tests pass |
| E2E / 실기기 수동 테스트 | ⚠️ 다수 미완 |

---

## ✅ 완료된 작업 (최근)

### P0 — 반복 일정 롤링 생성
- [x] `ScheduleSeriesMaterializer.ensureMaterializedThrough()` — 주 스크롤 시 4주 버퍼까지 생성
- [x] series `PESSIMISTIC_WRITE` 잠금 — 동시 API 호출 중복 INSERT 방지
- [x] Flyway **V7** — 중복 `(series_id, start_at)` 정리 + 유니크 인덱스

### P1 — 일정 취소/수정
- [x] `POST .../cancel` — `{ scope: "OCCURRENCE" | "FUTURE" }`
- [x] `FUTURE` — materialized occurrence 일괄 취소 + **`effectiveUntil` 갱신** (이후 자동 생성 중단)
- [x] `PUT .../schedules/{id}` — occurrence 수정
- [x] `PUT .../schedules/series/{seriesId}` — series 필드 수정 (API만)
- [x] Mobile: `ScheduleListCard` ⋯ → 수정 / 삭제 (반복 시 「이 일정만 / 이후 모두」)
- [x] Mobile: `ScheduleEditScreen`

### P1 — 가족 초대
- [x] `FamilyInviteService` / `FamilyJoinService` + Redis `InviteCodeStore`
- [x] API: invite-codes, join-requests, approve/reject
- [x] Mobile: `MyScreen` 초대·승인, `FamilySetupScreen` 코드 참여

### 기타
- [x] OAuth 계정 연결 (Kakao/Naver/Google) — 백엔드 + `MyScreen`
- [x] Kakao `scope` 제거 (KOE205), provider enum 소문자 변환
- [x] 로그인 시 `activeFamilyId` 초기화, FamilyContext stale ID 처리
- [x] 일정 탭 주간 캘린더 ◀ ▶, KST 날짜 처리

---

## 🔲 TODO (우선순위)

### P1 — 남은 MVP
- [ ] **반복 series 수정 UI** — API는 있음, mobile 미연결
- [ ] **완료 일정 하이브리드 UI** — peek 1건 + gradient (현재 `ScheduleCompletedSection`은 접기/펼치기만)
- [ ] **가족 초대 E2E** — 2계정 실제 OAuth로 승인 플로우 검증
- [ ] **FUTURE 삭제 재검증** — V7·effectiveUntil fix 이후 10월+ 주 이동 확인

### P2 — 기능 갭
- [ ] 일정 **검색** (`ScheduleScreen` 검색 버튼 noop)
- [ ] **월 선택** picker (주간 캘린더만 있음)
- [ ] **DROPOFF** 일정 타입 UI
- [ ] **귀가 픽업 보호자** 선택 UI (현재 로그인 사용자 고정)
- [ ] JWT **refresh** 토큰
- [ ] **푸시 알림**
- [ ] 아이 **PIN/QR** 로그인

### P2 — 품질·운영
- [ ] `FamilyInvite` integration test
- [ ] `FUTURE` cancel integration test (기존 DB 시나리오)
- [ ] Render / Supabase 배포
- [ ] CI (GitHub Actions — `./gradlew test`)

---

## ⚠️ 구현됨 · 테스트 미완 (수동 확인 필요)

| 기능 | 자동 테스트 | 수동 테스트 | 비고 |
|------|-------------|-------------|------|
| 롤링 materialize (12주+) | `ScheduleSeriesMaterializerIntegrationTest` | ⬜ | 9월 이후 주 스크롤 |
| 중복 일정 방지 (V7) | migration only | ⬜ | 9/22+ 1개씩만 보이는지 |
| 일정 ⋯ 수정 | ⬜ | ⬜ | `ScheduleEditScreen` |
| 일정 ⋯ 삭제 OCCURRENCE | `ScheduleApiIntegrationTest` (1회) | ⬜ | 반복 occurrence |
| 일정 ⋯ 삭제 FUTURE | `ScheduleApiIntegrationTest` (recurring) | ⬜ | 10월+ 비어야 함 |
| Series update API | ⬜ | ⬜ | mobile UI 없음 |
| OAuth 계정 연결 | `OAuthAccountLinkIntegrationTest` | ⬜ | 실기기 3 provider |
| Kakao 로그인 | ⬜ | ✅ | KOE205 fix 후 |
| Naver 로그인 | ⬜ | ⬜ | |
| Google 로그인 | ⬜ | ⬜ | |
| 가족 초대 코드 생성 | ⬜ | ⬜ | 마스터 `MyScreen` |
| 초대 코드 참여 | ⬜ | ⬜ | `FamilySetupScreen` |
| 가입 승인/거절 | ⬜ | ⬜ | 마스터 `MyScreen` |
| 실기기 API (Mac IP) | ⬜ | ⬜ | `EXPO_PUBLIC_API_URL` |
| Academy CRUD | `AcademyApiIntegrationTest` | ⬜ | |
| Home dashboard | ⬜ | ⬜ | |

---

## 수동 테스트 체크리스트 (QA)

### 일정
1. **일정** 탭 → 6/30 이후 날짜 선택 (데이터 시작일)
2. ▶로 10월·11월 이동 → 반복 일정 **1개씩**만 표시
3. ⋯ → **수정** → 저장 → 목록 반영
4. ⋯ → **삭제 → 이후 반복 일정 모두** → 해당일·이후 주 모두 비어야 함
5. ⋯ → **삭제 → 이 일정만** → 해당일만 사라짐

### 가족 초대
1. 마스터 **마이** → 초대 코드 생성
2. 다른 계정 → **초대 코드로 참여**
3. 마스터 **마이** → 승인 → 참여 계정에서 가족 데이터 표시

### OAuth
1. **마이** → 카카오/네이버/Google 연결
2. 다른 provider로 로그인 → 동일 가족 데이터

---

## 알려진 이슈 / 주의

| 이슈 | 상태 | 메모 |
|------|------|------|
| 9/22+ 일정 2개씩 표시 | ✅ fix | V7 + materializer lock |
| FUTURE 삭제 후 미래 일정 잔존 | ✅ fix | `effectiveUntil` 갱신 |
| bootRun Flyway V7 실패 | ✅ fix | dedupe SQL 개선 |
| 8080 already in use | — | 기존 java 프로세스 kill |
| 오늘 일정 없음 (6/28) | 데이터 | 스케줄 시작 6/30부터 |
| `cancelled=true` = UI 「삭제」 | by design | hard DELETE 아님 |

---

## Agent에게 시킬 때 추천 프롬프트

```
docs/project-context.md 와 docs/status-and-todo.md 를 읽고,
[예: 반복 series 수정 UI] 를 mobile에 연결해줘.
기존 ScheduleEditScreen / PUT series API 패턴을 따를 것.
```

```
docs/status-and-todo.md 의 미테스트 항목 중
[가족 초대 E2E] integration test를 backend에 추가해줘.
```
