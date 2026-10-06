import { useState } from 'react';
import Field from './Field';
import { validateEmail, firstError, compact } from '../utils/validate';

export default function LoginForm({ onLogin, onInvalid, busy }) {
  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});

  const change = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const submit = (e) => {
    e.preventDefault();
    const found = compact({
      email: validateEmail(form.email),
      password: form.password ? '' : 'Password is required.'
    });
    setErrors(found);
    if (Object.keys(found).length) { onInvalid(firstError(found)); return; }
    onLogin({ email: form.email.trim(), password: form.password });
  };

  return (
    <form onSubmit={submit} noValidate>
      <Field label="Email" id="login-email" type="email" autoComplete="email" placeholder="you@example.com"
        value={form.email} onChange={change('email')} error={errors.email} />
      <Field label="Password" id="login-password" type="password" autoComplete="current-password"
        value={form.password} onChange={change('password')} error={errors.password} />
      <button className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Logging in…' : 'Login'}</button>
    </form>
  );
}
