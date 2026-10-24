import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { Music, BookOpen, Library, BarChart3, Globe, Menu, X } from "lucide-react";

export default function Navbar({ language, setLanguage }) {
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();

  const links = [
    { to: "/", label: "Dashboard", icon: BarChart3 },
    { to: "/topics", label: "Explore Topics", icon: BookOpen },
    { to: "/songs", label: "Song Library", icon: Library },
  ];

  const langOptions = [
    { code: "en", label: "English" },
    { code: "hi", label: "हिंदी" },
    { code: "mr", label: "मराठी" },
  ];

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="navbar-logo">
          <Music size={24} />
          <span>LearnTune</span>
        </Link>

        <div className={`navbar-links ${mobileOpen ? "open" : ""}`}>
          {links.map(({ to, label, icon: Icon }) => (
            <Link
              key={to}
              to={to}
              className={`navbar-link ${location.pathname === to ? "active" : ""}`}
              onClick={() => setMobileOpen(false)}
            >
              <Icon size={16} />
              {label}
            </Link>
          ))}
        </div>

        <div className="navbar-actions">
          <div className="lang-selector">
            <Globe size={16} />
            <select value={language} onChange={(e) => setLanguage(e.target.value)}>
              {langOptions.map((opt) => (
                <option key={opt.code} value={opt.code}>
                  {opt.label}
                </option>
              ))}
            </select>
          </div>

          <button className="mobile-toggle" onClick={() => setMobileOpen(!mobileOpen)}>
            {mobileOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
      </div>
    </nav>
  );
}
