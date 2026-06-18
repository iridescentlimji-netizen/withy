import * as Linking from 'expo-linking';
import * as WebBrowser from 'expo-web-browser';
import { API_BASE_URL } from '../config/env';
import { KAKAO_REDIRECT_URI } from '../constants/auth';
import { clearAccessToken, saveAccessToken } from './tokenStorage';

WebBrowser.maybeCompleteAuthSession();

function getAppReturnUri() {
  return Linking.createURL('oauth/kakao');
}

export async function getKakaoAuthUrl() {
  const params = new URLSearchParams({
    redirectUri: KAKAO_REDIRECT_URI,
    returnUri: getAppReturnUri(),
  });
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/kakao/url?${params}`);
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message ?? '카카오 로그인 URL을 가져오지 못했습니다.');
  }
  return response.json();
}

export async function loginWithKakao() {
  const appReturnUri = getAppReturnUri();
  const { authUrl } = await getKakaoAuthUrl();
  const result = await WebBrowser.openAuthSessionAsync(authUrl, appReturnUri);

  if (result.type !== 'success') {
    throw new Error('카카오 로그인이 취소되었습니다.');
  }

  const parsed = Linking.parse(result.url);
  const code = parsed.queryParams?.code;
  const state = parsed.queryParams?.state;

  if (!code || !state) {
    throw new Error('카카오 로그인 응답에 code 또는 state가 없습니다.');
  }

  const response = await fetch(`${API_BASE_URL}/api/v1/auth/kakao/callback`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ code, state }),
  });

  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message ?? `카카오 로그인 실패 (${response.status})`);
  }

  const data = await response.json();
  await saveAccessToken(data.accessToken);
  return data;
}

export async function fetchCurrentUser(accessToken) {
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/me`, {
    headers: { Authorization: `Bearer ${accessToken}` },
  });

  if (!response.ok) {
    throw new Error('로그인 정보를 확인하지 못했습니다.');
  }

  return response.json();
}

export async function logout() {
  await clearAccessToken();
}
