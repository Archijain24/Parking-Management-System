export const money = (value) => `₹${value}`;

export function dateTime(value) {
  const d = new Date(value);
  return Number.isNaN(d.getTime())
    ? value
    : d.toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
}

// Picks the badge colour for a status (parking, booking or payment).
export function badgeClass(status) {
  const s = String(status || '').toUpperCase();
  if (['AVAILABLE', 'CONFIRMED', 'SUCCESS', 'SUCCESSFUL', 'PAID'].includes(s)) return 'badge badge-good';
  if (['UNAVAILABLE', 'CANCELLED', 'FAILED'].includes(s)) return 'badge badge-bad';
  return 'badge badge-neutral';
}
