export function formatTime(isoString) {
  if (!isoString) {
    return '';
  }

  return new Date(isoString).toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
    timeZone: 'Asia/Seoul',
  });
}

export function formatTimeRange(startAt, endAt) {
  if (!startAt || !endAt) {
    return '';
  }
  return `${formatTime(startAt)} - ${formatTime(endAt)}`;
}

export function formatClock(date = new Date()) {
  return date.toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
    timeZone: 'Asia/Seoul',
  });
}

export function formatHomeDate(date = new Date()) {
  return date.toLocaleDateString('en-US', {
    month: 'long',
    day: '2-digit',
    year: 'numeric',
    timeZone: 'Asia/Seoul',
  });
}

export function formatRelativeUntil(isoString) {
  if (!isoString) {
    return '';
  }

  const diffMs = new Date(isoString).getTime() - Date.now();
  if (diffMs <= 0) {
    return '곧';
  }

  const totalMinutes = Math.floor(diffMs / 60000);
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;

  if (hours > 0 && minutes > 0) {
    return `${hours}시간 ${minutes}분 후`;
  }
  if (hours > 0) {
    return `${hours}시간 후`;
  }
  return `${minutes}분 후`;
}

export function formatTodayLabel() {
  return new Date().toLocaleDateString('ko-KR', {
    month: 'long',
    day: 'numeric',
    weekday: 'short',
    timeZone: 'Asia/Seoul',
  });
}

export function toDateParam(date = new Date()) {
  return formatDateParamInTimeZone(date, 'Asia/Seoul');
}

export function formatDateParamInTimeZone(date, timeZone) {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(date);

  const year = parts.find((part) => part.type === 'year')?.value;
  const month = parts.find((part) => part.type === 'month')?.value;
  const day = parts.find((part) => part.type === 'day')?.value;
  return `${year}-${month}-${day}`;
}

export function parseDateParam(dateParam) {
  const [year, month, day] = dateParam.split('-').map(Number);
  return new Date(year, month - 1, day);
}

export function addDaysToDateParam(dateParam, days) {
  const date = parseDateParam(dateParam);
  date.setDate(date.getDate() + days);
  return toDateParam(date);
}

export function toKstInstant(date, time) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(time.getHours()).padStart(2, '0');
  const minutes = String(time.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}:00+09:00`;
}

export function toLocalTimeParam(time) {
  const hours = String(time.getHours()).padStart(2, '0');
  const minutes = String(time.getMinutes()).padStart(2, '0');
  return `${hours}:${minutes}:00`;
}

export function formatDateLabel(dateParam) {
  const date = parseDateParam(dateParam);
  return date.toLocaleDateString('ko-KR', {
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  });
}

export function formatMonthYearLabel(dateParam) {
  const date = parseDateParam(dateParam);
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    timeZone: 'Asia/Seoul',
  });
}

export function toMonthParam(dateParam) {
  const date = parseDateParam(dateParam);
  return toDateParam(new Date(date.getFullYear(), date.getMonth(), 1));
}

export const WEEKDAY_SHORT_LABELS = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];

export function getWeekDateParams(dateParam) {
  const date = parseDateParam(dateParam);
  const start = new Date(date);
  start.setDate(date.getDate() - date.getDay());

  return Array.from({ length: 7 }, (_, index) => {
    const next = new Date(start);
    next.setDate(start.getDate() + index);
    return toDateParam(next);
  });
}

export function addWeeksToDateParam(dateParam, weeks) {
  return addDaysToDateParam(dateParam, weeks * 7);
}
