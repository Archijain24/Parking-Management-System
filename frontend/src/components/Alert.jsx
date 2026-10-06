import { useEffect } from 'react';

const ICONS = { success: '✓', error: '✕', warning: '!', info: 'i' };

// type: 'success' | 'error' | 'warning' | 'info'
export default function Alert({ type = 'info', message, onClose }) {
  // Success and info messages close themselves; errors stay until dismissed.
  useEffect(() => {
    if (!message || type === 'error' || type === 'warning') return undefined;
    const timer = setTimeout(onClose, 5000);
    return () => clearTimeout(timer);
  }, [message, type, onClose]);

  if (!message) return null;

  return (
    <div className={`alert alert-${type}`} role={type === 'error' ? 'alert' : 'status'}>
      <span className="alert-icon" aria-hidden="true">{ICONS[type]}</span>
      <span className="alert-text">{message}</span>
      <button type="button" className="alert-close" onClick={onClose} aria-label="Dismiss message">×</button>
    </div>
  );
}
