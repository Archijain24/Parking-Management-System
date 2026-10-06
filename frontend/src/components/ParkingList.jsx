import ParkingCard from './ParkingCard';
import ParkingSearch from './ParkingSearch';

export default function ParkingList({
  parking, loading, filtersActive, busy,
  canBook, isOwner, userId, selectedId,
  onBook, onEdit, onDelete, onRefresh, onSearch, onClear
}) {
  // Owners can edit/delete their own parking. If the backend does not send an ownerId,
  // the buttons are shown and the backend decides whether the action is allowed.
  const canManage = (p) => isOwner && (p.ownerId === undefined || String(p.ownerId) === String(userId));

  return (
    <section className="card">
      <div className="card-head">
        <div>
          <h2>Parking spaces</h2>
          <p className="muted">{parking.length} {parking.length === 1 ? 'space' : 'spaces'} {filtersActive ? 'found' : 'listed'}</p>
        </div>
        <button type="button" className="btn btn-secondary btn-sm" onClick={onRefresh} disabled={loading}>
          {loading ? 'Refreshing…' : 'Refresh'}
        </button>
      </div>

      <ParkingSearch onSearch={onSearch} onClear={onClear} busy={loading} />

      {parking.length === 0 ? (
        <p className="empty">
          {loading ? 'Loading parking spaces…' : filtersActive ? 'No parking spaces match your search.' : 'No parking spaces yet.'}
        </p>
      ) : (
        <div className="parking-grid">
          {parking.map((p) => (
            <ParkingCard key={p.parkingId} parking={p} busy={busy}
              canBook={canBook} canManage={canManage(p)}
              selected={String(selectedId) === String(p.parkingId)}
              onBook={onBook} onEdit={onEdit} onDelete={onDelete} />
          ))}
        </div>
      )}
    </section>
  );
}
