# SSO 설정 가이드 (Kakao / Naver / Google)

로컬 개발 기준 Redirect URI는 **http만** 등록합니다. (커스텀 scheme `kid-schedule://`은 카카오/네이버/구글 콘솔에 넣지 않음)

## 공통

`.env` (프로젝트 루트):

```env
JWT_SECRET=32자_이상_랜덤_문자열

# 시뮬레이터
KAKAO_REDIRECT_URIS=http://localhost:8080/api/v1/auth/kakao/redirect
NAVER_REDIRECT_URIS=http://localhost:8080/api/v1/auth/naver/redirect
GOOGLE_REDIRECT_URIS=http://localhost:8080/api/v1/auth/google/redirect

# 실기기 추가 시 (콤마로 병렬 등록)
# http://192.168.x.x:8080/api/v1/auth/{provider}/redirect
```

`mobile/.env`:

```env
# 시뮬레이터
EXPO_PUBLIC_API_URL=http://localhost:8080
# 실기기
# EXPO_PUBLIC_API_URL=http://192.168.x.x:8080
```

백엔드 `./gradlew bootRun` 전 `docker compose up -d` (Redis 필요).

---

## 1. Kakao

1. [Kakao Developers](https://developers.kakao.com/) → 앱 → **카카오 로그인 ON**
2. **[앱] → [플랫폼 키] → REST API 키 → Redirect URI**
   - `http://localhost:8080/api/v1/auth/kakao/redirect`
3. `.env`:
   ```env
   KAKAO_REST_API_KEY=...
   KAKAO_CLIENT_SECRET=...   # Client Secret 사용 시
   ```

---

## 2. Naver

1. [Naver Developers](https://developers.naver.com/apps) → 애플리케이션 등록
2. **API 설정 → 네아로(네이버 아이디로 로그인) → Callback URL**
   - `http://localhost:8080/api/v1/auth/naver/redirect`
3. **환경** → PC 웹 등 필요 시 추가
4. `.env`:
   ```env
   NAVER_CLIENT_ID=...
   NAVER_CLIENT_SECRET=...
   ```

---

## 3. Google

1. [Google Cloud Console](https://console.cloud.google.com/) → 프로젝트 생성
2. **APIs & Services → OAuth consent screen** (External, 테스트 사용자 추가)
3. **Credentials → Create OAuth client ID → Web application**
4. **Authorized redirect URIs**
   - `http://localhost:8080/api/v1/auth/google/redirect`
5. `.env`:
   ```env
   GOOGLE_CLIENT_ID=...apps.googleusercontent.com
   GOOGLE_CLIENT_SECRET=...
   ```

---

## 로그인 흐름 (참고)

1. 앱 → `GET /api/v1/auth/{provider}/url`
2. 인앱 브라우저 → 카카오/네이버/구글 로그인
3. `{provider}/redirect?code&state` → HTML이 `exp://.../oauth/{provider}` 로 브릿지
4. 앱 → `POST /api/v1/auth/{provider}/callback` → JWT

---

## API 목록

| Provider | URL | Redirect | Callback |
|----------|-----|----------|----------|
| Kakao | GET `/api/v1/auth/kakao/url` | GET `/api/v1/auth/kakao/redirect` | POST `/api/v1/auth/kakao/callback` |
| Naver | GET `/api/v1/auth/naver/url` | GET `/api/v1/auth/naver/redirect` | POST `/api/v1/auth/naver/callback` |
| Google | GET `/api/v1/auth/google/url` | GET `/api/v1/auth/google/redirect` | POST `/api/v1/auth/google/callback` |
