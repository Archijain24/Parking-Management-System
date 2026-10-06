export default function Navbar({ user, tabs, active, onChange, onLogout }) {
  return (
    <header className="navbar">
      <div className="navbar-inner">
        <div className="brand">
          <span className="brand-mark" aria-hidden="true">P</span>
          <span className="brand-name">Parkezz</span>
        </div>

        <nav className="nav-links" aria-label="Main">
          {tabs.map((tab) => (
            <button key={tab.id} type="button"
              className={`nav-link${active === tab.id ? ' active' : ''}`}
              aria-current={active === tab.id ? 'page' : undefined}
              onClick={() => onChange(tab.id)}>
              {tab.label}
            </button>
          ))}
        </nav>

        <div className="nav-user">
          <span className="nav-user-name">{user.name}</span>
          <span className="role-chip">{user.role}</span>
          <button type="button" className="btn btn-ghost btn-sm" onClick={onLogout}>Logout</button>
        </div>
      </div>
    </header>
  );
}
