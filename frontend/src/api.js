const BASE = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '');

// Errors carry a status (or code 'NETWORK') so the UI can show a translated message.
export class ApiError extends Error {
  constructor(message, status, code) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

async function request(path, options = {}) {
  let res;
  try {
    res = await fetch(`${BASE}${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    });
  } catch {
    throw new ApiError('Network error', 0, 'NETWORK');
  }
  const body = await res.json().catch(() => null);
  if (!res.ok) {
    throw new ApiError(body?.error || `Request failed (${res.status})`, res.status);
  }
  return body;
}

export const register = (name, email, password) =>
  request('/api/customers', { method: 'POST', body: JSON.stringify({ name, email, password }) });

export const login = (email, password) =>
  request('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) });

export const listStylists = () => request('/api/stylists');

export const listServices = () => request('/api/services');

export const checkAvailability = (stylistId, startTime, endTime) =>
  request(`/api/stylists/${stylistId}/availability?` + new URLSearchParams({ startTime, endTime }));

export const bookAppointment = (payload) =>
  request('/api/appointments', { method: 'POST', body: JSON.stringify(payload) });

export const cancelAppointment = (appointmentId, requestingCustomerId) =>
  request(`/api/appointments/${appointmentId}`, {
    method: 'DELETE',
    body: JSON.stringify({ requestingCustomerId }),
  });
