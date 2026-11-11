import { useEffect, useState, useCallback } from "react";
import { useNavigate, useParams } from "react-router";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import AddOfficer from "./AddOfficer";
import "../../styles/AdminDashboard.css";

function AdminDashboard() {
  const navigate = useNavigate();
  const params = useParams();
  const token = params.token || localStorage.getItem("token");

  // KPI Telemetry State
  const [loadingStats, setLoadingStats] = useState(true);
  const [stats, setStats] = useState({
    registeredOfficers: 0,
    activeInspectors: 0,
    totalCustomers: 0,
    issuedPolicies: 0,
  });

  // Officer Directory & Pagination State
  const [officers, setOfficers] = useState([]);
  const [loadingOfficers, setLoadingOfficers] = useState(true);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(5);
  const [totalPages, setTotalPages] = useState(0);
  const [totalRecords, setTotalRecords] = useState(0);
  const [searchTerm, setSearchTerm] = useState("");

  // Role Authentication Guard
  useEffect(() => {
    const urlToken = params.token;
    const memToken = localStorage.getItem("token");

    if (urlToken && urlToken !== memToken) {
      navigate("/no-access");
      return;
    }

    const getUserDetails = async () => {
      try {
        const resp = await axios.get(`${API_SERVER_URL_DEV}/api/auth/user-details`, {
          headers: { Authorization: "Bearer " + memToken },
        });
        if (resp.data.role !== "ADMIN") {
          navigate("/no-access");
        }
      } catch (err) {
        navigate("/no-access");
      }
    };
    getUserDetails();
  }, [params.token, navigate]);

  // 1. Fetch Live KPI Telemetry from @GetMapping("/api/admin/stats")
  const fetchStats = useCallback(async () => {
    setLoadingStats(true);
    try {
      const resp = await axios.get(`${API_SERVER_URL_DEV}/api/admin/stats`, {
        headers: { Authorization: "Bearer " + token },
      });
      if (resp.data) {
        setStats({
          registeredOfficers: resp.data.registeredOfficers || 0,
          activeInspectors: resp.data.activeInspectors || 0,
          totalCustomers: resp.data.totalCustomers || 0,
          issuedPolicies: resp.data.issuedPolicies || 0,
        });
      }
    } catch (err) {
      console.error("Failed to load admin telemetry stats:", err);
    } finally {
      setLoadingStats(false);
    }
  }, [token]);

  // 2. Fetch Paginated Officers
  const fetchOfficers = useCallback(async () => {
    setLoadingOfficers(true);
    try {
      const resp = await axios.get(
        `${API_SERVER_URL_DEV}/api/officer/paginated?page=${page}&size=${pageSize}`,
        {
          headers: { Authorization: "Bearer " + token },
        }
      );
      if (resp.data) {
        setOfficers(resp.data.list || []);
        if (resp.data.paginationInfo) {
          setTotalPages(resp.data.paginationInfo.totalPages || 0);
          setTotalRecords(resp.data.paginationInfo.totalRecords || 0);
        }
      } else {
        setOfficers([]);
      }
    } catch (err) {
      console.error("Failed to fetch paginated officers:", err);
      setOfficers([]);
    } finally {
      setLoadingOfficers(false);
    }
  }, [page, pageSize, token]);

  useEffect(() => {
    fetchStats();
    fetchOfficers();
  }, [fetchStats, fetchOfficers]);

  const logout = () => {
    localStorage.clear();
    navigate("/");
  };

  // Called when AddOfficer successfully creates a record
  const handleOfficerAdded = () => {
    fetchStats();
    fetchOfficers();
  };

  const filteredOfficers = officers.filter((off) => {
    const term = searchTerm.toLowerCase().trim();
    if (!term) return true;
    return (
      off.name?.toLowerCase().includes(term) ||
      off.username?.toLowerCase().includes(term) ||
      String(off.id).includes(term)
    );
  });

  return (
    <div className="admin-shell">
      {/* Top Navigation Bar */}
      <nav className="navbar admin-navbar px-3 px-md-4">
        <div className="container-fluid px-0 d-flex justify-content-between align-items-center">
          <div className="d-flex align-items-center gap-2">
            <span className="admin-brand">Automobile Insurance</span>
            <span className="admin-role-badge">ADMIN</span>
          </div>

          <div className="d-flex align-items-center gap-3">
            <span className="text-secondary small d-none d-sm-inline">
              System Administrator
            </span>
            <button className="btn btn-admin-logout btn-sm" onClick={logout}>
              Logout
            </button>
          </div>
        </div>
      </nav>

      {/* Main Container */}
      <main className="container-fluid px-3 px-md-4 py-4 flex-grow-1">
        {/* Title Header */}
        <div className="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-4">
          <div>
            <h1 className="h4 fw-bold text-dark mb-1">Admin Dashboard</h1>
            <p className="text-muted small mb-0">
              Platform performance overview and officer management
            </p>
          </div>
          <button
            className="btn btn-outline-secondary btn-sm"
            onClick={() => {
              fetchStats();
              fetchOfficers();
            }}
            disabled={loadingStats || loadingOfficers}
          >
            {loadingStats || loadingOfficers ? "Refreshing..." : "↻ Refresh Metrics"}
          </button>
        </div>

        {/* 1. Live KPI Telemetry Cards */}
        <div className="row g-3 mb-4">
          <div className="col-12 col-sm-6 col-lg-3">
            <div className="admin-kpi-card border-top border-4 border-primary">
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="admin-kpi-label">Registered Officers</div>
                  <div className="admin-kpi-value text-primary">
                    {loadingStats ? "--" : stats.registeredOfficers}
                  </div>
                </div>
                <div className="kpi-icon-bubble bg-primary-subtle text-primary">👔</div>
              </div>
              <div className="small text-muted mt-2">Active underwriting personnel</div>
            </div>
          </div>

          <div className="col-12 col-sm-6 col-lg-3">
            <div className="admin-kpi-card border-top border-4 border-warning">
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="admin-kpi-label">Active Inspectors</div>
                  <div className="admin-kpi-value text-warning-emphasis">
                    {loadingStats ? "--" : stats.activeInspectors}
                  </div>
                </div>
                <div className="kpi-icon-bubble bg-warning-subtle text-warning-emphasis">🕵️</div>
              </div>
              <div className="small text-muted mt-2">Certified field surveyors</div>
            </div>
          </div>

          <div className="col-12 col-sm-6 col-lg-3">
            <div className="admin-kpi-card border-top border-4 border-info">
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="admin-kpi-label">Total Customers</div>
                  <div className="admin-kpi-value text-info-emphasis">
                    {loadingStats ? "--" : stats.totalCustomers}
                  </div>
                </div>
                <div className="kpi-icon-bubble bg-info-subtle text-info-emphasis">👥</div>
              </div>
              <div className="small text-muted mt-2">Registered vehicle owners</div>
            </div>
          </div>

          <div className="col-12 col-sm-6 col-lg-3">
            <div className="admin-kpi-card border-top border-4 border-success">
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="admin-kpi-label">Issued Policies</div>
                  <div className="admin-kpi-value text-success">
                    {loadingStats ? "--" : stats.issuedPolicies}
                  </div>
                </div>
                <div className="kpi-icon-bubble bg-success-subtle text-success">🛡️</div>
              </div>
              <div className="small text-muted mt-2">Active coverage contracts</div>
            </div>
          </div>
        </div>

        {/* 2. Action Area: Add Officer */}
        <div className="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
          <div>
            <h5 className="fw-bold text-dark mb-0">Underwriting Officer Directory</h5>
            <span className="text-muted small">
              Manage system authorization and officer onboarding
            </span>
          </div>
          <AddOfficer onOfficerAdded={handleOfficerAdded} />
        </div>

        {/* 3. Paginated Officer Directory Container */}
        <div className="card border-0 shadow-sm overflow-hidden mb-4">
          <div className="card-header bg-white py-3 d-flex justify-content-between align-items-center flex-wrap gap-2">
            <span className="fw-semibold text-secondary small">
              Enrolled Insurance Officers
            </span>
            <input
              type="text"
              className="form-control form-control-sm"
              style={{ width: "220px" }}
              placeholder="Search officer name, user..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div className="p-3 bg-light-subtle">
            {loadingOfficers ? (
              <div className="p-5 text-center text-muted">
                <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                Loading registered officers...
              </div>
            ) : filteredOfficers.length === 0 ? (
              <div className="p-5 text-center text-muted bg-white rounded border">
                <div className="fs-2 mb-2">👔</div>
                <h6 className="fw-bold text-dark mb-1">No Officers Found</h6>
                <p className="small text-muted mb-0">
                  {searchTerm
                    ? "No officers match your search term."
                    : "No officers enrolled yet. Click '+ Add Officer' to register one."}
                </p>
              </div>
            ) : (
              <div className="d-flex flex-column gap-3">
                {filteredOfficers.map((off) => (
                  <div key={off.id} className="officer-entry-card bg-white border rounded p-3 shadow-sm">
                    <div className="d-flex flex-wrap justify-content-between align-items-center border-bottom pb-2 mb-2 gap-2">
                      <div className="d-flex align-items-center gap-2">
                        <span className="officer-badge">ID #{off.id}</span>
                        <span className="fw-bold text-dark fs-6">{off.name}</span>
                        <span className="badge bg-primary-subtle text-primary border border-primary-subtle">
                          Underwriting Officer
                        </span>
                      </div>
                      <div className="font-monospace text-secondary small">
                        Username: <strong className="text-dark">{off.username}</strong>
                      </div>
                    </div>

                    <div className="row g-3 align-items-center small">
                      <div className="col-12 col-md-4">
                        <span className="text-muted d-block"> Full Name</span>
                        <strong className="text-dark">{off.name}</strong>
                      </div>
                      <div className="col-12 col-md-4">
                        <span className="text-muted d-block">Account Role</span>
                        <span className="text-secondary font-monospace fw-semibold">OFFICER</span>
                      </div>
                      <div className="col-12 col-md-4 text-md-end">
                        <span className="text-muted d-block">System Authority</span>
                        <span className="badge bg-success-subtle text-success border border-success-subtle">
                          ✓ Authorized Underwriter
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Pagination Strip (‹ Prev, Numbers, Next ›) */}
          <div className="d-flex justify-content-between align-items-center flex-wrap px-4 py-3 border-top bg-white">
            <div className="text-muted small">
              Showing <strong>{totalRecords === 0 ? 0 : page * pageSize + 1}</strong> to{" "}
              <strong>{Math.min((page + 1) * pageSize, totalRecords)}</strong> of{" "}
              <strong>{totalRecords}</strong> officers
            </div>

            <div className="d-flex align-items-center gap-2">
              <select
                className="form-select form-select-sm"
                style={{ width: "80px" }}
                value={pageSize}
                onChange={(e) => {
                  setPageSize(Number(e.target.value));
                  setPage(0);
                }}
              >
                <option value={3}>3</option>
                <option value={5}>5</option>
                <option value={10}>10</option>
              </select>

              <div className="btn-group btn-group-sm">
                <button
                  className="btn btn-outline-secondary"
                  disabled={page === 0 || loadingOfficers}
                  onClick={() => setPage((p) => Math.max(p - 1, 0))}
                >
                  ‹ Prev
                </button>
                {Array.from({ length: totalPages || 0 }, (_, idx) => (
                  <button
                    key={idx}
                    className={`btn ${page === idx ? "btn-primary" : "btn-outline-secondary"}`}
                    disabled={loadingOfficers}
                    onClick={() => setPage(idx)}
                  >
                    {idx + 1}
                  </button>
                ))}
                <button
                  className="btn btn-outline-secondary"
                  disabled={page >= (totalPages || 1) - 1 || loadingOfficers}
                  onClick={() => setPage((p) => p + 1)}
                >
                  Next ›
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

export default AdminDashboard;