import { useCallback, useEffect, useState } from 'react';
import * as backend from './services/api';
import Alert from './components/Alert';
import Navbar from './components/Navbar';
import LoginForm from './components/LoginForm';
import RegisterForm from './components/RegisterForm';
import ParkingList from './components/ParkingList';
import ParkingForm from './components/ParkingForm';
import BookingForm from './components/BookingForm';
import PaymentForm from './components/PaymentForm';
import MyBookings from './components/MyBookings';
import ProfileCard from './components/ProfileCard';

const NO_FILTERS = { location: '', vehicleType: '', status: '' };

function readStoredUser() {
  try { return JSON.parse(localStorage.getItem('user') || 'null'); } catch { return null; }
}

function App() {
  const [user, setUser] = useState(readStoredUser);
  const [mode, setMode] = useState('login');           // login | register
  const [tab, setTab] = useState('parking');           // parking | booking | profile
  const [alert, setAlert] = useState(null);            // { type, message }
  const [busy, setBusy] = useState(false);

  // parking
  const [parking, setParking] = useState([]);
  const [loadingParking, setLoadingParking] = useState(false);
  const [filters, setFilters] = useState(NO_FILTERS);  // current search filters
  const [editingParking, setEditingParking] = useState(null); // owner: parking being edited
  const [parkingNames, setParkingNames] = useState({}); // { parkingId: location } used by My Bookings

  // customer
  const [selectedParking, setSelectedParking] = useState(null);
  const [booking, setBooking] = useState(null);        // last created booking
  const [paymentPrefill, setPaymentPrefill] = useState(null);
  const [myBookings, setMyBookings] = useState([]);
  const [loadingBookings, setLoadingBookings] = useState(false);

  const [profile, setProfile] = useState(null);

  const notify = (type, message) => setAlert({ type, message });
  const closeAlert = useCallback(() => setAlert(null), []);
  const invalid = (message) => notify('error', message);

  // Runs an async action while disabling the buttons.
  const run = async (action) => {
    setBusy(true);
    try { return await action(); } finally { setBusy(false); }
  };

  // ---------- data loading ----------
  // Loads all parking, or the search results when filters are given.
  const loadParking = async (activeFilters = filters) => {
    setLoadingParking(true);
    try {
      const list = await backend.searchParking(activeFilters);
      setParking(list);
      const names = {};
      list.forEach((p) => { names[p.parkingId] = p.location; });
      setParkingNames((prev) => ({ ...prev, ...names }));
    } catch (e) { notify('error', e.message); }
    setLoadingParking(false);
  };

  const loadProfile = async () => {
    if (!user) return;
    try { setProfile(await backend.getUser(user.userId)); } catch (e) { notify('error', e.message); }
  };

  const loadBookings = async () => {
    setLoadingBookings(true);
    try {
      const list = await backend.getMyBookings();
      setMyBookings([...list].sort((a, b) => b.bookingId - a.bookingId)); // newest first

      // Bookings only contain a parkingId, so look up names we don't know yet.
      const missing = [...new Set(list.map((b) => b.parkingId))].filter((id) => id && !parkingNames[id]);
      const found = {};
      await Promise.all(missing.map(async (id) => {
        try { found[id] = (await backend.getParkingById(id)).location; } catch { /* show "Parking #id" instead */ }
      }));
      if (Object.keys(found).length) setParkingNames((prev) => ({ ...prev, ...found }));
    } catch (e) { notify('error', e.message); }
    setLoadingBookings(false);
  };

  useEffect(() => { if (user) { loadParking(); loadProfile(); } }, [user]);

  // Customers: load their bookings when they open the Booking tab
  useEffect(() => {
    if (user && user.role !== 'OWNER' && tab === 'booking') loadBookings();
  }, [user, tab]);

  // ---------- auth ----------
  const doRegister = (payload) => run(async () => {
    try {
      await backend.register(payload);
      notify('success', 'Registration successful. Please log in.');
      setMode('login');
    } catch (e) { notify('error', e.message); }
  });

  const doLogin = (credentials) => run(async () => {
    try {
      const data = await backend.login(credentials);
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(data));
      setUser(data);
      setTab('parking');
      notify('success', 'Login successful.');
    } catch (e) {
      const wrongLogin = (e.status === 401 || e.status === 403) && !e.fromServer;
      notify('error', wrongLogin ? 'Invalid email or password.' : e.message);
    }
  });

  const logout = () => {
    localStorage.clear();
    setUser(null); setProfile(null); setParking([]); setFilters(NO_FILTERS);
    setEditingParking(null); setSelectedParking(null); setBooking(null);
    setPaymentPrefill(null); setMyBookings([]); setParkingNames({});
    setMode('login'); setTab('parking');
  };

  // ---------- parking search ----------
  const searchParking = (newFilters) => { setFilters(newFilters); loadParking(newFilters); };
  const clearSearch = () => { setFilters(NO_FILTERS); loadParking(NO_FILTERS); };
  const filtersActive = Object.values(filters).some((v) => String(v).trim() !== '');

  // ---------- parking (owner): add, edit, delete ----------
  const saveParking = (payload) => run(async () => {
    try {
      if (editingParking) {
        await backend.updateParking(editingParking.parkingId, payload);
        notify('success', 'Parking updated successfully.');
        setEditingParking(null);
      } else {
        await backend.createParking(payload);
        notify('success', 'Parking created successfully.');
      }
      loadParking();
      return true;
    } catch (e) { notify('error', e.message); return false; }
  });

  const startEditing = (p) => {
    setEditingParking(p);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const removeParking = (id) => run(async () => {
    try {
      await backend.deleteParking(id);
      notify('success', 'Parking deleted successfully.');
      if (editingParking && editingParking.parkingId === id) setEditingParking(null);
      loadParking();
      return true;
    } catch (e) { notify('error', e.message); return false; }
  });

  // ---------- booking (customer) ----------
  const selectParking = (p) => {
    setSelectedParking(p);
    setBooking(null);
    setTab('booking');
  };

  const createBooking = ({ startTime, endTime }) => run(async () => {
    try {
      const data = await backend.createBooking({ parkingId: Number(selectedParking.parkingId), startTime, endTime });
      setBooking({ ...data, parking: selectedParking, startTime, endTime });
      setPaymentPrefill({ bookingId: String(data.bookingId), amount: String(data.totalAmount) });
      notify('success', 'Booking created successfully. Complete the payment below.');
      loadBookings();
    } catch (e) { notify('error', e.message); }
  });

  // Re-reads a booking from the backend so the confirmation card shows its real status.
  const syncBooking = async (id) => {
    try {
      const fresh = await backend.getBookingById(id);
      setBooking((b) => (b && String(b.bookingId) === String(id) ? { ...b, status: fresh.status ?? fresh.bookingStatus ?? b.status } : b));
    } catch { /* not critical */ }
  };

  const getBookingDetails = async (id) => {
    try { return await backend.getBookingById(id); } catch (e) { notify('error', e.message); return null; }
  };

  const cancelMyBooking = (id) => run(async () => {
    try {
      await backend.cancelBooking(id);
      notify('success', 'Booking cancelled successfully.');
      await loadBookings();
      syncBooking(id);
      return true;
    } catch (e) { notify('error', e.message); return false; }
  });

  // "Pay Now" on a pending booking: fill the payment form and scroll up to it
  const payForBooking = (b) => {
    setPaymentPrefill({ bookingId: String(b.bookingId), amount: String(b.totalAmount) });
    window.scrollTo({ top: 0, behavior: 'smooth' });
    notify('info', `Payment details filled in for booking #${b.bookingId}.`);
  };

  // ---------- payment (customer) ----------
  const makePayment = (payload) => run(async () => {
    try {
      const data = await backend.makePayment(payload);
      if (data && /fail/i.test(String(data.paymentStatus))) {
        notify('error', 'Payment failed. Please try again.');
        return null;
      }
      notify('success', 'Payment successful.');
      // The backend confirms the booking after payment – refresh to show the new status.
      syncBooking(payload.bookingId);
      loadBookings();
      return data || {};
    } catch (e) { notify('error', e.message); return null; }
  });

  const getPaymentDetails = async (id) => {
    try { return await backend.getPaymentById(id); } catch (e) { notify('error', e.message); return null; }
  };

  // ---------- profile ----------
  const updateProfile = (payload) => run(async () => {
    try {
      const data = await backend.updateUser(user.userId, payload);
      setProfile(data);
      notify('success', 'Profile updated successfully.');
    } catch (e) { notify('error', e.message); }
  });

  const deleteProfile = () => run(async () => {
    try {
      await backend.deleteUser(user.userId);
      logout();
      notify('success', 'Your profile has been deleted.');
    } catch (e) { notify('error', e.message); }
  });

  const toast = <Alert type={alert?.type} message={alert?.message} onClose={closeAlert} />;

  // ---------- login / register screen ----------
  if (!user) {
    return (
      <div className="auth-page">
        <div className="toast-area">{toast}</div>

        <aside className="auth-hero">
          <div className="brand brand-light">
            <span className="brand-mark" aria-hidden="true">P</span>
            <span className="brand-name">Parkezz</span>
          </div>
          <div className="hero-copy">
            <h1>Find your spot. Park with ease.</h1>
            <p>Browse parking spaces, book the time you need and pay in a few steps. Space owners can list their parking for customers to book.</p>
          </div>
          <div className="bays" aria-hidden="true">
            <div className="bay"><span className="car" /></div>
            <div className="bay"><span className="car" /></div>
            <div className="bay free"><b>P</b></div>
            <div className="bay"><span className="car" /></div>
            <div className="bay"><span className="car" /></div>
          </div>
        </aside>

        <main className="auth-panel">
          <div className="auth-card">
            <h2>{mode === 'login' ? 'Welcome back' : 'Create your account'}</h2>
            <p className="muted">{mode === 'login' ? 'Log in to find and book parking.' : 'It only takes a minute.'}</p>

            <div className="segmented" role="tablist" aria-label="Login or register">
              <button type="button" role="tab" aria-selected={mode === 'login'} className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>Login</button>
              <button type="button" role="tab" aria-selected={mode === 'register'} className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>Register</button>
            </div>

            {mode === 'login'
              ? <LoginForm onLogin={doLogin} onInvalid={invalid} busy={busy} />
              : <RegisterForm onRegister={doRegister} onInvalid={invalid} busy={busy} />}
          </div>
        </main>
      </div>
    );
  }

  // ---------- dashboard ----------
  const isOwner = user.role === 'OWNER';
  const tabs = [
    { id: 'parking', label: 'Parking' },
    ...(isOwner ? [] : [{ id: 'booking', label: 'Booking' }]),
    { id: 'profile', label: 'Profile' }
  ];

  return (
    <div className="app">
      <Navbar user={user} tabs={tabs} active={tab} onChange={setTab} onLogout={logout} />
      <div className="toast-area">{toast}</div>

      <main className="container">
        <div className="page-head">
          <h1>Welcome, {user.name}</h1>
          <p className="muted">Logged in as {user.role}</p>
        </div>

        {tab === 'parking' && (
          <div className={isOwner ? 'split' : ''}>
            <ParkingList parking={parking} loading={loadingParking} filtersActive={filtersActive} busy={busy}
              canBook={!isOwner} isOwner={isOwner} userId={user.userId}
              selectedId={isOwner ? editingParking?.parkingId : selectedParking?.parkingId}
              onBook={selectParking} onEdit={startEditing} onDelete={removeParking}
              onRefresh={() => loadParking()} onSearch={searchParking} onClear={clearSearch} />
            {isOwner && (
              <ParkingForm key={editingParking ? editingParking.parkingId : 'new'} parking={editingParking}
                onSave={saveParking} onCancel={() => setEditingParking(null)} onInvalid={invalid} busy={busy} />
            )}
          </div>
        )}

        {tab === 'booking' && !isOwner && (
          <div className="stack">
            <div className="split">
              <BookingForm parking={selectedParking} booking={booking} busy={busy}
                onBook={createBooking} onInvalid={invalid}
                onReset={() => setBooking(null)} onBrowse={() => setTab('parking')} />
              <PaymentForm prefill={paymentPrefill} onPay={makePayment} onGetPayment={getPaymentDetails} onInvalid={invalid} busy={busy} />
            </div>
            <MyBookings bookings={myBookings} loading={loadingBookings} parkingNames={parkingNames} busy={busy}
              onRefresh={loadBookings} onPay={payForBooking} onCancel={cancelMyBooking} onGetDetails={getBookingDetails} />
          </div>
        )}

        {tab === 'profile' && (
          <ProfileCard profile={profile} busy={busy} onUpdate={updateProfile} onDelete={deleteProfile} onInvalid={invalid} />
        )}
      </main>
    </div>
  );
}

export default App;
