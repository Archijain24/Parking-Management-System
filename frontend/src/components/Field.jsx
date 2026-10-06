// A label + input (or select) + inline error message.
export default function Field({ label, id, error, as = 'input', children, ...props }) {
  const Tag = as;
  return (
    <div className={`field${error ? ' has-error' : ''}`}>
      <label htmlFor={id}>{label}</label>
      <Tag id={id} aria-invalid={error ? 'true' : 'false'} aria-describedby={error ? `${id}-error` : undefined} {...props}>
        {children}
      </Tag>
      {error && <p className="field-error" id={`${id}-error`}>{error}</p>}
    </div>
  );
}
