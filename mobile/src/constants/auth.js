import { API_BASE_URL } from '../config/env';

export const ACCESS_TOKEN_KEY = 'kid_schedule_access_token';

export const OAUTH_PROVIDERS = ['kakao', 'naver', 'google'];

export function getOAuthRedirectUri(provider) {
  return `${API_BASE_URL}/api/v1/auth/${provider}/redirect`;
}

export const KAKAO_REDIRECT_URI = getOAuthRedirectUri('kakao');
