import { useEffect, useState } from "react";
import { useNavigate, useParams, NavLink, Outlet } from "react-router";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import "../../styles/CustomerDashboard.css";

function CustomerDashboard() {
  const navigate = useNavigate();
  const params = useParams();

  const userDetailsUrl = API_SERVER_URL_DEV + "/api/auth/user-details";
  const myPoliciesUrl = API_SERVER_URL_DEV + "/api/policy/customer/my-policies/v4";
  const myProposalsUrl = API_SERVER_URL_DEV + "/api/proposal/customer/v2";
  const myClaimsUrl = API_SERVER_URL_DEV + "/api/claim/customer/my-claims/v4";

  const [username, setUsername] = useState("");
  const [stats, setStats] = useState({
    activePolicies: 0,
    pendingProposals: 0,
    openClaims: 0,
    totalPremiumPaid: 0,
  });

  useEffect(() => {
    const urlToken = params.token;
    const memToken = localStorage.getItem("token");

    // Strictly verify token equivalence
    if (urlToken !== memToken) {
      navigate("/no-access");
      return;
    }

    const getUserDetailsAndMetrics = async () => {
      try {
        // Step 1: Verify user details & role authority
        const resp = await axios.get(userDetailsUrl, {
          headers: { Authorization: "Bearer " + memToken },
        });

        if (resp.data.role !== "CUSTOMER") {
          navigate("/no-access");
          return;
        }

        setUsername(
          resp.data.username ||
          localStorage.getItem("username") ||
          "Customer"
        );

        // Step 2: Fetch customer metrics concurrently (policies, proposals, claims)
        const [polRes, propRes, claimRes] = await Promise.allSettled([
          axios.get(myPoliciesUrl, { headers: { Authorization: "Bearer " + memToken } }),
          axios.get(myProposalsUrl, { headers: { Authorization: "Bearer " + memToken } }),
          axios.get(myClaimsUrl, { headers: { Authorization: "Bearer " + memToken } }),
        ]);

        const policyList = polRes.status === "fulfilled" ? polRes.value.data || [] : [];
        const proposalList = propRes.status === "fulfilled" ? propRes.value.data || [] : [];
        const claimList = claimRes.status === "fulfilled" ? claimRes.value.data || [] : [];

        // Sum aggregate premium paid across all issued policies
        const totalPaid = policyList.reduce(
          (acc, curr) => acc + (Number(curr.totalPremium) || 0),
          0
        );

        // Proposals waiting for survey, quote or payment
        const pendingCount = proposalList.filter((p) => p.status !== "POLICY_ISSUED").length;

        // Active claims (not yet approved or rejected)
        const openClaimCount = claimList.filter(
          (c) => c.status !== "APPROVED" && c.status !== "REJECTED"
        ).length;

        setStats({
          activePolicies: policyList.filter((p) => p.status === "ACTIVE").length || policyList.length,
          pendingProposals: pendingCount,
          openClaims: openClaimCount,
          totalPremiumPaid: totalPaid,
        });
      } catch (err) {
        navigate("/no-access");
      }
    };

    getUserDetailsAndMetrics();
  }, [navigate, params.token, userDetailsUrl, myPoliciesUrl, myProposalsUrl, myClaimsUrl]);

  const logout = () => {
    localStorage.clear();
    navigate("/");
  };

  return (
    <div className="customer-shell">
      {/* Top Header Navigation */}
      <header className="navbar customer-topbar sticky-top">
        <div className="container-fluid px-0 d-flex justify-content-between align-items-center">
          <div className="d-flex align-items-center gap-2">
            <span className="brand-text">AutoSure</span>
            <span className="portal-tag">CUSTOMER DESK</span>
          </div>

          <div className="d-flex align-items-center gap-3">
            <span className="text-secondary small d-none d-sm-inline">
              Signed in as <strong className="text-light">{username}</strong>
            </span>
            <button className="btn btn-topbar-logout" onClick={logout}>
              Logout
            </button>
          </div>
        </div>
      </header>

      {/* Main Structural Frame */}
      <div className="dashboard-container">
        {/* Navigation Sidebar */}
        <aside className="customer-sidebar">
          {/* User Profile Mini Badge */}
          <div className="user-mini-card">
            <div className="avatar-badge">
              {username ? username.charAt(0).toUpperCase() : "C"}
            </div>
            <div className="overflow-hidden">
              <div className="fw-bold text-dark text-truncate small">{username}</div>
              <div className="text-muted small" style={{ fontSize: "0.75rem" }}>
                Active Policyholder
              </div>
            </div>
          </div>

          {/* Group 1: Account Settings */}
          <div className="nav-section-title">Account Settings</div>
          <NavLink
            to={`/customer/${params.token}/profile`}
            className={({ isActive }) =>
              isActive ? "sidebar-nav-link active" : "sidebar-nav-link"
            }
          >
            <span className="nav-icon">👤</span>
            <span>My Profile</span>
          </NavLink>

          {/* Group 2: Insurance Lifecycle */}
          <div className="nav-section-title mt-3">Insurance Lifecycle</div>
          <NavLink
            to={`/customer/${params.token}/proposals`}
            className={({ isActive }) =>
              isActive ? "sidebar-nav-link active" : "sidebar-nav-link"
            }
          >
            <span className="nav-icon">📋</span>
            <span>My Proposals</span>
          </NavLink>

          <NavLink
            to={`/customer/${params.token}/quotes-payment`}
            className={({ isActive }) =>
              isActive ? "sidebar-nav-link active" : "sidebar-nav-link"
            }
          >
            <span className="nav-icon">💳</span>
            <span>Quotes & Pay</span>
          </NavLink>

          <NavLink
            to={`/customer/${params.token}/policies`}
            className={({ isActive }) =>
              isActive ? "sidebar-nav-link active" : "sidebar-nav-link"
            }
          >
            <span className="nav-icon">🛡️</span>
            <span>Policy Vault</span>
          </NavLink>

          {/* Group 3: Claims Desk */}
          <div className="nav-section-title mt-3">Claims & Adjudication</div>
          <NavLink
            to={`/customer/${params.token}/claims`}
            className={({ isActive }) =>
              isActive ? "sidebar-nav-link active" : "sidebar-nav-link"
            }
          >
            <span className="nav-icon">⚖️</span>
            <span>Claims Desk</span>
          </NavLink>
        </aside>

        {/* Dynamic Workspace Canvas */}
        <main className="main-workspace">
          {/* Welcome Headline */}
          <div className="mb-4">
            <h1 className="h4 fw-bold text-dark mb-1">Customer Workspace</h1>
            <p className="text-muted small mb-0">
              Track policy proposals, check surveyor-approved IDV quotes, make premium payments, and adjudicate insurance claims.
            </p>
          </div>

          {/* Officer-Styled Metric Cards (Full Coloured Border + Centered Layout) */}
          {/* Metric Cards Row with Proportional Column Space */}
          <div className="row g-3 mb-4 align-items-stretch">
            {/* Card 1: Active Policies */}
            <div className="col-12 col-sm-8 col-lg-3 col-xl">
              <div className="stat-card-widget border-card-blue h-100">
                <div className="stat-header-row">
                  <span className="stat-widget-label">Active Policies</span>
                </div>
                <div className="stat-widget-number text-primary">{stats.activePolicies}</div>
                <div className="stat-widget-subtext">Active underwritten coverage</div>
              </div>
            </div>

            {/* Card 2: Pending Proposals */}
            <div className="col-12 col-sm-6 col-lg-3 col-xl">
              <div className="stat-card-widget border-card-amber h-100">
                <div className="stat-header-row">
                  <span className="stat-widget-label">Pending Proposals</span>
                </div>
                <div className="stat-widget-number text-warning">{stats.pendingProposals}</div>
                <div className="stat-widget-subtext">Awaiting review & payment</div>
              </div>
            </div>

            {/* Card 3: Active Claims */}
            <div className="col-12 col-sm-6 col-lg-3 col-xl">
              <div className="stat-card-widget border-card-slate h-100">
                <div className="stat-header-row">
                  <span className="stat-widget-label">Active Claims</span>
                </div>
                <div className="stat-widget-number text-dark">{stats.openClaims}</div>
                <div className="stat-widget-subtext">Under survey or decision</div>
              </div>
            </div>

            {/* Card 4: Total Premium Paid (Given More Horizontal Space) */}
            <div className="col-12 col-sm-6 col-lg-3 col-xl-4">
              <div className="stat-card-widget border-card-green h-100">
                <div className="stat-header-row">
                  <span className="stat-widget-label">Total Premium Paid</span>
                </div>
                <div className="stat-widget-number stat-currency-number text-success">
                  ₹{Number(stats.totalPremiumPaid || 0).toLocaleString("en-IN", {
                    minimumFractionDigits: 1,
                    maximumFractionDigits: 2
                  })}
                </div>
                <div className="stat-widget-subtext">Cumulative payments disbursed</div>
              </div>
            </div>
          </div>

          <hr className="my-4 text-secondary" style={{ opacity: 0.15 }} />

          {/* Dynamic Nested Router Outlet */}
          <div className="dynamic-outlet-container">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}

export default CustomerDashboard;