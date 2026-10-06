import { useState } from 'react';
import { badgeClass, money } from '../utils/format';

// Customers get a "Book Parking" button. Owners get Edit / Delete (with a confirmation step).
export default function ParkingCard({ parking, canBook, canManage, selected, busy, onBook, onEdit, onDelete }) {
  const [confirmingDelete, setConfirmingDelete] = useState(false);
  const available = String(parking.status).toUpperCase() === 'AVAILABLE';

  const confirmDelete = async () => {
    const ok = await onDelete(parking.parkingId);
    if (!ok) setConfirmingDelete(false);
  };

  return (
    <article className={`parking-card${selected ? ' is-selected' : ''}`}>
      <div className="parking-top">
        <span className="parking-id">Parking #{parking.parkingId}</span>
        <span className={badgeClass(parking.status)}>{parking.status}</span>
      </div>

      <h3>{parking.location}</h3>
      <p className="parking-address">{parking.address}</p>

      <div className="parking-meta">
        <div>
          <span className="meta-label">Price</span>
          <span className="parking-price">{money(parking.pricePerHour)}<small>/hour</small></span>
        </div>
        <div>
          <span className="meta-label">Vehicle</span>
          <span className="meta-value">{parking.vehicleType}</span>
        </div>
      </div>

      {canBook && (
        <button type="button" className="btn btn-primary btn-block" disabled={!available} onClick={() => onBook(parking)}>
          {!available ? 'Not available' : selected ? 'Selected – continue booking' : 'Book Parking'}
        </button>
      )}

      {canManage && !confirmingDelete && (
        <div className="button-row card-actions">
          <button type="button" className="btn btn-secondary btn-sm" onClick={() => onEdit(parking)}>Edit</button>
          <button type="button" className="btn btn-outline-danger btn-sm" onClick={() => setConfirmingDelete(true)}>Delete</button>
        </div>
      )}

      {canManage && confirmingDelete && (
        <div className="confirm-box">
          <p>Are you sure you want to delete this parking space?</p>
          <div className="button-row">
            <button type="button" className="btn btn-danger btn-sm" disabled={busy} onClick={confirmDelete}>Yes, delete</button>
            <button type="button" className="btn btn-secondary btn-sm" onClick={() => setConfirmingDelete(false)}>Cancel</button>
          </div>
        </div>
      )}
    </article>
  );
}
