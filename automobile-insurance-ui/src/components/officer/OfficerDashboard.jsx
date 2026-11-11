import { useEffect, useState, useCallback } from "react";
import { useNavigate, useParams, NavLink, Outlet } from "react-router";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import "../../styles/OfficerDashboard.css";

function OfficerDashboard() {
  const navigate = useNavigate();
  const params = useParams();

  const userDetailsUrl = API_SERVER_URL_DEV + "/api/auth/user-details";
  const [officerName, setOfficerName] = useState("");

  const verifyOfficer = useCallback(async () => {
    const memToken = localStorage.getItem("token");
    if (!memToken) {
      navigate("/no-access");
      return;
    }

    try {
      const userResp = await axios.get(userDetailsUrl, {
        headers: { Authorization: "Bearer " + memToken },
      });

      if (userResp.data.role !== "OFFICER") {
        navigate("/no-access");
        return;
      }

      setOfficerName(
        userResp.data.username ||
        localStorage.getItem("username") ||
        "Underwriting Officer"
      );
    } catch (err) {
      navigate("/no-access");
    }
  }, [navigate, userDetailsUrl]);

  useEffect(() => {
    const urlToken = params.token;
    const memToken = localStorage.getItem("token");

    if (urlToken !== memToken) {
      navigate("/no-access");
      return;
    }

    verifyOfficer();
  }, [navigate, params.token, verifyOfficer]);

  const logout = () => {
    localStorage.clear();
    navigate("/");
  };

  return (
    <div className="officer-shell">
      {/* Sticky Header */}
      <header className="navbar officer-topbar sticky-top">
        <div className="container-fluid px-0 d-flex justify-content-between align-items-center">
          <div className="d-flex align-items-center gap-2">
            <span className="officer-brand">AutoSure</span>
            <span className="officer-role-tag">UNDERWRITING OFFICER</span>
          </div>

          <div className="d-flex align-items-center gap-3">
            <span className="text-secondary small d-none d-sm-inline">
              Officer: <strong className="text-light">{officerName}</strong>
            </span>
            <button className="btn btn-officer-signout" onClick={logout}>
              Sign Out
            </button>
          </div>
        </div>
      </header>

      {/* Main Structural Frame */}
      <div className="officer-main-frame">
        {/* Navigation Sidebar */}
        <aside className="officer-sidebar">
          <div className="officer-mini-card">
            <div className="officer-avatar-box">
              {officerName ? officerName.charAt(0).toUpperCase() : "O"}
            </div>
            <div className="overflow-hidden">
              <div className="fw-bold text-dark text-truncate small">
                {officerName}
              </div>
              <div className="text-muted small" style={{ fontSize: "0.75rem" }}>
                Underwriting Division
              </div>
            </div>
          </div>

          <div className="officer-nav-group-label">Intelligence & Analytics</div>

          {/* Dedicated Executive Overview Link (Top) */}
          <NavLink
            to={`/officer/${params.token}/overview`}
            className={({ isActive }) =>
              isActive ? "officer-sidebar-link active" : "officer-sidebar-link"
            }
          >
            <span className="officer-sidebar-icon">📊</span>
            <span>Executive Overview</span>
          </NavLink>

          <div className="officer-nav-group-label mt-3">Underwriting Desk</div>

          <NavLink
            to={`/officer/${params.token}/proposals`}
            className={({ isActive }) =>
              isActive ? "officer-sidebar-link active" : "officer-sidebar-link"
            }
          >
            <span className="officer-sidebar-icon">📋</span>
            <span>Proposals & Quotes</span>
          </NavLink>

          <NavLink
            to={`/officer/${params.token}/claims`}
            className={({ isActive }) =>
              isActive ? "officer-sidebar-link active" : "officer-sidebar-link"
            }
          >
            <span className="officer-sidebar-icon">⚖️</span>
            <span>Claims Adjudication</span>
          </NavLink>

          <div className="officer-nav-group-label mt-3">Governance & Records</div>

          <NavLink
            to={`/officer/${params.token}/inspectors`}
            className={({ isActive }) =>
              isActive ? "officer-sidebar-link active" : "officer-sidebar-link"
            }
          >
            <span className="officer-sidebar-icon">🕵️</span>
            <span>Field Inspectors</span>
          </NavLink>

          <NavLink
            to={`/officer/${params.token}/policies`}
            className={({ isActive }) =>
              isActive ? "officer-sidebar-link active" : "officer-sidebar-link"
            }
          >
            <span className="officer-sidebar-icon">🛡️</span>
            <span>Policy Registry</span>
          </NavLink>
        </aside>

        {/* Dynamic Workspace Canvas: Loads whatever sub-route is selected */}
        <main className="officer-workspace">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default OfficerDashboard;