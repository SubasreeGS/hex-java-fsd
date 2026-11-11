import { useEffect, useState, useCallback } from "react";
import { useParams } from "react-router";
import { useDispatch } from "react-redux";
import axios from "axios";
import "chart.js/auto";
import { Doughnut, Pie } from "react-chartjs-2";
import { API_SERVER_URL_DEV } from "../../../config";
import { setInspectors } from "../../store/inspectorSlice";
import "../../styles/OfficerOverviewSection.css";

function OfficerOverviewSection() {
  const params = useParams();
  const token = params.token || localStorage.getItem("token");
  const dispatch = useDispatch();

  // Endpoints - targeted to unpaginated /dropdown or paginated /all safely
  const submittedProposalsUrl = `${API_SERVER_URL_DEV}/api/proposal/status/PROPOSAL_SUBMITTED`;
  const completedInspectionsUrl = `${API_SERVER_URL_DEV}/api/proposal/status/INSPECTION_COMPLETED`;
  const claimsUrl = `${API_SERVER_URL_DEV}/api/claim/officer/all`;
  const inspectorsUrl = `${API_SERVER_URL_DEV}/api/inspector/all`;
  const policiesUrl = `${API_SERVER_URL_DEV}/api/policy/all`;
  const analyticsUrl = `${API_SERVER_URL_DEV}/api/officer/analytics`;
  const proposalsFilterUrl = `${API_SERVER_URL_DEV}/api/officer/proposals/filter`;
  const claimsFilterUrl = `${API_SERVER_URL_DEV}/api/officer/claims/filter`;

  // Zone 1 State (KPIs)
  const [loadingMetrics, setLoadingMetrics] = useState(true);
  const [stats, setStats] = useState({
    pendingProposals: 0,
    activeClaims: 0,
    issuedPolicies: 0,
    totalInspectors: 0,
  });

  // Zone 2 State (Charts)
  const [analyticsData, setAnalyticsData] = useState({
    claimsStats: [],
    proposalStats: [],
    totalSettledPayout: 0,
  });

  // Tab State: "PROPOSALS" | "CLAIMS"
  const [activeTab, setActiveTab] = useState("PROPOSALS");

  // Proposals Table & Filter State
  const [propPolicyType, setPropPolicyType] = useState("");
  const [propStatus, setPropStatus] = useState("");
  const [propFromDate, setPropFromDate] = useState("");
  const [propToDate, setPropToDate] = useState("");
  const [propPage, setPropPage] = useState(0);
  const [propPageSize, setPropPageSize] = useState(5);
  const [propData, setPropData] = useState({
    list: [],
    paginationInfo: { totalPages: 0, totalRecords: 0, numRecordsInPage: 0 },
  });
  const [propLoading, setPropLoading] = useState(false);

  // Claims Table & Filter State
  const [claimStatus, setClaimStatus] = useState("");
  const [claimFromDate, setClaimFromDate] = useState("");
  const [claimToDate, setClaimToDate] = useState("");
  const [claimPage, setClaimPage] = useState(0);
  const [claimPageSize, setClaimPageSize] = useState(5);
  const [claimData, setClaimData] = useState({
    list: [],
    paginationInfo: { totalPages: 0, totalRecords: 0, numRecordsInPage: 0 },
  });
  const [claimLoading, setClaimLoading] = useState(false);

  // 1. Fetch Executive Overview (KPIs & Chart Aggregates)
  const fetchOverviewData = useCallback(async () => {
    setLoadingMetrics(true);
    try {
      const [subPropRes, compInspRes, claimRes, inspRes, polRes, analyticsRes] =
        await Promise.allSettled([
          axios.get(submittedProposalsUrl, { headers: { Authorization: "Bearer " + token } }),
          axios.get(completedInspectionsUrl, { headers: { Authorization: "Bearer " + token } }),
          axios.get(claimsUrl, { headers: { Authorization: "Bearer " + token } }),
          axios.get(inspectorsUrl, { headers: { Authorization: "Bearer " + token } }),
          axios.get(policiesUrl, { headers: { Authorization: "Bearer " + token } }),
          axios.get(analyticsUrl, { headers: { Authorization: "Bearer " + token } }),
        ]);

      // Proposals: Safe array vs paginated object unwrapping
      const subPayload = subPropRes.status === "fulfilled" ? subPropRes.value.data : null;
      const subCount = subPayload
        ? (Array.isArray(subPayload) ? subPayload.length : subPayload.paginationInfo?.totalRecords || (subPayload.list || []).length)
        : 0;

      const compPayload = compInspRes.status === "fulfilled" ? compInspRes.value.data : null;
      const compCount = compPayload
        ? (Array.isArray(compPayload) ? compPayload.length : compPayload.paginationInfo?.totalRecords || (compPayload.list || []).length)
        : 0;

      // Claims: Safe array vs paginated object unwrapping
      const claimPayload = claimRes.status === "fulfilled" ? claimRes.value.data : null;
      const allClaimsList = claimPayload
        ? (Array.isArray(claimPayload) ? claimPayload : claimPayload.list || [])
        : [];

      // Inspectors: Extract total count whether endpoint returns array or { list, paginationInfo }
      let resolvedInspectorCount = 0;
      if (inspRes.status === "fulfilled" && inspRes.value.data) {
        const inspPayload = inspRes.value.data;
        if (Array.isArray(inspPayload)) {
          resolvedInspectorCount = inspPayload.length;
          dispatch(setInspectors(inspPayload));
        } else if (inspPayload.paginationInfo) {
          resolvedInspectorCount = inspPayload.paginationInfo.totalRecords || 0;
          dispatch(setInspectors(inspPayload));
        } else if (inspPayload.list) {
          resolvedInspectorCount = inspPayload.list.length;
          dispatch(setInspectors(inspPayload.list));
        }
      }

      // Policies
      const polPayload = polRes.status === "fulfilled" ? polRes.value.data : [];
      const issuedPoliciesCount = Array.isArray(polPayload) ? polPayload.length : (polPayload.list || []).length;

      setStats({
        pendingProposals: subCount + compCount,
        activeClaims: allClaimsList.filter((c) => c.status === "FILED" || c.status === "UNDER_ASSESSMENT").length,
        issuedPolicies: issuedPoliciesCount,
        totalInspectors: resolvedInspectorCount,
      });

      if (analyticsRes.status === "fulfilled" && analyticsRes.value.data) {
        setAnalyticsData(analyticsRes.value.data);
      }
    } catch (err) {
      console.error("Dashboard overview fetch error:", err);
    } finally {
      setLoadingMetrics(false);
    }
  }, [token, dispatch, submittedProposalsUrl, completedInspectionsUrl, claimsUrl, inspectorsUrl, policiesUrl, analyticsUrl]);

  // 2. Fetch Proposals Filtered Ledger
  const fetchProposals = useCallback(async () => {
    setPropLoading(true);
    try {
      const q = new URLSearchParams();
      if (propPolicyType) q.append("policyType", propPolicyType);
      if (propStatus) q.append("status", propStatus);
      if (propFromDate) q.append("fromDate", propFromDate);
      if (propToDate) q.append("toDate", propToDate);
      q.append("page", propPage);
      q.append("size", propPageSize);

      const res = await axios.get(`${proposalsFilterUrl}?${q.toString()}`, {
        headers: { Authorization: "Bearer " + token },
      });
      if (res.data) setPropData(res.data);
    } catch (err) {
      console.error("Proposal filter fetch error:", err);
    } finally {
      setPropLoading(false);
    }
  }, [proposalsFilterUrl, token, propPolicyType, propStatus, propFromDate, propToDate, propPage, propPageSize]);

  // 3. Fetch Claims Filtered Ledger
  const fetchClaims = useCallback(async () => {
    setClaimLoading(true);
    try {
      const q = new URLSearchParams();
      if (claimStatus) q.append("status", claimStatus);
      if (claimFromDate) q.append("fromDate", claimFromDate);
      if (claimToDate) q.append("toDate", claimToDate);
      q.append("page", claimPage);
      q.append("size", claimPageSize);

      const res = await axios.get(`${claimsFilterUrl}?${q.toString()}`, {
        headers: { Authorization: "Bearer " + token },
      });
      if (res.data) setClaimData(res.data);
    } catch (err) {
      console.error("Claim filter fetch error:", err);
    } finally {
      setClaimLoading(false);
    }
  }, [claimsFilterUrl, token, claimStatus, claimFromDate, claimToDate, claimPage, claimPageSize]);

  useEffect(() => {
    fetchOverviewData();
  }, [fetchOverviewData]);

  useEffect(() => {
    if (activeTab === "PROPOSALS") {
      fetchProposals();
    } else {
      fetchClaims();
    }
  }, [activeTab, fetchProposals, fetchClaims]);

  // Status Badge Formatting
  const formatLabel = (raw) => {
    if (!raw) return "";
    return raw
      .toLowerCase()
      .split("_")
      .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
      .join(" ");
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case "PROPOSAL_SUBMITTED":
      case "FILED":
        return <span className="badge bg-primary-subtle text-primary border border-primary-subtle">{formatLabel(status)}</span>;
      case "UNDER_REVIEW":
      case "INSPECTOR_ASSIGNED":
      case "UNDER_ASSESSMENT":
        return <span className="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle">{formatLabel(status)}</span>;
      case "INSPECTION_COMPLETED":
      case "IDV_APPROVED":
      case "QUOTE_GENERATED":
      case "APPROVED":
        return <span className="badge bg-info-subtle text-info-emphasis border border-info-subtle">{formatLabel(status)}</span>;
      case "PAYMENT_PENDING":
        return <span className="badge bg-secondary-subtle text-dark border border-secondary-subtle">{formatLabel(status)}</span>;
      case "POLICY_ISSUED":
      case "SETTLED":
        return <span className="badge bg-success-subtle text-success border border-success-subtle">{formatLabel(status)}</span>;
      case "REJECTED":
        return <span className="badge bg-danger-subtle text-danger border border-danger-subtle">{formatLabel(status)}</span>;
      default:
        return <span className="badge bg-secondary-subtle text-secondary">{formatLabel(status)}</span>;
    }
  };

  // Zone 2 Chart Configurations
  const claimsLabels = (analyticsData.claimsStats || []).map((s) => formatLabel(s.label));
  const claimsCounts = (analyticsData.claimsStats || []).map((s) => s.count);
  const claimsChartConfig = {
    labels: claimsLabels.length > 0 ? claimsLabels : ["No Claims Recorded"],
    datasets: [
      {
        data: claimsCounts.length > 0 ? claimsCounts : [1],
        backgroundColor: ["#3b82f6", "#f59e0b", "#06b6d4", "#10b981", "#ef4444"],
        borderColor: "#ffffff",
        borderWidth: 2,
        hoverOffset: 6,
      },
    ],
  };

  const proposalLabels = (analyticsData.proposalStats || []).map((s) => formatLabel(s.label));
  const proposalCounts = (analyticsData.proposalStats || []).map((s) => s.count);
  const proposalChartConfig = {
    labels: proposalLabels.length > 0 ? proposalLabels : ["No Proposals Recorded"],
    datasets: [
      {
        data: proposalCounts.length > 0 ? proposalCounts : [1],
        backgroundColor: ["#6366f1", "#8b5cf6", "#ec4899", "#22c55e"],
        borderColor: "#ffffff",
        borderWidth: 2,
        hoverOffset: 6,
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: "bottom",
        labels: {
          boxWidth: 12,
          padding: 12,
          font: { size: 11, family: "Inter, sans-serif" },
          color: "#475569",
        },
      },
    },
  };

  return (
    <div className="officer-overview-container">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-4">
        <div>
          <h1 className="h4 fw-bold text-dark mb-1">Executive Overview & Operational Pulse</h1>
          <p className="text-muted small mb-0">Underwriting pipeline, claims adjudication, and surveyor management.</p>
        </div>
        <button
          className="btn btn-outline-secondary btn-sm px-3"
          onClick={() => {
            fetchOverviewData();
            if (activeTab === "PROPOSALS") fetchProposals();
            else fetchClaims();
          }}
          disabled={loadingMetrics || propLoading || claimLoading}
        >
          {loadingMetrics || propLoading || claimLoading ? "Refreshing..." : "↻ Refresh Metrics"}
        </button>
      </div>

      {/* ZONE 1: Executive KPI Telemetry Cards */}
      <div className="row g-3 mb-4">
        <div className="col-12 col-sm-6 col-lg-3">
          <div className="officer-stat-card border-top border-4 border-primary">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="officer-stat-label">Actionable Proposals</div>
                <div className="officer-stat-number text-primary">
                  {loadingMetrics ? "--" : stats.pendingProposals}
                </div>
              </div>
              <div className="stat-icon-bubble bg-primary-subtle text-primary">📋</div>
            </div>
            <div className="stat-card-footnote text-muted mt-2">New submissions & survey reviews</div>
          </div>
        </div>

        <div className="col-12 col-sm-6 col-lg-3">
          <div className="officer-stat-card border-top border-4 border-warning">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="officer-stat-label">Claims In Review</div>
                <div className="officer-stat-number text-warning">
                  {loadingMetrics ? "--" : stats.activeClaims}
                </div>
              </div>
              <div className="stat-icon-bubble bg-warning-subtle text-warning">⚖️</div>
            </div>
            <div className="stat-card-footnote text-muted mt-2">Pending survey or decision</div>
          </div>
        </div>

        <div className="col-12 col-sm-6 col-lg-3">
          <div className="officer-stat-card border-top border-4 border-dark">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="officer-stat-label">Field Inspectors</div>
                <div className="officer-stat-number text-dark">
                  {loadingMetrics ? "--" : stats.totalInspectors}
                </div>
              </div>
              <div className="stat-icon-bubble bg-dark-subtle text-dark">🕵️</div>
            </div>
            <div className="stat-card-footnote text-muted mt-2">Active licensed surveyors</div>
          </div>
        </div>

        <div className="col-12 col-sm-6 col-lg-3">
          <div className="officer-stat-card border-top border-4 border-success">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <div className="officer-stat-label">Issued Policies</div>
                <div className="officer-stat-number text-success">
                  {loadingMetrics ? "--" : stats.issuedPolicies}
                </div>
              </div>
              <div className="stat-icon-bubble bg-success-subtle text-success">🛡️</div>
            </div>
            <div className="stat-card-footnote text-muted mt-2">Active underwritten coverage</div>
          </div>
        </div>
      </div>

      {/* ZONE 2: Visual Charts */}
      <div className="row g-4 mb-4">
        <div className="col-12 col-lg-6">
          <div className="analytics-chart-card">
            <div className="chart-card-header d-flex justify-content-between align-items-center">
              <div>
                <h6 className="fw-bold text-dark mb-0">Claims Ratio & Disbursed Payout</h6>
                <span className="text-muted small">Adjudication & settlement breakdown</span>
              </div>
              <span className="badge bg-light text-dark border">
                ₹{(analyticsData.totalSettledPayout || 0).toLocaleString("en-IN")} Disbursed
              </span>
            </div>
            <div className="chart-canvas-wrapper">
              <Doughnut data={claimsChartConfig} options={chartOptions} />
            </div>
          </div>
        </div>

        <div className="col-12 col-lg-6">
          <div className="analytics-chart-card">
            <div className="chart-card-header d-flex justify-content-between align-items-center">
              <div>
                <h6 className="fw-bold text-dark mb-0">Underwriting Pipeline</h6>
                <span className="text-muted small">Proposal status progression</span>
              </div>
              <span className="badge bg-light text-dark border">
                Total Proposals: {proposalCounts.reduce((a, b) => a + b, 0)}
              </span>
            </div>
            <div className="chart-canvas-wrapper">
              <Pie data={proposalChartConfig} options={chartOptions} />
            </div>
          </div>
        </div>
      </div>

      {/* ZONE 3 & 4: Tabbed Ledgers */}
      <div className="ledger-card mb-4">
        {/* Navigation Tabs */}
        <div className="border-bottom px-3 pt-3 bg-white">
          <ul className="nav nav-tabs card-header-tabs border-0">
            <li className="nav-item">
              <button
                className={`nav-link fw-semibold px-4 ${activeTab === "PROPOSALS" ? "active text-primary" : "text-secondary"}`}
                onClick={() => setActiveTab("PROPOSALS")}
              >
                📋 Proposals Pipeline
              </button>
            </li>
            <li className="nav-item">
              <button
                className={`nav-link fw-semibold px-4 ${activeTab === "CLAIMS" ? "active text-primary" : "text-secondary"}`}
                onClick={() => setActiveTab("CLAIMS")}
              >
                ⚖️ Claims Adjudication
              </button>
            </li>
          </ul>
        </div>

        {/* TAB 1: Proposals View */}
        {activeTab === "PROPOSALS" && (
          <div>
            <div className="p-3 bg-light border-bottom">
              <div className="d-flex flex-wrap align-items-end gap-2">
                {/* Policy Type */}
                <div style={{ flex: "1 1 160px", minWidth: "150px" }}>
                  <label className="form-label small fw-semibold text-secondary mb-1">Policy Type</label>
                  <select
                    className="form-select form-select-sm"
                    value={propPolicyType}
                    onChange={(e) => {
                      setPropPolicyType(e.target.value);
                      setPropPage(0);
                    }}
                  >
                    <option value="">All Types</option>
                    <option value="COMPREHENSIVE">Comprehensive</option>
                    <option value="THIRD_PARTY">Third Party</option>
                  </select>
                </div>

                {/* Proposal Status */}
                <div style={{ flex: "2 1 200px", minWidth: "190px" }}>
                  <label className="form-label small fw-semibold text-secondary mb-1">Proposal Status</label>
                  <select
                    className="form-select form-select-sm"
                    value={propStatus}
                    onChange={(e) => {
                      setPropStatus(e.target.value);
                      setPropPage(0);
                    }}
                  >
                    <option value="">All Statuses</option>
                    <option value="PROPOSAL_SUBMITTED">Proposal Submitted</option>
                    <option value="UNDER_REVIEW">Under Review</option>
                    <option value="INSPECTOR_ASSIGNED">Inspector Assigned</option>
                    <option value="INSPECTION_COMPLETED">Inspection Completed</option>
                    <option value="IDV_APPROVED">IDV Approved</option>
                    <option value="QUOTE_GENERATED">Quote Generated</option>
                    <option value="PAYMENT_PENDING">Payment Pending</option>
                    <option value="POLICY_ISSUED">Policy Issued</option>
                    <option value="REJECTED">Rejected</option>
                  </select>
                </div>

                {/* From Date */}
                <div style={{ flex: "1 1 140px", minWidth: "135px" }}>
                  <label className="form-label small fw-semibold text-secondary mb-1">From Date</label>
                  <input
                    type="date"
                    className="form-control form-control-sm"
                    value={propFromDate}
                    onChange={(e) => {
                      setPropFromDate(e.target.value);
                      setPropPage(0);
                    }}
                  />
                </div>

                {/* To Date */}
                <div style={{ flex: "1 1 140px", minWidth: "135px" }}>
                  <label className="form-label small fw-semibold text-secondary mb-1">To Date</label>
                  <input
                    type="date"
                    className="form-control form-control-sm"
                    value={propToDate}
                    onChange={(e) => {
                      setPropToDate(e.target.value);
                      setPropPage(0);
                    }}
                  />
                </div>

                {/* Page Size */}
                <div style={{ width: "85px" }}>
                  <label className="form-label small fw-semibold text-secondary mb-1">Size</label>
                  <select
                    className="form-select form-select-sm ps-2 pe-4 text-center"
                    value={propPageSize}
                    onChange={(e) => {
                      setPropPageSize(Number(e.target.value));
                      setPropPage(0);
                    }}
                  >
                    <option value={5}>5</option>
                    <option value={10}>10</option>
                    <option value={20}>20</option>
                  </select>
                </div>

                {/* Reset Button */}
                <div>
                  <button
                    type="button"
                    className="btn btn-sm btn-outline-secondary d-flex align-items-center justify-content-center"
                    title="Reset Filters"
                    aria-label="Reset Filters"
                    style={{ width: "36px", height: "31px" }}
                    onClick={() => {
                      setPropPolicyType("");
                      setPropStatus("");
                      setPropFromDate("");
                      setPropToDate("");
                      setPropPage(0);
                    }}
                  >
                    <span style={{ fontSize: "1.1rem", lineHeight: 1 }}>↻</span>
                  </button>
                </div>
              </div>
            </div>

            {/* Proposals Data Table */}
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light small text-secondary">
                  <tr>
                    <th style={{ width: "10%" }}>Prop ID</th>
                    <th style={{ width: "25%" }}>Customer Name</th>
                    <th style={{ width: "20%" }}>Vehicle Registration</th>
                    <th style={{ width: "15%" }}>Policy Type</th>
                    <th style={{ width: "15%" }}>Status</th>
                    <th style={{ width: "15%" }}>Submitted On</th>
                  </tr>
                </thead>
                <tbody>
                  {propLoading ? (
                    <tr>
                      <td colSpan="6" className="text-center py-4 text-muted small">
                        <span className="spinner-border spinner-border-sm me-2" role="status" />
                        Loading proposals...
                      </td>
                    </tr>
                  ) : propData.list.length === 0 ? (
                    <tr>
                      <td colSpan="6" className="text-center py-4 text-muted small">
                        No proposals found matching the filter criteria.
                      </td>
                    </tr>
                  ) : (
                    propData.list.map((item) => (
                      <tr key={item.id}>
                        <td className="fw-semibold text-dark small">#{item.id}</td>
                        <td className="small text-dark fw-medium">{item.customerName || "N/A"}</td>
                        <td className="small text-secondary">{item.vehicleNumber || "N/A"}</td>
                        <td className="small text-dark fw-semibold">
                          {item.policyType === "COMPREHENSIVE" ? "Comprehensive" : "Third Party"}
                        </td>
                        <td>{getStatusBadge(item.status)}</td>
                        <td className="small text-secondary">
                          {item.createdAt ? new Date(item.createdAt).toLocaleDateString("en-IN") : "N/A"}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {/* Proposals Pagination */}
            <div className="d-flex justify-content-between align-items-center flex-wrap p-3 border-top">
              <div className="text-muted small">
                Showing{" "}
                <strong>
                  {propData.paginationInfo.totalRecords === 0 ? 0 : propPage * propPageSize + 1}
                </strong>{" "}
                to{" "}
                <strong>
                  {Math.min((propPage + 1) * propPageSize, propData.paginationInfo.totalRecords)}
                </strong>{" "}
                of <strong>{propData.paginationInfo.totalRecords}</strong> proposals
              </div>

              <div className="btn-group btn-group-sm">
                <button
                  className="btn btn-outline-secondary"
                  disabled={propPage === 0 || propLoading}
                  onClick={() => setPropPage((p) => Math.max(p - 1, 0))}
                >
                  ‹ Prev
                </button>
                {Array.from({ length: propData.paginationInfo.totalPages || 0 }, (_, idx) => (
                  <button
                    key={idx}
                    className={`btn ${propPage === idx ? "btn-primary" : "btn-outline-secondary"}`}
                    disabled={propLoading}
                    onClick={() => setPropPage(idx)}
                  >
                    {idx + 1}
                  </button>
                ))}
                <button
                  className="btn btn-outline-secondary"
                  disabled={propPage >= (propData.paginationInfo.totalPages || 1) - 1 || propLoading}
                  onClick={() => setPropPage((p) => p + 1)}
                >
                  Next ›
                </button>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: Claims View */}
        {activeTab === "CLAIMS" && (
          <div>
            <div className="p-3 bg-light border-bottom">
              <div className="d-flex flex-wrap align-items-end gap-2">
                {/* Claim Status */}
                <div style={{ flex: "2 1 200px", minWidth: "180px" }}>
                  <label className="form-label small fw-semibold text-secondary mb-1">Claim Status</label>
                  <select
                    className="form-select form-select-sm"
                    value={claimStatus}
                    onChange={(e) => {
                      setClaimStatus(e.target.value);
                      setClaimPage(0);
                    }}
                  >
                    <option value="">All Statuses</option>
                    <option value="FILED">Filed</option>
                    <option value="UNDER_ASSESSMENT">Under Assessment</option>
                    <option value="APPROVED">Approved</option>
                    <option value="SETTLED">Settled</option>
                    <option value="REJECTED">Rejected</option>
                  </select>
                </div>

                {/* From Date */}
                <div style={{ flex: "1 1 140px", minWidth: "135px" }}>
                  <label className="form-label small fw-semibold text-secondary mb-1">From Date</label>
                  <input
                    type="date"
                    className="form-control form-control-sm"
                    value={claimFromDate}
                    onChange={(e) => {
                      setClaimFromDate(e.target.value);
                      setClaimPage(0);
                    }}
                  />
                </div>

                {/* To Date */}
                <div style={{ flex: "1 1 140px", minWidth: "135px" }}>
                  <label className="form-label small fw-semibold text-secondary mb-1">To Date</label>
                  <input
                    type="date"
                    className="form-control form-control-sm"
                    value={claimToDate}
                    onChange={(e) => {
                      setClaimToDate(e.target.value);
                      setClaimPage(0);
                    }}
                  />
                </div>

                {/* Page Size */}
                <div style={{ width: "85px" }}>
                  <label className="form-label small fw-semibold text-secondary mb-1">Size</label>
                  <select
                    className="form-select form-select-sm ps-2 pe-4 text-center"
                    value={claimPageSize}
                    onChange={(e) => {
                      setClaimPageSize(Number(e.target.value));
                      setClaimPage(0);
                    }}
                  >
                    <option value={5}>5</option>
                    <option value={10}>10</option>
                    <option value={20}>20</option>
                  </select>
                </div>

                {/* Reset Button */}
                <div>
                  <button
                    type="button"
                    className="btn btn-sm btn-outline-secondary d-flex align-items-center justify-content-center"
                    title="Reset Filters"
                    aria-label="Reset Filters"
                    style={{ width: "36px", height: "31px" }}
                    onClick={() => {
                      setClaimStatus("");
                      setClaimFromDate("");
                      setClaimToDate("");
                      setClaimPage(0);
                    }}
                  >
                    <span style={{ fontSize: "1.1rem", lineHeight: 1 }}>↻</span>
                  </button>
                </div>
              </div>
            </div>

            {/* Claims Data Table */}
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light small text-secondary">
                  <tr>
                    <th style={{ width: "20%" }}>Claim No</th>
                    <th style={{ width: "25%" }}>Customer Name</th>
                    <th style={{ width: "20%" }}>Claimed Amount</th>
                    <th style={{ width: "15%" }}>Status</th>
                    <th style={{ width: "20%" }}>Filed Date</th>
                  </tr>
                </thead>
                <tbody>
                  {claimLoading ? (
                    <tr>
                      <td colSpan="5" className="text-center py-4 text-muted small">
                        <span className="spinner-border spinner-border-sm me-2" role="status" />
                        Loading claims...
                      </td>
                    </tr>
                  ) : claimData.list.length === 0 ? (
                    <tr>
                      <td colSpan="5" className="text-center py-4 text-muted small">
                        No claims found matching the filter criteria.
                      </td>
                    </tr>
                  ) : (
                    claimData.list.map((item) => (
                      <tr key={item.id}>
                        <td className="fw-semibold text-dark small">{item.claimNo}</td>
                        <td className="small text-dark fw-medium">{item.customerName || "N/A"}</td>
                        <td className="small fw-semibold text-dark">
                          ₹{item.claimedAmount ? Number(item.claimedAmount).toLocaleString("en-IN") : "0.00"}
                        </td>
                        <td>{getStatusBadge(item.status)}</td>
                        <td className="small text-secondary">
                          {item.createdAt ? new Date(item.createdAt).toLocaleDateString("en-IN") : "N/A"}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {/* Claims Pagination */}
            <div className="d-flex justify-content-between align-items-center flex-wrap p-3 border-top">
              <div className="text-muted small">
                Showing{" "}
                <strong>
                  {claimData.paginationInfo.totalRecords === 0 ? 0 : claimPage * claimPageSize + 1}
                </strong>{" "}
                to{" "}
                <strong>
                  {Math.min((claimPage + 1) * claimPageSize, claimData.paginationInfo.totalRecords)}
                </strong>{" "}
                of <strong>{claimData.paginationInfo.totalRecords}</strong> claims
              </div>

              <div className="btn-group btn-group-sm">
                <button
                  className="btn btn-outline-secondary"
                  disabled={claimPage === 0 || claimLoading}
                  onClick={() => setClaimPage((p) => Math.max(p - 1, 0))}
                >
                  ‹ Prev
                </button>
                {Array.from({ length: claimData.paginationInfo.totalPages || 0 }, (_, idx) => (
                  <button
                    key={idx}
                    className={`btn ${claimPage === idx ? "btn-primary" : "btn-outline-secondary"}`}
                    disabled={claimLoading}
                    onClick={() => setClaimPage(idx)}
                  >
                    {idx + 1}
                  </button>
                ))}
                <button
                  className="btn btn-outline-secondary"
                  disabled={claimPage >= (claimData.paginationInfo.totalPages || 1) - 1 || claimLoading}
                  onClick={() => setClaimPage((p) => p + 1)}
                >
                  Next ›
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default OfficerOverviewSection;