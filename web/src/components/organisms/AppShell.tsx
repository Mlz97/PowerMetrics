import { NavLink, Outlet } from 'react-router-dom';

const icon = (path: string) => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
    <path d={path} />
  </svg>
);

const TABS = [
  { to: '/', label: 'Home', icon: icon('M3 11l9-7 9 7v9a1 1 0 0 1-1 1h-5v-6h-6v6H4a1 1 0 0 1-1-1z') },
  { to: '/analyze', label: 'Analyze', icon: icon('M12 16V4m0 0l-5 5m5-5l5 5M4 20h16') },
  { to: '/history', label: 'Historic', icon: icon('M3 17l6-6 4 4 8-8M15 7h6v6') },
  { to: '/community', label: 'Community', icon: icon('M8 21h8m-4-4v4M6 4h12v5a6 6 0 0 1-12 0zM6 6H3v2a3 3 0 0 0 3 3m12-5h3v2a3 3 0 0 1-3 3') },
];

/** Tabbed app frame: the selected tab's page above the bottom navigation bar. */
export function AppShell() {
  return (
    <div className="shell">
      <main className="shell-content">
        <Outlet />
      </main>
      <nav className="tabbar" aria-label="Main">
        <div className="tabbar-inner">
          {TABS.map((tab) => (
            <NavLink key={tab.to} to={tab.to} end={tab.to === '/'} className={({ isActive }) => `tab ${isActive ? 'active' : ''}`}>
              <span className="tab-pill">{tab.icon}</span>
              {tab.label}
            </NavLink>
          ))}
        </div>
      </nav>
    </div>
  );
}
