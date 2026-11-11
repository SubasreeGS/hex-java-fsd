import { useEffect, useState, useCallback } from "react";
import { useParams } from "react-router";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import Swal from "sweetalert2";
import "../../styles/OfficerPolicySection.css";

function OfficerPolicySection() {
  const params = useParams();
  const token = params.token || localStorage.getItem("token");

  // Dedicated paginated endpoint
  const paginatedPoliciesUrl = `${API_SERVER_URL_DEV}/api/policy/paginated`;
  const policyByNumberUrl = `${API_SERVER_URL_DEV}/api/policy/number/v1/`;

  // Pagination State
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(5);
  const [totalPages, setTotalPages] = useState(0);
  const [totalRecords, setTotalRecords] = useState(0);

  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");

  // Selected policy details for modal view
  const [selectedPolicy, setSelectedPolicy] = useState(null);
  const [loadingDetails, setLoadingDetails] = useState(false);

  const fetchPolicies = useCallback(async () => {
    setLoading(true);
    try {
      const resp = await axios.get(
        `${paginatedPoliciesUrl}?page=${page}&size=${pageSize}`,
        {
          headers: { Authorization: "Bearer " + token },
        }
      );

      if (resp.data) {
        setPolicies(resp.data.list || []);
        if (resp.data.paginationInfo) {
          setTotalPages(resp.data.paginationInfo.totalPages || 0);
          setTotalRecords(resp.data.paginationInfo.totalRecords || 0);
        }
      } else {
        setPolicies([]);
      }
    } catch (err) {
      console.error("Failed to fetch policies:", err);
      setPolicies([]);
    } finally {
      setLoading(false);
    }
  }, [page, pageSize, token, paginatedPoliciesUrl]);

  useEffect(() => {
    fetchPolicies();
  }, [fetchPolicies]);

  // Fetch full policy details by policy number
  const handleViewPolicyDetails = async (policyNumber) => {
    setLoadingDetails(true);
    setSelectedPolicy(null);
    try {
      const resp = await axios.get(policyByNumberUrl + policyNumber, {
        headers: { Authorization: "Bearer " + token },
      });
      setSelectedPolicy(resp.data);
    } catch (err) {
      console.error("Failed to fetch policy record:", err);
      Swal.fire({
        title: "Error",
        text: "Could not retrieve policy record details.",
        icon: "error",
      });
    } finally {
      setLoadingDetails(false);
    }
  };

  // Instant Native Print / Save to PDF Generator
  const downloadOfficialCertificate = (policy) => {
    const printWindow = window.open("", "_blank", "width=850,height=750");
    if (!printWindow) {
      window.print();
      return;
    }

    const htmlContent = `
      <!DOCTYPE html>
      <html>
        <head>
          <title>Policy Certificate - ${policy.policyNumber || "AutoSure"}</title>
          <style>
            body {
              font-family: Arial, sans-serif;
              color: #1e293b;
              margin: 0;
              padding: 40px;
              background-color: #ffffff;
            }
            .cert-box {
              border: 2px solid #0f172a;
              border-radius: 8px;
              padding: 32px;
              max-width: 750px;
              margin: auto;
            }
            .cert-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              border-bottom: 2px solid #0f172a;
              padding-bottom: 16px;
              margin-bottom: 24px;
            }
            .brand-name {
              font-size: 24px;
              font-weight: 800;
              color: #0f172a;
            }
            .sub-title {
              font-size: 12px;
              color: #64748b;
              text-transform: uppercase;
              letter-spacing: 0.05em;
            }
            .badge-active {
              background-color: #15803d;
              color: #ffffff;
              padding: 4px 12px;
              border-radius: 20px;
              font-weight: bold;
              font-size: 12px;
            }
            .grid {
              display: grid;
              grid-template-columns: 1fr 1fr;
              gap: 16px;
              margin-bottom: 24px;
            }
            .field-box {
              background-color: #f8fafc;
              border: 1px solid #e2e8f0;
              border-radius: 6px;
              padding: 12px 16px;
            }
            .label {
              font-size: 11px;
              color: #64748b;
              text-transform: uppercase;
              font-weight: bold;
              margin-bottom: 4px;
            }
            .val {
              font-size: 14px;
              font-weight: 600;
              color: #0f172a;
            }
            .premium-box {
              background-color: #ecfdf5;
              border: 1px solid #6ee7b7;
              border-radius: 6px;
              padding: 16px;
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-top: 16px;
            }
            .footer-note {
              margin-top: 24px;
              text-align: center;
              font-size: 11px;
              color: #94a3b8;
              border-top: 1px dashed #cbd5e1;
              padding-top: 12px;
            }
            @media print {
              body { padding: 0; }
              .cert-box { border: 1px solid #000; }
            }
          </style>
        </head>
        <body>
          <div class="cert-box">
            <div class="cert-header">
              <div>
                <div class="brand-name">AutoSure General Insurance</div>
                <div class="sub-title">Motor Vehicle Certificate of Insurance (Form 51)</div>
              </div>
              <span class="badge-active">${policy.status || "ACTIVE"}</span>
            </div>

            <div class="grid">
              <div class="field-box">
                <div class="label">Policy Number</div>
                <div class="val" style="font-family: monospace;">${policy.policyNumber || "POL-AUTO-" + policy.id}</div>
              </div>
              <div class="field-box">
                <div class="label">Policyholder Name</div>
                <div class="val">${policy.customerName || "Customer"}</div>
              </div>
              <div class="field-box">
                <div class="label">Vehicle Registration Number</div>
                <div class="val" style="color: #2563eb; font-family: monospace;">${policy.registrationNo || "--"}</div>
              </div>
              <div class="field-box">
                <div class="label">Vehicle Make & Model</div>
                <div class="val">${policy.vehicleMake || ""} ${policy.vehicleModel || ""}</div>
              </div>
              <div class="field-box">
                <div class="label">Policy Coverage Type</div>
                <div class="val">${policy.policyType || "COMPREHENSIVE"}</div>
              </div>
              <div class="field-box">
                <div class="label">Insured Declared Value (IDV)</div>
                <div class="val">INR ${Number(policy.idv || 0).toLocaleString("en-IN")}</div>
              </div>
              <div class="field-box">
                <div class="label">Coverage Commences</div>
                <div class="val">${policy.startDate || "--"}</div>
              </div>
              <div class="field-box">
                <div class="label">Coverage Expires</div>
                <div class="val">${policy.endDate || "--"}</div>
              </div>
            </div>

            <div class="premium-box">
              <div>
                <div class="label" style="color: #047857;">Total Premium Paid (Incl. Taxes)</div>
                <div style="font-size: 20px; font-weight: 800; color: #047857;">
                  INR ${Number(policy.totalPremium || 0).toLocaleString("en-IN")}
                </div>
              </div>
              <div style="text-align: right; font-size: 11px; color: #047857;">
                Payment Authorized & Verified
              </div>
            </div>

            <div class="footer-note">
              This document is an authentic certificate issued by AutoSure Insurance Co. Ltd. from the Underwriting Policy Ledger.
            </div>
          </div>
        </body>
      </html>
    `;

    printWindow.document.open();
    printWindow.document.write(htmlContent);
    printWindow.document.close();

    setTimeout(() => {
      printWindow.focus();
      printWindow.print();
    }, 200);
  };

  const filtered = policies.filter((p) => {
    const term = searchTerm.toLowerCase().trim();
    if (!term) return true;

    return (
      p.policyNumber?.toLowerCase().includes(term) ||
      p.registrationNo?.toLowerCase().includes(term) ||
      p.customerName?.toLowerCase().includes(term) ||
      p.vehicleMake?.toLowerCase().includes(term) ||
      p.vehicleModel?.toLowerCase().includes(term)
    );
  });

  return (
    <div className="officer-pol-panel">
      {/* Header */}
      <div className="officer-pol-header d-flex justify-content-between align-items-center flex-wrap gap-2">
        <div>
          <h2 className="h5 fw-bold text-dark mb-1">
            Insurance Policy Audit Registry
          </h2>
          <p className="text-muted small mb-0">
            System-wide repository of active, expired, and renewed automobile policies issued across the platform.
          </p>
        </div>

        <div className="d-flex align-items-center gap-2 flex-wrap">
          <input
            type="text"
            className="form-control form-control-sm"
            style={{ width: "260px" }}
            placeholder="Search policy, reg no, customer..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <button className="btn btn-outline-secondary btn-sm" onClick={fetchPolicies}>
            Refresh Ledger
          </button>
        </div>
      </div>

      {/* Main Content: Card-Based Grid Layout */}
      <div className="p-3 p-md-4">
        {loading ? (
          <div className="p-5 text-center text-muted">
            <div className="spinner-border spinner-border-sm text-primary me-2"></div>
            Loading master policy ledger...
          </div>
        ) : filtered.length === 0 ? (
          <div className="p-5 text-center text-muted bg-light rounded-3 border">
            <div className="fs-1 mb-2">📜</div>
            <h6 className="fw-bold text-dark mb-1">No Policies Found</h6>
            <p className="small text-muted mb-0">
              {searchTerm
                ? "No policies matching search criteria on this page."
                : "No issued policies found in the master registry."}
            </p>
          </div>
        ) : (
          <div className="d-flex flex-column gap-3">
            {filtered.map((pol, idx) => (
              <div key={pol.id || idx} className="policy-ledger-card">
                {/* Meta Header */}
                <div className="d-flex flex-wrap justify-content-between align-items-center border-bottom pb-2 mb-3 gap-2">
                  <div className="d-flex align-items-center gap-2 flex-wrap">
                    <span className="policy-no-badge">{pol.policyNumber}</span>
                    <span className="fw-bold text-primary font-monospace">{pol.registrationNo}</span>
                    <span className="text-secondary small">Policy ID: #{pol.id}</span>
                  </div>
                  <div className="d-flex align-items-center gap-2">
                    <span className="plan-pill">{pol.policyType}</span>
                    <span
                      className={`badge px-2 py-1 ${
                        pol.status === "ACTIVE"
                          ? "bg-success-subtle text-success border border-success-subtle"
                          : "bg-danger-subtle text-danger border border-danger-subtle"
                      }`}
                    >
                      {pol.status || "ACTIVE"}
                    </span>
                  </div>
                </div>

                {/* 3-Column Structured Layout */}
                <div className="row g-3 align-items-center">
                  {/* Column 1: Policyholder & Vehicle */}
                  <div className="col-12 col-md-5 policy-col policy-col-divider">
                    <div className="field-label mb-1">Policyholder & Vehicle Details</div>
                    <div className="fw-bold text-dark fs-6">{pol.customerName || "Customer on Record"}</div>
                    <div className="small text-secondary mt-1">
                      <strong>Vehicle:</strong> {pol.vehicleMake} {pol.vehicleModel}
                    </div>
                    <div className="small text-muted font-monospace mt-1">
                      Registration: <strong className="text-dark">{pol.registrationNo}</strong>
                    </div>
                  </div>

                  {/* Column 2: Financials & Coverage Term */}
                  <div className="col-12 col-md-4 policy-col policy-col-divider">
                    <div className="field-label mb-1">Valuation & Premium</div>
                    <div className="d-flex justify-content-between small mb-1">
                      <span className="text-muted">Assessed IDV:</span>
                      <strong className="text-dark">₹{Number(pol.idv || 0).toLocaleString("en-IN")}</strong>
                    </div>
                    <div className="d-flex justify-content-between small mb-1">
                      <span className="text-muted">Total Premium:</span>
                      <strong className="text-success">₹{Number(pol.totalPremium || 0).toLocaleString("en-IN")}</strong>
                    </div>
                    <div className="small text-secondary border-top pt-1 mt-1">
                      Validity: <strong>{pol.startDate || "--"}</strong> to <strong>{pol.endDate || "--"}</strong>
                    </div>
                  </div>

                  {/* Column 3: Actions */}
                  <div className="col-12 col-md-3 policy-col">
                    <div className="field-label mb-2">Audit Actions</div>
                    <div className="d-flex flex-column gap-2">
                      <button
                        className="btn btn-outline-primary btn-sm w-100 fw-semibold"
                        data-bs-toggle="modal"
                        data-bs-target="#viewPolicyModal"
                        onClick={() => handleViewPolicyDetails(pol.policyNumber)}
                      >
                        👁 View Policy
                      </button>
                      <button
                        className="btn btn-officer-download btn-sm w-100 d-flex align-items-center justify-content-center gap-1"
                        onClick={() => downloadOfficialCertificate(pol)}
                        title="Download Certificate"
                      >
                        <span>Download Certificate</span>
                        <span>↓</span>
                      </button>
                    </div>
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
          <strong>{totalRecords}</strong> policies
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
              disabled={page === 0 || loading}
              onClick={() => setPage((p) => Math.max(p - 1, 0))}
            >
              ‹ Prev
            </button>
            {Array.from({ length: totalPages || 0 }, (_, idx) => (
              <button
                key={idx}
                className={`btn ${page === idx ? "btn-primary" : "btn-outline-secondary"}`}
                disabled={loading}
                onClick={() => setPage(idx)}
              >
                {idx + 1}
              </button>
            ))}
            <button
              className="btn btn-outline-secondary"
              disabled={page >= (totalPages || 1) - 1 || loading}
              onClick={() => setPage((p) => p + 1)}
            >
              Next ›
            </button>
          </div>
        </div>
      </div>

      {/* Policy Details Modal for Officer Review */}
      <div
        className="modal fade"
        id="viewPolicyModal"
        tabIndex="-1"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-lg modal-dialog-centered">
          <div className="modal-content border-0 shadow">
            <div className="modal-header modal-officer-header px-4 py-3">
              <div className="d-flex align-items-center gap-2">
                <span className="fs-5">🛡️</span>
                <div>
                  <h5 className="modal-title fs-6 fw-bold mb-0">
                    Official Car Insurance Policy
                  </h5>
                  <span className="small text-secondary" style={{ fontSize: "0.75rem" }}>
                    AutoSure General Insurance • IRDAI Compliant
                  </span>
                </div>
              </div>
              <button
                type="button"
                className="btn-close"
                data-bs-dismiss="modal"
              ></button>
            </div>

            <div className="modal-body p-4">
              {loadingDetails ? (
                <div className="text-center py-5 text-muted">
                  <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                  Fetching authentic policy ledger record...
                </div>
              ) : selectedPolicy ? (
                <div className="d-flex flex-column gap-3">
                  {/* Top Bar */}
                  <div className="d-flex justify-content-between align-items-center p-3 bg-light rounded border">
                    <div>
                      <span className="text-muted small d-block text-uppercase" style={{ fontSize: "0.7rem", letterSpacing: "0.05em" }}>
                        Policy Schedule Number
                      </span>
                      <strong className="fs-5 text-primary font-monospace">
                        {selectedPolicy.policyNumber}
                      </strong>
                    </div>
                    <span
                      className={`badge px-3 py-2 fw-semibold ${
                        selectedPolicy.status === "ACTIVE"
                          ? "bg-success text-white"
                          : "bg-danger text-white"
                      }`}
                    >
                      {selectedPolicy.status || "ACTIVE"}
                    </span>
                  </div>

                  {/* 2-Column Specs Grid */}
                  <div className="row g-3">
                    <div className="col-12 col-md-6">
                      <div className="spec-field-box">
                        <span className="spec-caption">
                          Policyholder Details
                        </span>
                        <div className="fw-bold text-dark fs-6 mb-1">
                          {selectedPolicy.customerName || "Customer"}
                        </div>
                        <div className="small text-muted">
                          Official Registered Insured
                        </div>
                      </div>
                    </div>

                    <div className="col-12 col-md-6">
                      <div className="spec-field-box">
                        <span className="spec-caption">
                          Vehicle Identification
                        </span>
                        <div className="fw-bold text-primary fs-6 font-monospace mb-1">
                          {selectedPolicy.registrationNo}
                        </div>
                        <div className="small text-secondary">
                          {selectedPolicy.vehicleMake} {selectedPolicy.vehicleModel}
                        </div>
                      </div>
                    </div>

                    <div className="col-12 col-md-6">
                      <div className="spec-field-box">
                        <span className="spec-caption">
                          Coverage & Valuation
                        </span>
                        <div className="d-flex justify-content-between py-1 border-bottom small">
                          <span className="text-muted">Plan Type:</span>
                          <strong>{selectedPolicy.policyType}</strong>
                        </div>
                        <div className="d-flex justify-content-between py-1 small">
                          <span className="text-muted">Assessed IDV:</span>
                          <strong className="text-dark">
                            ₹{Number(selectedPolicy.idv || 0).toLocaleString("en-IN")}
                          </strong>
                        </div>
                      </div>
                    </div>

                    <div className="col-12 col-md-6">
                      <div className="spec-field-box">
                        <span className="spec-caption">
                          Premium & Validity
                        </span>
                        <div className="d-flex justify-content-between py-1 border-bottom small">
                          <span className="text-muted">Coverage Term:</span>
                          <strong>
                            {selectedPolicy.startDate || "--"} to {selectedPolicy.endDate || "--"}
                          </strong>
                        </div>
                        <div className="d-flex justify-content-between py-1 small">
                          <span className="text-muted">Total Premium Paid:</span>
                          <strong className="text-success fs-6">
                            ₹{Number(selectedPolicy.totalPremium || 0).toLocaleString("en-IN")}
                          </strong>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="text-center py-4 text-muted">No policy data available.</div>
              )}
            </div>

            <div className="modal-footer bg-light px-4 py-2 d-flex justify-content-between">
              <button
                type="button"
                className="btn btn-officer-download d-flex align-items-center gap-1"
                disabled={!selectedPolicy}
                onClick={() => selectedPolicy && downloadOfficialCertificate(selectedPolicy)}
              >
                <span>Download Policy PDF</span>
                <span>↓</span>
              </button>

              <button type="button" className="btn btn-secondary btn-sm" data-bs-dismiss="modal">
                Close
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default OfficerPolicySection;