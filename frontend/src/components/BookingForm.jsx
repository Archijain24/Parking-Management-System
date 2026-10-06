import { useState } from 'react';
import Field from './Field';
import { badgeClass, dateTime, money } from '../utils/format';

// Customer only. Shows the selected parking, the date/time form, and then the booking confirmation.
export default function BookingForm({ parking, booking, onBook, onReset, onBrowse, onInvalid, busy }) {
  const [form, setForm] = useState({ startTime: '', endTime: '' });
  const [errors, setErrors] = useState({});

  const change = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const submit = (e) => {
    e.preventDefault();
    const found = {};
    if (!form.startTime) found.startTime = 'Start time is required.';
    if (!form.endTime) found.endTime = 'End time is required.';
    // Allow a minute of slack so "now" is not rejected
    if (form.startTime && new Date(form.startTime) < new Date(Date.now() - 60000)) {
      found.startTime = 'Start time cannot be in the past.';
    }
    if (form.startTime && form.endTime && new Date(form.endTime) <= new Date(form.startTime)) {
      found.endTime = 'End time must be after start time.';
    }
    setErrors(found);
    const first = Object.values(found)[0];
    if (first) { onInvalid(first); return; }
    onBook(form);
  };

  // After booking: show confirmation
  if (booking) {
    return (
      <section className="card">
        <div className="card-head"><h2>Booking confirmed</h2></div>
        <dl className="details">
          <div><dt>Booking ID</dt><dd>#{booking.bookingId}</dd></div>
          <div><dt>Parking</dt><dd>{booking.parking.location} (#{booking.parking.parkingId})</dd></div>
          <div><dt>Start</dt><dd>{dateTime(booking.startTime)}</dd></div>
          <div><dt>End</dt><dd>{dateTime(booking.endTime)}</dd></div>
          <div><dt>Total amount</dt><dd className="strong">{money(booking.totalAmount)}</dd></div>
          <div><dt>Status</dt><dd><span className="badge badge-neutral">{booking.status || booking.bookingStatus || 'Awaiting payment'}</span></dd></div>
        </dl>
        {!['CONFIRMED', 'CANCELLED'].includes(String(booking.status || booking.bookingStatus || '').toUpperCase()) && (
          <p className="muted">Complete the payment to finish this booking.</p>
        )}
        <button type="button" className="btn btn-secondary btn-block" onClick={onReset}>Book another spot</button>
      </section>
    );
  }

  return (
    <section className="card">
      <div className="card-head">
        <div>
          <h2>Book parking</h2>
          <p className="muted">Choose when you need the space.</p>
        </div>
      </div>

      {parking ? (
        <div className="selected-parking">
          <div>
            <span className="meta-label">Selected parking</span>
            <strong>#{parking.parkingId} · {parking.location}</strong>
            <span className="muted">{parking.address}</span>
            <span className="muted">{money(parking.pricePerHour)}/hour · {parking.vehicleType}</span>
          </div>
          <span className={badgeClass(parking.status)}>{parking.status}</span>
        </div>
      ) : (
        <div className="empty">
          <p>No parking space selected yet.</p>
          <button type="button" className="btn btn-secondary btn-sm" onClick={onBrowse}>Browse parking spaces</button>
        </div>
      )}

      {parking && (
        <form onSubmit={submit} noValidate>
          <div className="form-row">
            <Field label="Start date & time" id="bk-start" type="datetime-local" value={form.startTime} onChange={change('startTime')} error={errors.startTime} />
            <Field label="End date & time" id="bk-end" type="datetime-local" value={form.endTime} onChange={change('endTime')} error={errors.endTime} />
          </div>
          <button className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Creating booking…' : 'Create Booking'}</button>
          <button type="button" className="btn btn-link" onClick={onBrowse}>Choose a different parking space</button>
        </form>
      )}
    </section>
  );
}
