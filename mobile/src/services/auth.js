import * as Linking from 'expo-linking';
import * as WebBrowser from 'expo-web-browser';
import { API_BASE_URL } from '../config/env';
import { getOAuthRedirectUri } from '../constants/auth';
import { clearAccessToken, saveAccessToken } from './tokenStorage';

WebBrowser.maybeCompleteAuthSession();

const PROVIDER_LABELS = {
  kakao: '카카오',
  naver: '네이버',
  google: 'Google',
};

function getAppReturnUri(provider) {
  return Linking.createURL(`oauth/${provider}`);
}

function readQueryParam(value) {
  if (Array.isArray(value)) {
    return value[0];
  }
  return value;
}

async function getOAuthAuthUrl(provider) {
  const params = new URLSearchParams({
    redirectUri: getOAuthRedirectUri(provider),
    returnUri: getAppReturnUri(provider),
  });
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/${provider}/url?${params}`);
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message ?? `${PROVIDER_LABELS[provider]} 로그인 URL을 가져오지 못했습니다.`);
  }
  return response.json();
}

export async function loginWithOAuthProvider(provider) {
  const appReturnUri = getAppReturnUri(provider);
  const { authUrl } = await getOAuthAuthUrl(provider);
  const result = await WebBrowser.openAuthSessionAsync(authUrl, appReturnUri);

  if (result.type !== 'success') {
    throw new Error(`${PROVIDER_LABELS[provider]} 로그인이 취소되었습니다.`);
  }

  const parsed = Linking.parse(result.url);
  const code = readQueryParam(parsed.queryParams?.code);
  const state = readQueryParam(parsed.queryParams?.state);

  if (!code || !state) {
    throw new Error(`${PROVIDER_LABELS[provider]} 로그인 응답에 code 또는 state가 없습니다.`);
  }

  const response = await fetch(`${API_BASE_URL}/api/v1/auth/${provider}/callback`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ code, state }),
  });

  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message ?? `${PROVIDER_LABELS[provider]} 로그인 실패 (${response.status})`);
  }

  const data = await response.json();
  await saveAccessToken(data.accessToken);
  return data;
}

export async function loginWithKakao() {
  return loginWithOAuthProvider('kakao');
}

export async function loginWithNaver() {
  return loginWithOAuthProvider('naver');
}

export async function loginWithGoogle() {
  return loginWithOAuthProvider('google');
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
