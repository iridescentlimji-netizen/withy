// Figma 디자인 토큰 (홈 화면 Dev Mode HTML export 기준)
// Font: Pretendard Variable — 앱에 폰트 패키지 연결 전까지 시스템 폰트 사용

export const colors = {
  // 배경
  background: '#F3F6F9',
  surface: '#FFFFFF',
  surfaceMuted: '#F3F6F9',
  surfaceSubtle: '#F1F3F5',

  // 텍스트
  text: '#171719',
  textSecondary: '#454F5D',
  textTertiary: '#989BA2',
  textStrong: '#000000',

  // 브랜드 / 탭
  primary: '#0066FF',
  tabInactive: '#989BA2',

  // 상태 (수업 중 등)
  statusActive: '#009632',
  statusActiveBg: 'rgba(0, 150, 50, 0.20)',

  // 구분선
  border: '#E5E7EB',
  divider: '#989BA2',
};

export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
};

export const radius = {
  sm: 8,
  md: 20,
  lg: 32,
  pill: 53,
  full: 9999,
};

export const typography = {
  userName: {
    fontSize: 20,
    fontWeight: '700',
    lineHeight: 24,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '700',
    lineHeight: 22,
  },
  title: {
    fontSize: 18,
    fontWeight: '700',
    lineHeight: 22,
  },
  body: {
    fontSize: 16,
    fontWeight: '500',
    lineHeight: 20,
  },
  bodySemibold: {
    fontSize: 16,
    fontWeight: '600',
    lineHeight: 18,
  },
  bodySmall: {
    fontSize: 14,
    fontWeight: '500',
    lineHeight: 18,
  },
  bodySmallSemibold: {
    fontSize: 14,
    fontWeight: '600',
    lineHeight: 18,
  },
  badge: {
    fontSize: 14,
    fontWeight: '700',
    lineHeight: 18,
  },
  caption: {
    fontSize: 12,
    fontWeight: '500',
    lineHeight: 16,
  },
};
