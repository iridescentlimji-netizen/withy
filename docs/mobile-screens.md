# Mobile Screens — Figma ↔ Code

MVP 화면 목록과 Figma 링크. UI 작업 시 참고.

## 하단 탭 (확정)

| 순서 | 탭 이름 | 코드 | 비고 |
|------|---------|------|------|
| 1 | 홈 | `HomeScreen` | |
| 2 | 일정 | `ScheduleScreen` | 주간 캘린더, ⋯ 수정/삭제 |
| 3 | 학원관리 | `AcademyManagementScreen` | |
| 4 | 마이 | `MyScreen` | OAuth 연결, 가족 초대, 아이 등록 |

네비게이션: `mobile/src/navigation/MainTabs.js`, `RootNavigator.js`

## 화면 목록

| 화면 | 코드 파일 | API |
|------|-----------|-----|
| 로그인 | `LoginScreen.js` | `/api/v1/auth/*` |
| 가족 설정 | `FamilySetupScreen.js` | `POST /families`, `POST /join-requests` |
| 홈 | `HomeScreen.js` | `GET /home?familyId=` |
| 일정 목록 | `ScheduleScreen.js` | `GET /schedules`, `GET /schedules/calendar` |
| 일정 등록 | `ScheduleCreateScreen.js` | `POST /schedules` |
| 일정 수정 | `ScheduleEditScreen.js` | `GET/PUT /schedules/{id}` |
| 학원관리 | `AcademyManagementScreen.js` | academies CRUD |
| 아이 등록 | `ChildSetupScreen.js` | `POST /children` |
| 마이 | `MyScreen.js` | `/auth/me`, invite, join-requests |

## 일정 목록 UX

| 동작 | 구현 |
|------|------|
| ⋯ 메뉴 | `ScheduleListCard` → `ScheduleScreen.handlePressScheduleMenu` |
| 수정 | `ScheduleEdit` (modal) |
| 삭제 1회 | Alert → `cancelSchedule(id, 'OCCURRENCE')` |
| 삭제 이후 반복 | Alert → `cancelSchedule(id, 'FUTURE')` |
| canEdit=false | ⋯ 숨김 |

## Figma (참고)

| 화면 | Figma node |
|------|------------|
| 홈 | [30-2513](https://www.figma.com/design/BNjgD2wnOLOHKrvqm56pjq/Untitled?node-id=30-2513&m=dev) |
| 귀가 일정 | [49-2292](https://www.figma.com/design/BNjgD2wnOLOHKrvqm56pjq/Untitled?node-id=49-2292&m=dev) |
| 일정 빈 목록 | [62-2731](https://www.figma.com/design/BNjgD2wnOLOHKrvqm56pjq/Untitled?node-id=62-2731&m=dev) |
| 일정 목록 | [67-3218](https://www.figma.com/design/BNjgD2wnOLOHKrvqm56pjq/Untitled?node-id=67-3218&m=dev) |

## 디자인 토큰

`mobile/src/theme/index.js` — Figma Dev export 기준 (2026-06).

| 토큰 | HEX |
|------|-----|
| `background` | `#F3F6F9` |
| `surface` | `#FFFFFF` |
| `primary` | `#0066FF` |
| `statusActive` | `#009632` |
| `text` | `#171719` |

상세 typography → 이 파일 이전 버전 또는 `theme/index.js` 주석 참고.
