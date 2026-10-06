import { useState } from 'react';
import Field from './Field';

// Simple filter bar for the Parking page. Empty filters are ignored by searchParking().
export default function ParkingSearch({ onSearch, onClear, busy }) {
  const [filters, setFilters] = useState({ location: '', vehicleType: '', status: '' });

  const change = (key) => (e) => setFilters({ ...filters, [key]: e.target.value });

  const submit = (e) => {
    e.preventDefault();
    onSearch(filters);
  };

  const clear = () => {
    setFilters({ location: '', vehicleType: '', status: '' });
    onClear();
  };

  return (
    <form className="search-form" onSubmit={submit}>
      <Field label="Location" id="search-location" placeholder="e.g. Whitefield" value={filters.location} onChange={change('location')} />
      <Field label="Vehicle type" id="search-vehicle" list="search-vehicle-types" placeholder="Any" value={filters.vehicleType} onChange={change('vehicleType')} />
      <datalist id="search-vehicle-types"><option value="CAR" /><option value="BIKE" /></datalist>
      <Field label="Status" id="search-status" as="select" value={filters.status} onChange={change('status')}>
        <option value="">ALL</option>
        <option value="AVAILABLE">AVAILABLE</option>
        <option value="UNAVAILABLE">UNAVAILABLE</option>
      </Field>
      <div className="button-row">
        <button className="btn btn-primary" disabled={busy}>Search</button>
        <button type="button" className="btn btn-secondary" onClick={clear} disabled={busy}>Clear</button>
      </div>
    </form>
  );
}
