// All backend calls go through this one helper.
// Paths stay relative (e.g. /api/parking) so the Vite proxy forwards them to the API Gateway.

const NETWORK_MESSAGE = 'Unable to connect to the server. Please make sure the backend services are running.';

export class ApiError extends Error {
  constructor(message, status = 0, fromServer = false) {
    super(message);
    this.status = status;
    this.fromServer = fromServer; // true when the message came from the backend response body
  }
}

// Pull the most useful message out of a backend error response.
function extractMessage(data) {
  if (!data) return null;
  if (typeof data === 'string') return data;
  if (Array.isArray(data.errors) && data.errors.length) {
    const parts = data.errors
      .map((e) => (typeof e === 'string' ? e : e.defaultMessage || e.message || ''))
      .filter(Boolean);
    if (parts.length) return parts.join('. ');
  }
  if (data.message) return data.message;
  return null;
}

function fallbackMessage(status) {
  if (status === 401) return 'Your credentials are invalid or your session has expired. Please log in again.';
  if (status === 403) return 'You do not have permission to do this.';
  if (status === 404) return 'The requested item was not found.';
  if (status >= 500) return 'The server could not process the request. Please make sure the backend services are running.';
  return 'Something went wrong. Please try again.';
}

export async function api(url, options = {}) {
  const token = localStorage.getItem('token');
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (token) headers.Authorization = `Bearer ${token}`;

  let response;
  try {
    response = await fetch(url, { ...options, headers });
  } catch {
    throw new ApiError(NETWORK_MESSAGE, 0, true);
  }

  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text.length < 200 ? text : null;
    }
  }

  if (!response.ok) {
    if ([502, 503, 504].includes(response.status)) throw new ApiError(NETWORK_MESSAGE, response.status, true);
    const message = extractMessage(data);
    throw new ApiError(message || fallbackMessage(response.status), response.status, Boolean(message));
  }
  return data;
}

// ---------------------------------------------------------------------------
// Named API functions. Components and App.jsx call these instead of using fetch directly.
// All paths are relative (/api/...) so the Vite proxy sends them to the API Gateway.
// ---------------------------------------------------------------------------
const json = (data) => JSON.stringify(data);

// Auth + users
export const login = (credentials) => api('/api/auth/login', { method: 'POST', body: json(credentials) });
export const register = (data) => api('/api/register', { method: 'POST', body: json(data) });
export const getUser = (id) => api(`/api/users/${id}`);
export const updateUser = (id, data) => api(`/api/users/${id}`, { method: 'PUT', body: json(data) });
export const deleteUser = (id) => api(`/api/users/${id}`, { method: 'DELETE' });

// Parking
export const getParking = () => api('/api/parking');
export const getParkingById = (id) => api(`/api/parking/${id}`);
export const createParking = (data) => api('/api/parking', { method: 'POST', body: json(data) });
export const updateParking = (id, data) => api(`/api/parking/${id}`, { method: 'PUT', body: json(data) });
export const deleteParking = (id) => api(`/api/parking/${id}`, { method: 'DELETE' });

// params: { location, vehicleType, status }. Empty values are left out of the URL.
// If every filter is empty this simply loads all parking spaces.
export function searchParking(params = {}) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    const text = String(value ?? '').trim();
    if (text) query.append(key, text);
  });
  const queryString = query.toString();
  return queryString ? api(`/api/parking/search?${queryString}`) : getParking();
}

// Bookings (the internal /confirm endpoint is intentionally NOT used – payment confirms bookings)
export const createBooking = (data) => api('/api/bookings', { method: 'POST', body: json(data) });
export const getMyBookings = () => api('/api/bookings/my');
export const getBookingById = (id) => api(`/api/bookings/${id}`);
export const cancelBooking = (id) => api(`/api/bookings/${id}/cancel`, { method: 'PUT' });

// Payments (simulated)
export const makePayment = (data) => api('/api/payments', { method: 'POST', body: json(data) });
export const getPaymentById = (id) => api(`/api/payments/${id}`);
