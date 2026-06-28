import { makeRedirectUri } from 'expo-auth-session';
import * as Linking from 'expo-linking';
import * as WebBrowser from 'expo-web-browser';
import { API_BASE_URL } from '../config/env';
import { getOAuthRedirectUri } from '../constants/auth';
import { clearAccessToken, getAccessToken, saveAccessToken } from './tokenStorage';
import { clearActiveFamilyId } from './familyStorage';

WebBrowser.maybeCompleteAuthSession();

const PROVIDER_LABELS = {
  kakao: '카카오',
  naver: '네이버',
  google: 'Google',
};

function getAppReturnUri(provider) {
  return makeRedirectUri({ path: `oauth/${provider}` });
}

function getAppLinkReturnUri(provider) {
  return makeRedirectUri({ path: `oauth/${provider}/link` });
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
  await clearActiveFamilyId();
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

export async function updateMyNickname(nickname) {
  const token = await getAccessToken();
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/me`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ nickname }),
  });

  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message ?? '이름을 저장하지 못했습니다.');
  }

  return response.json();
}

export async function logout() {
  await clearAccessToken();
  await clearActiveFamilyId();
}

export async function fetchOAuthLinks() {
  const token = await getAccessToken();
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/me/oauth-links`, {
    headers: { Authorization: `Bearer ${token}` },
  });

  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message ?? '연결된 로그인 정보를 불러오지 못했습니다.');
  }

  return response.json();
}

async function getOAuthLinkUrl(provider) {
  const token = await getAccessToken();
  const params = new URLSearchParams({
    redirectUri: getOAuthRedirectUri(provider),
    returnUri: getAppLinkReturnUri(provider),
  });
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/${provider}/link/url?${params}`, {
    headers: { Authorization: `Bearer ${token}` },
  });

  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message ?? `${PROVIDER_LABELS[provider]} 연결 URL을 가져오지 못했습니다.`);
  }

  return response.json();
}

export async function linkOAuthProvider(provider) {
  const appReturnUri = getAppLinkReturnUri(provider);
  const { authUrl } = await getOAuthLinkUrl(provider);
  const result = await WebBrowser.openAuthSessionAsync(authUrl, appReturnUri);

  if (result.type !== 'success') {
    throw new Error(`${PROVIDER_LABELS[provider]} 연결이 취소되었습니다.`);
  }

  const parsed = Linking.parse(result.url);
  const code = readQueryParam(parsed.queryParams?.code);
  const state = readQueryParam(parsed.queryParams?.state);

  if (!code || !state) {
    throw new Error(`${PROVIDER_LABELS[provider]} 연결 응답에 code 또는 state가 없습니다.`);
  }

  const response = await fetch(`${API_BASE_URL}/api/v1/auth/${provider}/link/callback`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ code, state }),
  });

  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message ?? `${PROVIDER_LABELS[provider]} 연결 실패 (${response.status})`);
  }

  return response.json();
}
