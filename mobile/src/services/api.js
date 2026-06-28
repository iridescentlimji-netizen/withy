import { API_BASE_URL } from '../config/env';
import { getAccessToken } from './tokenStorage';

export async function checkApiHealth() {
  const response = await fetch(`${API_BASE_URL}/api/v1/health`);
  if (!response.ok) {
    throw new Error(`API health check failed: ${response.status}`);
  }
  return response.json();
}

async function parseError(response) {
  const body = await response.json().catch(() => ({}));
  return body.message ?? `요청 실패 (${response.status})`;
}

export async function apiFetch(path, options = {}) {
  const token = await getAccessToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers ?? {}),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export function listFamilies() {
  return apiFetch('/api/v1/families');
}

export function createFamily(name) {
  return apiFetch('/api/v1/families', {
    method: 'POST',
    body: JSON.stringify({ name }),
  });
}

export function listChildren(familyId) {
  return apiFetch(`/api/v1/families/${familyId}/children`);
}

export function createChild(familyId, body) {
  return apiFetch(`/api/v1/families/${familyId}/children`, {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

export function listAcademies(familyId, query) {
  const params = query ? `?query=${encodeURIComponent(query)}` : '';
  return apiFetch(`/api/v1/families/${familyId}/academies${params}`);
}

export function createAcademy(familyId, body) {
  return apiFetch(`/api/v1/families/${familyId}/academies`, {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

export function updateAcademy(familyId, academyId, body) {
  return apiFetch(`/api/v1/families/${familyId}/academies/${academyId}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  });
}

export function deleteAcademy(familyId, academyId) {
  return apiFetch(`/api/v1/families/${familyId}/academies/${academyId}`, {
    method: 'DELETE',
  });
}

export function createSchedule(familyId, body) {
  return apiFetch(`/api/v1/families/${familyId}/schedules`, {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

export function listSchedulesForDay(familyId, date, childId) {
  const params = new URLSearchParams({ date });
  if (childId) {
    params.set('childId', childId);
  }
  return apiFetch(`/api/v1/families/${familyId}/schedules?${params}`);
}

export function getScheduleCalendar(familyId, month, childId) {
  const params = new URLSearchParams({ month });
  if (childId) {
    params.set('childId', childId);
  }
  return apiFetch(`/api/v1/families/${familyId}/schedules/calendar?${params}`);
}

export function cancelSchedule(familyId, scheduleId, scope = 'OCCURRENCE') {
  return apiFetch(`/api/v1/families/${familyId}/schedules/${scheduleId}/cancel`, {
    method: 'POST',
    body: JSON.stringify({ scope }),
  });
}

export function getSchedule(familyId, scheduleId) {
  return apiFetch(`/api/v1/families/${familyId}/schedules/${scheduleId}`);
}

export function updateSchedule(familyId, scheduleId, body) {
  return apiFetch(`/api/v1/families/${familyId}/schedules/${scheduleId}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  });
}

export function updateScheduleSeries(familyId, seriesId, body) {
  return apiFetch(`/api/v1/families/${familyId}/schedules/series/${seriesId}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  });
}

export function createInviteCode(familyId, body) {
  return apiFetch(`/api/v1/families/${familyId}/invite-codes`, {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

export function submitJoinRequest(code) {
  return apiFetch('/api/v1/join-requests', {
    method: 'POST',
    body: JSON.stringify({ code }),
  });
}

export function listPendingJoinRequests(familyId) {
  return apiFetch(`/api/v1/families/${familyId}/join-requests`);
}

export function approveJoinRequest(familyId, requestId) {
  return apiFetch(`/api/v1/families/${familyId}/join-requests/${requestId}/approve`, {
    method: 'POST',
  });
}

export function rejectJoinRequest(familyId, requestId) {
  return apiFetch(`/api/v1/families/${familyId}/join-requests/${requestId}/reject`, {
    method: 'POST',
  });
}

export function getHome(familyId) {
  return apiFetch(`/api/v1/home?familyId=${familyId}`);
}
