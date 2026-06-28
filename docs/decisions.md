# Architecture & Product Decisions

짧은 결정 로그. Cursor Agent가 컨텍스트 없이 작업할 때 참고.

| 날짜 | 결정 | 이유 |
|------|------|------|
| 2026-06 | SSO only, 최소 PII | 맞벌이 부부 MVP, 닉네임은 SSO 또는 앱 내 수집 |
| 2026-06 | OAuth redirect = HTTP 백엔드 URI → bridge → deep link | Kakao/Naver/Google 콘솔이 custom scheme 거부 |
| 2026-06 | JWT HS256, 60분, refresh 없음 (MVP) | 단순화; 로그아웃은 클라이언트 token 삭제 |
| 2026-06 | SSO security hardening | JWT secret 강제, returnUri allowlist, provider 검증, @Transactional 분리 |
| 2026-06 | `academies` 테이블 추가 | 학원관리 탭 + 일정 등록 「학원찾기」 |
| 2026-06 | 귀가 = `schedules` PICKUP (전용 테이블 없음) | 일정 목록 통합; 홈은 오늘 PICKUP만 요약 |
| 2026-06 | 반복 = `schedule_series` + materialized `schedules` | 캘린더 daily count, 단건 수정/예외 처리 |
| 2026-06 | 반복 패턴: WEEKLY, BIWEEKLY, MONTHLY(day-of-month) | 학원 수업 패턴 반영 |
| 2026-06 | 이전 일정 UI = **하이브리드** | 예정 집중 + 최근 완료 1건 peek + 펼치기 |
| 2026-06 | 학원 찾기 = 선택 pre-fill + 신규 등록 | 중복 입력 방지, schedule에 category 스냅샷 |
| 2026-06 | 하단 탭 4개: **홈 / 일정 / 학원관리 / 마이** | Figma MVP 목업과 일치; 기존 「관리」 탭 분리 |
| 2026-06 | `schedules.cancelled` = UI 「삭제」(soft delete) | 반복 예외·FUTURE 취소; hard DELETE 없음 |
| 2026-06 | 반복 materialize rolling + V7 dedupe | 12주 horizon 이후 스크롤 생성; `(series_id,start_at)` 유니크 |
| 2026-06 | FUTURE cancel → `effectiveUntil` 갱신 | materializer가 취소 후 미래 일정 재생성하지 않도록 |
| 2026-06 | OAuth 계정 연결 (multi-provider → 1 user) | 카카오/네이버/구글 동일 가족 데이터 |
| 2026-06 | 가족 초대 Redis 8자 코드 24h | join_requests 승인 후 guardian 추가 |

## 미결정 / 구현 시 확정

- 매월 31일 → 2월 **말일 보정** vs skip
- 알림, 날씨 위젯, DROPOFF 일정 타입
- JWT refresh
