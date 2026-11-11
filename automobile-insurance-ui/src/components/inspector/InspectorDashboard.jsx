import { useEffect, useState } from "react";
import { useNavigate, useParams, NavLink, Outlet } from "react-router";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import "../../styles/InspectorDashboard.css";

function InspectorDashboard() {
  const navigate = useNavigate();
  const params = useParams();

  const userDetailsUrl = API_SERVER_URL_DEV + "/api/auth/user-details";

  const [inspectorName, setInspectorName] = useState("");
  const [inspectorId, setInspectorId] = useState(
    localStorage.getItem("inspectorId") || null
  );

  useEffect(() => {
    const urlToken = params.token;
    const memToken = localStorage.getItem("token");

    if (!urlToken || urlToken !== memToken) {
      navigate("/no-access");
      return;
    }

    const verifyInspector = async () => {
      try {
        const resp = await axios.get(userDetailsUrl, {
          headers: { Authorization: "Bearer " + memToken },
        });

        if (resp.data.role !== "INSPECTOR") {
          navigate("/no-access");
          return;
        }

        const id = resp.data.id || resp.data.inspectorId;
        if (id) {
          setInspectorId(id);
          localStorage.setItem("inspectorId", id);
        }

        setInspectorName(
          resp.data.username ||
            localStorage.getItem("username") ||
            "Field Surveyor"
        );
      } catch (err) {
        navigate("/no-access");
      }
    };

    verifyInspector();
  }, [navigate, params.token, userDetailsUrl]);

  const logout = () => {
    localStorage.clear();
    navigate("/");
  };

  return (
    <div className="inspector-shell">
      {/* Sticky Top Header */}
      <header className="navbar inspector-topbar sticky-top">
        <div className="container-fluid px-0 d-flex justify-content-between align-items-center">
          <div className="d-flex align-items-center gap-2">
            <span className="inspector-brand">AutoSure</span>
            <span className="inspector-role-badge">FIELD INSPECTOR</span>
          </div>

          <div className="d-flex align-items-center gap-3">
            <span className="text-secondary small d-none d-sm-inline">
              Surveyor: <strong className="text-light">{inspectorName}</strong>
            </span>
            <button className="btn btn-inspector-logout" onClick={logout}>
              Sign Out
            </button>
          </div>
        </div>
      </header>

      {/* Main Structural Frame */}
      <div className="inspector-container">
        {/* Navigation Sidebar */}
        <aside className="inspector-sidebar">
          <div className="inspector-mini-card">
            <div className="inspector-avatar">
              {inspectorName ? inspectorName.charAt(0).toUpperCase() : "I"}
            </div>
            <div className="overflow-hidden">
              <div className="fw-bold text-dark text-truncate small">
                {inspectorName}
              </div>
              <div className="text-muted small" style={{ fontSize: "0.75rem" }}>
                Certified Technical Surveyor
              </div>
            </div>
          </div>

          <div className="insp-nav-section-title">Survey Operations</div>

          <NavLink
            to={`/inspector/${params.token}`}
            end
            className={({ isActive }) =>
              isActive ? "insp-sidebar-nav-link active" : "insp-sidebar-nav-link"
            }
          >
            <span className="insp-nav-icon">📊</span>
            <span>Task Overview</span>
          </NavLink>

          <NavLink
            to={`/inspector/${params.token}/pre-policy`}
            className={({ isActive }) =>
              isActive ? "insp-sidebar-nav-link active" : "insp-sidebar-nav-link"
            }
          >
            <span className="insp-nav-icon">🚗</span>
            <span>Pre-Policy Surveys</span>
          </NavLink>

          <NavLink
            to={`/inspector/${params.token}/claim-assessments`}
            className={({ isActive }) =>
              isActive ? "insp-sidebar-nav-link active" : "insp-sidebar-nav-link"
            }
          >
            <span className="insp-nav-icon">🛠️</span>
            <span>Claim Assessments</span>
          </NavLink>
        </aside>

        {/* Dynamic Workspace Canvas (Outlet handles component contents) */}
        <main className="inspector-workspace">
          <Outlet context={{ inspectorId }} />
        </main>
      </div>
    </div>
  );
}

export default InspectorDashboard;