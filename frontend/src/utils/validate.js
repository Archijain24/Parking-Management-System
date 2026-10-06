// Simple client-side checks. They mirror the backend rules; the backend stays the source of truth.

export const isEmail = (value) => /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(String(value ?? '').trim());
export const isPhone = (value) => /^[0-9]{10}$/.test(String(value ?? '').trim());

export function validateName(name) {
  const v = (name || '').trim();
  if (!v) return 'Name is required.';
  if (v.length < 2 || v.length > 100) return 'Name must be between 2 and 100 characters.';
  return '';
}

export function validateEmail(email) {
  if (!(email || '').trim()) return 'Email is required.';
  if (!isEmail(email)) return 'Please enter a valid email address.';
  return '';
}

export function validatePhone(phone) {
  if (!String(phone ?? '').trim()) return 'Phone number is required.';
  if (!isPhone(phone)) return 'Phone number must be exactly 10 digits.';
  return '';
}

export function validatePassword(password) {
  if (!password) return 'Password is required.';
  if (password.length < 6) return 'Password must be at least 6 characters long.';
  return '';
}

// Returns the first error message in an errors object, or ''.
export const firstError = (errors) => Object.values(errors).find(Boolean) || '';

// Removes empty entries so we can check "is the form valid?" with Object.keys(...).length.
export const compact = (errors) =>
  Object.fromEntries(Object.entries(errors).filter(([, message]) => message));
