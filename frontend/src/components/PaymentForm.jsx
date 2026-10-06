import { useEffect, useState } from 'react';
import Field from './Field';
import { badgeClass, money } from '../utils/format';

// Simulated payment only – no real payment gateway.
export default function PaymentForm({ prefill, onPay, onGetPayment, onInvalid, busy }) {
  const [form, setForm] = useState({ bookingId: '', amount: '', paymentMethod: 'UPI' });
  const [errors, setErrors] = useState({});
  const [result, setResult] = useState(null);
  const [paymentDetails, setPaymentDetails] = useState(null);

  // When a new booking is created (or "Pay Now" is clicked), fill in its ID and amount.
  useEffect(() => {
    if (prefill) {
      setForm((f) => ({ ...f, bookingId: prefill.bookingId, amount: prefill.amount, paymentMethod: 'UPI' }));
      setResult(null);
      setPaymentDetails(null);
      setErrors({});
    }
  }, [prefill]);

  const change = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    const found = {};
    if (!form.bookingId || Number(form.bookingId) <= 0) found.bookingId = 'Booking ID is required.';
    if (form.amount === '' || Number(form.amount) <= 0) found.amount = 'Enter an amount greater than 0.';
    if (!form.paymentMethod) found.paymentMethod = 'Payment method is required.';
    setErrors(found);
    const first = Object.values(found)[0];
    if (first) { onInvalid(first); return; }

    const data = await onPay({ ...form, bookingId: Number(form.bookingId), amount: Number(form.amount) });
    if (data) {
      // Only use a payment ID if the backend actually returned one.
      const paymentId = data.paymentId ?? data.id ?? null;
      setResult({ ...form, status: data.paymentStatus, paymentId });
      setPaymentDetails(null);
    }
  };

  const viewPayment = async () => {
    const data = await onGetPayment(result.paymentId);
    if (data) setPaymentDetails(data);
  };

  if (result) {
    const hasId = result.paymentId !== null && result.paymentId !== undefined;
    return (
      <section className="card payment-success">
        <div className="success-mark" aria-hidden="true">✓</div>
        <h2>Payment Successful</h2>
        <dl className="details">
          {hasId && <div><dt>Payment ID</dt><dd>#{result.paymentId}</dd></div>}
          <div><dt>Booking</dt><dd>#{result.bookingId}</dd></div>
          <div><dt>Amount paid</dt><dd className="strong">{money(result.amount)}</dd></div>
          <div><dt>Payment method</dt><dd>{result.paymentMethod.replace('_', ' ')}</dd></div>
          {result.status && <div><dt>Status</dt><dd><span className={badgeClass(result.status)}>{result.status}</span></dd></div>}
        </dl>

        {paymentDetails && (
          <div className="details-panel">
            <h4>Payment details</h4>
            <dl className="details compact">
              <div><dt>Payment ID</dt><dd>#{paymentDetails.paymentId ?? paymentDetails.id ?? result.paymentId}</dd></div>
              <div><dt>Booking ID</dt><dd>#{paymentDetails.bookingId}</dd></div>
              <div><dt>Amount</dt><dd className="strong">{money(paymentDetails.amount)}</dd></div>
              <div><dt>Method</dt><dd>{String(paymentDetails.paymentMethod ?? '').replace('_', ' ')}</dd></div>
              <div><dt>Status</dt><dd><span className={badgeClass(paymentDetails.paymentStatus ?? paymentDetails.status)}>{paymentDetails.paymentStatus ?? paymentDetails.status}</span></dd></div>
            </dl>
          </div>
        )}

        {hasId && !paymentDetails && (
          <button type="button" className="btn btn-secondary btn-block" onClick={viewPayment}>View Payment Details</button>
        )}
        <button type="button" className="btn btn-secondary btn-block" onClick={() => setResult(null)}>Make another payment</button>
      </section>
    );
  }

  return (
    <section className="card">
      <div className="card-head">
        <div>
          <h2>Payment</h2>
          <p className="muted">This is a simulated payment – no real money is charged.</p>
        </div>
      </div>
      <form onSubmit={submit} noValidate>
        <div className="form-row">
          <Field label="Booking ID" id="pay-booking" type="number" min="1" value={form.bookingId} onChange={change('bookingId')} error={errors.bookingId} />
          <Field label="Amount (₹)" id="pay-amount" type="number" min="0" step="0.01" value={form.amount} onChange={change('amount')} error={errors.amount} />
        </div>
        <Field label="Payment method" id="pay-method" as="select" value={form.paymentMethod} onChange={change('paymentMethod')} error={errors.paymentMethod}>
          <option value="UPI">UPI</option>
          <option value="CARD">CARD</option>
          <option value="NET_BANKING">NET_BANKING</option>
        </Field>
        <button className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Processing…' : form.amount ? `Pay ${money(form.amount)}` : 'Pay'}</button>
      </form>
    </section>
  );
}
