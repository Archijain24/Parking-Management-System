import { useState } from 'react';
import Field from './Field';
import { firstError, compact } from '../utils/validate';

const EMPTY = { location: '', address: '', pricePerHour: '', vehicleType: 'CAR', status: 'AVAILABLE' };

// Owner only. Used for both "Add parking" and "Edit parking".
// When `parking` is given the form is filled with it and saves with "Update Parking".
// App.jsx gives this component a different `key` for each parking, so the fields reset correctly.
export default function ParkingForm({ parking, onSave, onCancel, onInvalid, busy }) {
  const editing = Boolean(parking);
  const [form, setForm] = useState(
    editing
      ? {
          location: parking.location ?? '',
          address: parking.address ?? '',
          pricePerHour: String(parking.pricePerHour ?? ''),
          vehicleType: parking.vehicleType ?? '',
          status: parking.status ?? 'AVAILABLE'
        }
      : EMPTY
  );
  const [errors, setErrors] = useState({});

  const change = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    const price = Number(form.pricePerHour);
    const found = compact({
      location: form.location.trim() ? '' : 'Location is required.',
      address: form.address.trim() ? '' : 'Address is required.',
      pricePerHour: form.pricePerHour === '' ? 'Price per hour is required.' : price > 0 ? '' : 'Price per hour must be greater than 0.',
      vehicleType: form.vehicleType.trim() ? '' : 'Vehicle type is required.',
      status: form.status ? '' : 'Status is required.'
    });
    setErrors(found);
    if (Object.keys(found).length) { onInvalid(firstError(found)); return; }
    const saved = await onSave({
      location: form.location.trim(),
      address: form.address.trim(),
      pricePerHour: price,
      vehicleType: form.vehicleType.trim(),
      status: form.status
    });
    if (saved && !editing) setForm(EMPTY);
  };

  return (
    <section className={`card${editing ? ' editing' : ''}`}>
      <div className="card-head">
        <div>
          <h2>{editing ? `Edit parking #${parking.parkingId}` : 'Add parking'}</h2>
          <p className="muted">{editing ? 'Change the details and save.' : 'List a new space for customers to book.'}</p>
        </div>
      </div>
      <form onSubmit={submit} noValidate>
        <Field label="Location" id="pk-location" placeholder="e.g. Whitefield" value={form.location} onChange={change('location')} error={errors.location} />
        <Field label="Address" id="pk-address" value={form.address} onChange={change('address')} error={errors.address} />
        <div className="form-row">
          <Field label="Price per hour (₹)" id="pk-price" type="number" min="0" step="any" value={form.pricePerHour} onChange={change('pricePerHour')} error={errors.pricePerHour} />
          <Field label="Vehicle type" id="pk-vehicle" list="vehicle-types" value={form.vehicleType} onChange={change('vehicleType')} error={errors.vehicleType} />
          <datalist id="vehicle-types"><option value="CAR" /><option value="BIKE" /></datalist>
        </div>
        <Field label="Status" id="pk-status" as="select" value={form.status} onChange={change('status')} error={errors.status}>
          <option value="AVAILABLE">AVAILABLE</option>
          <option value="UNAVAILABLE">UNAVAILABLE</option>
        </Field>
        <button className="btn btn-primary btn-block" disabled={busy}>
          {busy ? 'Saving…' : editing ? 'Update Parking' : 'Create Parking'}
        </button>
        {editing && <button type="button" className="btn btn-secondary btn-block" onClick={onCancel}>Cancel editing</button>}
      </form>
    </section>
  );
}
