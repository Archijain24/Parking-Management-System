import { useState } from 'react';
import Field from './Field';
import { validateName, validateEmail, validatePassword, validatePhone, firstError, compact } from '../utils/validate';

export default function RegisterForm({ onRegister, onInvalid, busy }) {
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '', role: 'CUSTOMER' });
  const [errors, setErrors] = useState({});

  const change = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const submit = (e) => {
    e.preventDefault();
    const found = compact({
      name: validateName(form.name),
      email: validateEmail(form.email),
      password: validatePassword(form.password),
      phone: validatePhone(form.phone)
    });
    setErrors(found);
    if (Object.keys(found).length) { onInvalid(firstError(found)); return; }
    onRegister({
      name: form.name.trim(),
      email: form.email.trim(),
      password: form.password,
      phone: form.phone.trim(),
      role: form.role
    });
  };

  return (
    <form onSubmit={submit} noValidate>
      <Field label="Name" id="reg-name" autoComplete="name" value={form.name} onChange={change('name')} error={errors.name} />
      <Field label="Email" id="reg-email" type="email" autoComplete="email" placeholder="you@example.com"
        value={form.email} onChange={change('email')} error={errors.email} />
      <Field label="Password" id="reg-password" type="password" autoComplete="new-password"
        value={form.password} onChange={change('password')} error={errors.password} />
      <div className="form-row">
        <Field label="Phone" id="reg-phone" inputMode="numeric" maxLength={10} placeholder="10 digits"
          value={form.phone} onChange={change('phone')} error={errors.phone} />
        <Field label="I want to" id="reg-role" as="select" value={form.role} onChange={change('role')}>
          <option value="CUSTOMER">Book parking (Customer)</option>
          <option value="OWNER">List parking (Owner)</option>
        </Field>
      </div>
      <button className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}</button>
    </form>
  );
}
