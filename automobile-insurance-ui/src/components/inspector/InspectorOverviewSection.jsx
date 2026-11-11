import { useEffect, useState, useCallback } from "react";
import { useParams, useOutletContext } from "react-router";
import axios from "axios";
import { API_SERVER_URL_DEV } from "../../../config";
import "../../styles/InspectorLedger.css";

function InspectorOverviewSection() {
  const params = useParams();
  const token = params.token || localStorage.getItem("token");
  const outletContext = useOutletContext();

  // Keep inspectorId in reactive state so children re-fetch when it arrives
  const [inspectorId, setInspectorId] = useState(
    outletContext?.inspectorId || localStorage.getItem("inspectorId") || null
  );

  // Metrics State
  const [stats, setStats] = useState({
    pendingVehicleSurveys: 0,
    pendingClaimSurveys: 0,
    completedSurveys: 0,
  });
  const [loadingMetrics, setLoadingMetrics] = useState(true);

  // Tab State: "PROPOSALS" | "CLAIMS"
  const [activeTab, setActiveTab] = useState("PROPOSALS");

  // Proposals Filter State
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

  // Claims Filter State
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

  // 1. Fetch Logged-in Inspector Profile if inspectorId is missing
  useEffect(() => {
    const fetchCurrentInspector = async () => {
      if (inspectorId) return;

      try {
        const res = await axios.get(`${API_SERVER_URL_DEV}/api/inspector/profile/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (res.data && res.data.id) {
          setInspectorId(res.data.id);
          localStorage.setItem("inspectorId", res.data.id);
        }
      } catch (err) {
        console.error("Failed to fetch logged in inspector profile:", err);
      } finally {
        setLoadingMetrics(false);
      }
    };

    fetchCurrentInspector();
  }, [inspectorId, token]);

  // 2. Fetch KPI Metrics
  const fetchMetrics = useCallback(async (id) => {
    const targetId = id || inspectorId;
    if (!targetId) return;

    setLoadingMetrics(true);
    try {
      const res = await axios.get(
        `${API_SERVER_URL_DEV}/api/inspector/metrics/${targetId}`,
        { headers: { Authorization: "Bearer " + token } }
      );
      if (res.data) {
        setStats({
          pendingVehicleSurveys: res.data.pendingVehicleSurveys ?? 0,
          pendingClaimSurveys: res.data.pendingClaimSurveys ?? 0,
          completedSurveys: res.data.completedSurveys ?? 0,
        });
      }
    } catch (err) {
      console.error("Failed to load metrics:", err);
    } finally {
      setLoadingMetrics(false);
    }
  }, [inspectorId, token]);

  // 3. Fetch Filtered Proposals
  const fetchProposals = useCallback(async (id) => {
    const targetId = id || inspectorId;
    if (!targetId) return;

    setPropLoading(true);
    try {
      const q = new URLSearchParams();
      q.append("inspectorId", targetId);
      if (propPolicyType) q.append("policyType", propPolicyType);
      if (propStatus) q.append("status", propStatus);
      if (propFromDate) q.append("fromDate", propFromDate);
      if (propToDate) q.append("toDate", propToDate);
      q.append("page", propPage);
      q.append("size", propPageSize);

      const res = await axios.get(
        `${API_SERVER_URL_DEV}/api/inspector/proposals/filter?${q.toString()}`,
        { headers: { Authorization: "Bearer " + token } }
      );
      if (res.data) setPropData(res.data);
    } catch (err) {
      console.error("Proposal filter error:", err);
    } finally {
      setPropLoading(false);
    }
  }, [inspectorId, propPolicyType, propStatus, propFromDate, propToDate, propPage, propPageSize, token]);

  // 4. Fetch Filtered Claims
  const fetchClaims = useCallback(async () => {
    setClaimLoading(true);
    try {
      const q = new URLSearchParams();
      if (claimStatus) q.append("status", claimStatus);
      if (claimFromDate) q.append("fromDate", claimFromDate);
      if (claimToDate) q.append("toDate", claimToDate);
      q.append("page", claimPage);
      q.append("size", claimPageSize);

      const res = await axios.get(
        `${API_SERVER_URL_DEV}/api/inspector/claims/filter?${q.toString()}`,
        { headers: { Authorization: "Bearer " + token } }
      );
      if (res.data) setClaimData(res.data);
    } catch (err) {
      console.error("Claim filter error:", err);
    } finally {
      setClaimLoading(false);
    }
  }, [claimStatus, claimFromDate, claimToDate, claimPage, claimPageSize, token]);

  // 5. Trigger fetches as soon as inspectorId is available
  useEffect(() => {
    if (inspectorId) {
      fetchMetrics(inspectorId);
    }
  }, [inspectorId, fetchMetrics]);

  useEffect(() => {
    if (inspectorId) {
      if (activeTab === "PROPOSALS") {
        fetchProposals(inspectorId);
      } else {
        fetchClaims();
      }
    }
  }, [inspectorId, activeTab, fetchProposals, fetchClaims]);

  const formatLabel = (raw) => {
    if (!raw) return "";
    return raw
      .toLowerCase()
      .split("_")
      .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
      .join(" ");
  };

  const getStatusBadge = (s) => {
    switch (s) {
      case "SCHEDULED":
      case "FILED":
        return <span className="badge bg-primary-subtle text-primary border border-primary-subtle">{formatLabel(s)}</span>;
      case "IN_PROGRESS":
      case "UNDER_ASSESSMENT":
        return <span className="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle">{formatLabel(s)}</span>;
      case "COMPLETED":
      case "APPROVED":
        return <span className="badge bg-success-subtle text-success border border-success-subtle">{formatLabel(s)}</span>;
      case "CANCELLED":
      case "REJECTED":
        return <span className="badge bg-danger-subtle text-danger border border-danger-subtle">{formatLabel(s)}</span>;
      default:
        return <span className="badge bg-secondary-subtle text-secondary">{formatLabel(s)}</span>;
    }
  };

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-4">
        <div>
          <h1 className="h4 fw-bold text-dark mb-1">Field Operations Overview</h1>
          <p className="text-muted small mb-0">
            Real-time status of assigned vehicle roadworthiness surveys and damage claim reviews.
          </p>
        </div>
        <button
          className="btn btn-outline-secondary btn-sm px-3"
          onClick={() => {
            if (inspectorId) {
              fetchMetrics(inspectorId);
              if (activeTab === "PROPOSALS") fetchProposals(inspectorId);
              else fetchClaims();
            }
          }}
          disabled={loadingMetrics || propLoading || claimLoading}
        >
          {loadingMetrics ? "Refreshing..." : "↻ Refresh Overview"}
        </button>
      </div>

      {/* KPI Cards (ONLY in Overview) */}
      <div className="row g-3 mb-4">
        <div className="col-12 col-sm-6 col-md-4">
          <div className="card border-0 shadow-sm p-3 border-top border-4 border-primary">
            <span className="text-muted small fw-semibold text-uppercase">Pending Pre-Policy Surveys</span>
            <span className="fs-3 fw-bold text-primary mt-1">
              {loadingMetrics ? "--" : stats.pendingVehicleSurveys}
            </span>
            <span className="text-muted" style={{ fontSize: "0.75rem" }}>Awaiting physical vehicle check</span>
          </div>
        </div>

        <div className="col-12 col-sm-6 col-md-4">
          <div className="card border-0 shadow-sm p-3 border-top border-4 border-warning">
            <span className="text-muted small fw-semibold text-uppercase">Pending Claim Surveys</span>
            <span className="fs-3 fw-bold text-warning mt-1">
              {loadingMetrics ? "--" : stats.pendingClaimSurveys}
            </span>
            <span className="text-muted" style={{ fontSize: "0.75rem" }}>Under assessment damage claims</span>
          </div>
        </div>

        <div className="col-12 col-sm-6 col-md-4">
          <div className="card border-0 shadow-sm p-3 border-top border-4 border-success">
            <span className="text-muted small fw-semibold text-uppercase">Surveys Completed</span>
            <span className="fs-3 fw-bold text-success mt-1">
              {loadingMetrics ? "--" : stats.completedSurveys}
            </span>
            <span className="text-muted" style={{ fontSize: "0.75rem" }}>Submitted surveyor appraisals</span>
          </div>
        </div>
      </div>

      {/* Tabbed Ledgers (ONLY in Overview) */}
      <div className="card border-0 shadow-sm overflow-hidden mb-4">
        <div className="border-bottom px-3 pt-3 bg-white">
          <ul className="nav nav-tabs card-header-tabs border-0">
            <li className="nav-item">
              <button
                className={`nav-link fw-semibold px-4 ${activeTab === "PROPOSALS" ? "active text-primary" : "text-secondary"}`}
                onClick={() => setActiveTab("PROPOSALS")}
              >
                🚗 Pre-Policy Vehicle Inspections
              </button>
            </li>
            <li className="nav-item">
              <button
                className={`nav-link fw-semibold px-4 ${activeTab === "CLAIMS" ? "active text-primary" : "text-secondary"}`}
                onClick={() => setActiveTab("CLAIMS")}
              >
                💥 Claim Assessments
              </button>
            </li>
          </ul>
        </div>

        {/* TAB 1: Proposals View */}
        {activeTab === "PROPOSALS" && (
          <div>
            <div className="p-3 bg-light border-bottom">
              <div className="d-flex flex-wrap align-items-end gap-2">
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

                <div style={{ flex: "2 1 200px", minWidth: "190px" }}>
                  <label className="form-label small fw-semibold text-secondary mb-1">Inspection Status</label>
                  <select
                    className="form-select form-select-sm"
                    value={propStatus}
                    onChange={(e) => {
                      setPropStatus(e.target.value);
                      setPropPage(0);
                    }}
                  >
                    <option value="">All Statuses</option>
                    <option value="SCHEDULED">Scheduled</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="COMPLETED">Completed</option>
                    <option value="CANCELLED">Cancelled</option>
                  </select>
                </div>

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

                <div>
                  <button
                    type="button"
                    className="btn btn-sm btn-outline-secondary d-flex align-items-center justify-content-center"
                    title="Reset Filters"
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

            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light small text-secondary">
                  <tr>
                    <th style={{ width: "10%" }}>Insp ID</th>
                    <th style={{ width: "10%" }}>Prop ID</th>
                    <th style={{ width: "25%" }}>Customer Name</th>
                    <th style={{ width: "18%" }}>Vehicle Reg No</th>
                    <th style={{ width: "15%" }}>Policy Type</th>
                    <th style={{ width: "12%" }}>Status</th>
                    <th style={{ width: "10%" }}>Submitted On</th>
                  </tr>
                </thead>
                <tbody>
                  {propLoading ? (
                    <tr>
                      <td colSpan="7" className="text-center py-4 text-muted small">
                        <span className="spinner-border spinner-border-sm me-2" role="status" />
                        Loading surveys...
                      </td>
                    </tr>
                  ) : propData.list.length === 0 ? (
                    <tr>
                      <td colSpan="7" className="text-center py-4 text-muted small">
                        No vehicle inspection tasks found matching criteria.
                      </td>
                    </tr>
                  ) : (
                    propData.list.map((item) => (
                      <tr key={item.inspectionId}>
                        <td className="fw-semibold text-dark small">#{item.inspectionId}</td>
                        <td className="text-muted small">#{item.proposalId}</td>
                        <td className="small text-dark fw-medium">{item.customerName || "N/A"}</td>
                        <td className="small text-secondary">{item.vehicleNumber || "N/A"}</td>
                        <td className="small text-dark fw-semibold">
                          {item.policyType === "COMPREHENSIVE" ? "Comprehensive" : "Third Party"}
                        </td>
                        <td>{getStatusBadge(item.inspectionStatus)}</td>
                        <td className="small text-secondary">
                          {item.submittedOn ? new Date(item.submittedOn).toLocaleDateString("en-IN") : "N/A"}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            <div className="d-flex justify-content-between align-items-center flex-wrap p-3 border-top">
              <div className="text-muted small">
                Showing <strong>{propData.paginationInfo.totalRecords === 0 ? 0 : propPage * propPageSize + 1}</strong> to{" "}
                <strong>{Math.min((propPage + 1) * propPageSize, propData.paginationInfo.totalRecords)}</strong> of{" "}
                <strong>{propData.paginationInfo.totalRecords}</strong> tasks
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
                <div style={{ flex: "2 1 200px", minWidth: "180px" }}>
                  <label className="form-label small fw-semibold text-secondary mb-1">Assessment Stage</label>
                  <select
                    className="form-select form-select-sm"
                    value={claimStatus}
                    onChange={(e) => {
                      setClaimStatus(e.target.value);
                      setClaimPage(0);
                    }}
                  >
                    <option value="">All Assessment Stages</option>
                    <option value="UNDER_ASSESSMENT">Under Assessment (Pending Survey)</option>
                    <option value="APPROVED">Assessment Approved</option>
                    <option value="REJECTED">Assessment Rejected</option>
                  </select>
                </div>

                <div style={{ flex: "1 1 140px", minWidth: "135px" }}>
                  <label className="form-label small fw-semibold text-secondary mb-1">Incident From</label>
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

                <div style={{ flex: "1 1 140px", minWidth: "135px" }}>
                  <label className="form-label small fw-semibold text-secondary mb-1">Incident To</label>
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

                <div>
                  <button
                    type="button"
                    className="btn btn-sm btn-outline-secondary d-flex align-items-center justify-content-center"
                    title="Reset Filters"
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

            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light small text-secondary">
                  <tr>
                    <th style={{ width: "18%" }}>Claim No</th>
                    <th style={{ width: "24%" }}>Customer Name</th>
                    <th style={{ width: "20%" }}>Vehicle Reg No</th>
                    <th style={{ width: "15%" }}>Claimed Amount</th>
                    <th style={{ width: "13%" }}>Status</th>
                    <th style={{ width: "10%" }}>Incident Date</th>
                  </tr>
                </thead>
                <tbody>
                  {claimLoading ? (
                    <tr>
                      <td colSpan="6" className="text-center py-4 text-muted small">
                        <span className="spinner-border spinner-border-sm me-2" role="status" />
                        Loading claim assessments...
                      </td>
                    </tr>
                  ) : claimData.list.length === 0 ? (
                    <tr>
                      <td colSpan="6" className="text-center py-4 text-muted small">
                        No claim damage assessments found matching criteria.
                      </td>
                    </tr>
                  ) : (
                    claimData.list.map((item) => (
                      <tr key={item.id}>
                        <td className="fw-semibold text-dark small">{item.claimNo}</td>
                        <td className="small text-dark fw-medium">{item.customerName || "N/A"}</td>
                        <td className="small text-secondary">{item.vehicleNumber || "N/A"}</td>
                        <td className="small fw-semibold text-dark">
                          ₹{item.claimedAmount ? Number(item.claimedAmount).toLocaleString("en-IN") : "0.00"}
                        </td>
                        <td>{getStatusBadge(item.status)}</td>
                        <td className="small text-secondary">
                          {item.incidentDate ? new Date(item.incidentDate).toLocaleDateString("en-IN") : "N/A"}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            <div className="d-flex justify-content-between align-items-center flex-wrap p-3 border-top">
              <div className="text-muted small">
                Showing <strong>{claimData.paginationInfo.totalRecords === 0 ? 0 : claimPage * claimPageSize + 1}</strong> to{" "}
                <strong>{Math.min((claimPage + 1) * claimPageSize, claimData.paginationInfo.totalRecords)}</strong> of{" "}
                <strong>{claimData.paginationInfo.totalRecords}</strong> assessments
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

export default InspectorOverviewSection;