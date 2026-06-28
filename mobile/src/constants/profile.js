export const PLACEHOLDER_NICKNAMES = ['카카오 사용자', '네이버 사용자', 'Google 사용자'];

export function isPlaceholderNickname(nickname) {
  return !nickname || PLACEHOLDER_NICKNAMES.includes(nickname);
}

export const MEMBER_ROLE_LABELS = {
  MASTER: '보호자',
  FAMILY: '보호자',
  HELPER: '도움 보호자',
};

export function formatMemberRole(role) {
  return MEMBER_ROLE_LABELS[role] ?? '보호자';
}
