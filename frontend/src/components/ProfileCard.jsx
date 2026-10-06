import { useEffect, useState } from 'react';
import Field from './Field';
import { validateName, validateEmail, validatePhone, firstError, compact } from '../utils/validate';

export default function ProfileCard({ profile, onUpdate, onDelete, onInvalid, busy }) {
  const [form, setForm] = useState(profile);
  const [errors, setErrors] = useState({});
  const [confirming, setConfirming] = useState(false);

  useEffect(() => { setForm(profile); }, [profile]);

  if (!form) return <section className="card"><p className="empty">Loading profile…</p></section>;

  const change = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const submit = (e) => {
    e.preventDefault();
    const found = compact({
      name: validateName(form.name),
      email: validateEmail(form.email),
      phone: validatePhone(form.phone)
    });
    setErrors(found);
    if (Object.keys(found).length) { onInvalid(firstError(found)); return; }
    onUpdate({ ...form, name: form.name.trim(), email: form.email.trim(), phone: String(form.phone ?? '').trim() });
  };

  return (
    <section className="card profile-card">
      <div className="profile-head">
        <div className="avatar" aria-hidden="true">{(profile.name || '?').charAt(0).toUpperCase()}</div>
        <div>
          <h2>{profile.name}</h2>
          <p className="muted">{profile.email}</p>
        </div>
      </div>

      <form onSubmit={submit} noValidate>
        <Field label="Name" id="pf-name" value={form.name || ''} onChange={change('name')} error={errors.name} />
        <Field label="Email" id="pf-email" type="email" value={form.email || ''} onChange={change('email')} error={errors.email} />
        <Field label="Phone" id="pf-phone" inputMode="numeric" maxLength={10} value={form.phone ?? ''} onChange={change('phone')} error={errors.phone} />
        <button className="btn btn-primary btn-block" disabled={busy}>Update Profile</button>
      </form>

      <div className="danger-zone">
        {confirming ? (
          <>
            <p><strong>Delete your profile?</strong> This cannot be undone and you will be logged out.</p>
            <div className="button-row">
              <button type="button" className="btn btn-danger" disabled={busy} onClick={onDelete}>Yes, delete my profile</button>
              <button type="button" className="btn btn-secondary" onClick={() => setConfirming(false)}>Cancel</button>
            </div>
          </>
        ) : (
          <button type="button" className="btn btn-outline-danger" onClick={() => setConfirming(true)}>Delete Profile</button>
        )}
      </div>
    </section>
  );
}
