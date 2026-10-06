import { useState } from 'react';
import { badgeClass, dateTime, money } from '../utils/format';

// Cancel is only offered while the booking is still active.
const CANCELLABLE = ['PENDING', 'CONFIRMED'];

export default function MyBookings({ bookings, loading, parkingNames, busy, onRefresh, onPay, onCancel, onGetDetails }) {
  const [openId, setOpenId] = useState(null);      // booking whose details are showing
  const [details, setDetails] = useState(null);    // details loaded with GET /api/bookings/{id}
  const [confirmingId, setConfirmingId] = useState(null);

  const parkingName = (b) => parkingNames[b.parkingId] || `Parking #${b.parkingId}`;
  const statusOf = (b) => String(b.status ?? b.bookingStatus ?? '').toUpperCase();

  const toggleDetails = async (b) => {
    if (openId === b.bookingId) { setOpenId(null); return; }
    const data = await onGetDetails(b.bookingId);
    if (data) { setDetails(data); setOpenId(b.bookingId); }
  };

  const confirmCancel = async (b) => {
    const ok = await onCancel(b.bookingId);
    setConfirmingId(null);
    if (ok) setOpenId(null);
  };

  return (
    <section className="card">
      <div className="card-head">
        <div>
          <h2>My bookings</h2>
          <p className="muted">{bookings.length} {bookings.length === 1 ? 'booking' : 'bookings'}</p>
        </div>
        <button type="button" className="btn btn-secondary btn-sm" onClick={onRefresh} disabled={loading}>
          {loading ? 'Refreshing…' : 'Refresh'}
        </button>
      </div>

      {bookings.length === 0 ? (
        <p className="empty">{loading ? 'Loading your bookings…' : "You don't have any bookings yet."}</p>
      ) : (
        <div className="booking-grid">
          {bookings.map((b) => {
            const status = statusOf(b);
            return (
              <article className="booking-card" key={b.bookingId}>
                <div className="parking-top">
                  <h3>Booking #{b.bookingId}</h3>
                  <span className={badgeClass(status)}>{status || 'UNKNOWN'}</span>
                </div>

                <dl className="details compact">
                  <div><dt>Parking</dt><dd>{parkingName(b)}</dd></div>
                  <div><dt>Start</dt><dd>{dateTime(b.startTime)}</dd></div>
                  <div><dt>End</dt><dd>{dateTime(b.endTime)}</dd></div>
                  <div><dt>Amount</dt><dd className="strong">{money(b.totalAmount)}</dd></div>
                </dl>

                {openId === b.bookingId && details && (
                  <div className="details-panel">
                    <h4>Booking details</h4>
                    <dl className="details compact">
                      <div><dt>Booking ID</dt><dd>#{details.bookingId}</dd></div>
                      <div><dt>Parking</dt><dd>{parkingName(details)} (#{details.parkingId})</dd></div>
                      <div><dt>Start</dt><dd>{dateTime(details.startTime)}</dd></div>
                      <div><dt>End</dt><dd>{dateTime(details.endTime)}</dd></div>
                      <div><dt>Total amount</dt><dd className="strong">{money(details.totalAmount)}</dd></div>
                      <div><dt>Status</dt><dd><span className={badgeClass(details.status ?? details.bookingStatus)}>{details.status ?? details.bookingStatus}</span></dd></div>
                    </dl>
                  </div>
                )}

                {confirmingId === b.bookingId ? (
                  <div className="confirm-box">
                    <p>Are you sure you want to cancel this booking?</p>
                    <div className="button-row">
                      <button type="button" className="btn btn-danger btn-sm" disabled={busy} onClick={() => confirmCancel(b)}>Yes, cancel booking</button>
                      <button type="button" className="btn btn-secondary btn-sm" onClick={() => setConfirmingId(null)}>Keep booking</button>
                    </div>
                  </div>
                ) : (
                  <div className="button-row card-actions">
                    <button type="button" className="btn btn-secondary btn-sm" disabled={busy} onClick={() => toggleDetails(b)}>
                      {openId === b.bookingId ? 'Hide Details' : 'View Details'}
                    </button>
                    {status === 'PENDING' && (
                      <button type="button" className="btn btn-primary btn-sm" onClick={() => onPay(b)}>Pay Now</button>
                    )}
                    {CANCELLABLE.includes(status) && (
                      <button type="button" className="btn btn-outline-danger btn-sm" onClick={() => setConfirmingId(b.bookingId)}>Cancel Booking</button>
                    )}
                  </div>
                )}
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}
