export const SCHEDULE_TYPES = {
  ACTIVITY: 'ACTIVITY',
  PICKUP: 'PICKUP',
  DROPOFF: 'DROPOFF',
  OTHER: 'OTHER',
};

export const SCHEDULE_TYPE_LABELS = {
  ACTIVITY: '학원',
  PICKUP: '귀가',
  DROPOFF: '등원',
  OTHER: '기타',
};

export const RECURRENCE_TYPES = {
  NONE: 'NONE',
  WEEKLY: 'WEEKLY',
  BIWEEKLY: 'BIWEEKLY',
  MONTHLY: 'MONTHLY',
};

export const RECURRENCE_LABELS = {
  NONE: '1회성',
  WEEKLY: '매주',
  BIWEEKLY: '격주',
  MONTHLY: '매월',
};

export const SCHEDULE_STATUS = {
  UPCOMING: 'UPCOMING',
  IN_PROGRESS: 'IN_PROGRESS',
  COMPLETED: 'COMPLETED',
};

export const SCHEDULE_STATUS_LABELS = {
  IN_PROGRESS: '수업중',
  UPCOMING: '예정',
  COMPLETED: '완료',
};

export const SUBJECT_CATEGORY_ICONS = {
  LANGUAGE: '📖',
  MATH: '√',
  ENGLISH: '📘',
  SOCIAL: '🌏',
  SCIENCE: '🔬',
  MUSIC: '🎵',
  ART: '🎨',
  PE: '⚽',
  SECOND_LANGUAGE: '🗣️',
  OTHER: '📋',
};

export const SUBJECT_CATEGORIES = [
  'LANGUAGE',
  'MATH',
  'ENGLISH',
  'SOCIAL',
  'SCIENCE',
  'MUSIC',
  'ART',
  'PE',
  'SECOND_LANGUAGE',
  'OTHER',
];

export const SUBJECT_CATEGORY_LABELS = {
  LANGUAGE: '국어',
  MATH: '수학',
  ENGLISH: '영어',
  SOCIAL: '사회',
  SCIENCE: '과학',
  MUSIC: '음악',
  ART: '미술',
  PE: '체육',
  SECOND_LANGUAGE: '제2외국어',
  OTHER: '기타',
};

export const DAY_OF_WEEK_OPTIONS = [
  { value: 'MONDAY', label: '월' },
  { value: 'TUESDAY', label: '화' },
  { value: 'WEDNESDAY', label: '수' },
  { value: 'THURSDAY', label: '목' },
  { value: 'FRIDAY', label: '금' },
  { value: 'SATURDAY', label: '토' },
  { value: 'SUNDAY', label: '일' },
];
