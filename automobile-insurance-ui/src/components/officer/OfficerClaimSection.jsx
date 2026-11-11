import { useEffect, useState, useCallback } from "react";
import { useParams } from "react-router";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import Swal from "sweetalert2";
import "../../styles/OfficerClaimSection.css";

function OfficerClaimSection() {
  const params = useParams();
  const token = params.token || localStorage.getItem("token");

  // Pagination State
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(5);
  const [totalPages, setTotalPages] = useState(0);
  const [totalRecords, setTotalRecords] = useState(0);

  // Expanded card state map
  const [expandedCards, setExpandedCards] = useState({});

  // API Endpoints
  const myInspectorsDropdownUrl = `${API_SERVER_URL_DEV}/api/inspector/my-inspectors/dropdown`;
  const assessmentByClaimUrl = `${API_SERVER_URL_DEV}/api/assessment/claim/v2/`;
  const settleClaimUrl = `${API_SERVER_URL_DEV}/api/claim/settlement/process/`;
  const claimReceiptUrl = `${API_SERVER_URL_DEV}/api/claim/settlement/receipt/`;

  const [claims, setClaims] = useState([]);
  const [inspectors, setInspectors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");

  // Modal 1: Assign Inspector
  const [targetClaimForAssign, setTargetClaimForAssign] = useState(null);
  const [selectedInspectorId, setSelectedInspectorId] = useState("");
  const [assigning, setAssigning] = useState(false);

  // Modal 2: Adjudication Review
  const [targetClaimForReview, setTargetClaimForReview] = useState(null);
  const [claimAssessment, setClaimAssessment] = useState(null);
  const [loadingAssessment, setLoadingAssessment] = useState(false);
  const [approvedAmount, setApprovedAmount] = useState("");
  const [reviewDecision, setReviewDecision] = useState("APPROVED");
  const [reviewing, setReviewing] = useState(false);

  // Modal 3: Settlement Receipt
  const [receiptData, setReceiptData] = useState(null);
  const [loadingReceipt, setLoadingReceipt] = useState(false);
  const [settlingId, setSettlingId] = useState(null);

  // 1. Fetch Paginated Claims & Officer's Own Inspectors
  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const allClaimsUrl = `${API_SERVER_URL_DEV}/api/claim/officer/all?page=${page}&size=${pageSize}`;

      const [claimRes, inspRes] = await Promise.allSettled([
        axios.get(allClaimsUrl, { headers: { Authorization: "Bearer " + token } }),
        axios.get(myInspectorsDropdownUrl, { headers: { Authorization: "Bearer " + token } }),
      ]);

      if (claimRes.status === "fulfilled" && claimRes.value.data) {
        const payload = claimRes.value.data;
        // Sort descending so newly filed claims stay on top
        const list = (payload.list || []).sort((a, b) => (b.id || 0) - (a.id || 0));
        setClaims(list);
        if (payload.paginationInfo) {
          setTotalPages(payload.paginationInfo.totalPages || 0);
          setTotalRecords(payload.paginationInfo.totalRecords || 0);
        }
      } else {
        setClaims([]);
      }

      setInspectors(inspRes.status === "fulfilled" ? inspRes.value.data || [] : []);
    } catch (err) {
      console.error("Failed to load claims ledger:", err);
      setClaims([]);
    } finally {
      setLoading(false);
    }
  }, [page, pageSize, token, myInspectorsDropdownUrl]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const toggleExpand = (claimId) => {
    setExpandedCards((prev) => ({
      ...prev,
      [claimId]: !prev[claimId],
    }));
  };

  // Assign Inspector Logic
  const openAssignModal = (claim) => {
    setTargetClaimForAssign(claim);
    setSelectedInspectorId("");
  };

  const handleAssignInspector = async (e) => {
    e.preventDefault();
    if (!selectedInspectorId || !targetClaimForAssign || !targetClaimForAssign.id) return;

    setAssigning(true);
    const assignUrl = `${API_SERVER_URL_DEV}/api/claim/assign/${selectedInspectorId}/${targetClaimForAssign.id}`;

    try {
      await axios.put(assignUrl, {}, { headers: { Authorization: "Bearer " + token } });
      const closeBtn = document.getElementById("closeAssignModalBtn");
      if (closeBtn) closeBtn.click();

      Swal.fire({
        title: "Inspector Assigned",
        text: `Claim ${targetClaimForAssign.claimNo} assigned for technical survey.`,
        icon: "success",
        confirmButtonColor: "#0f172a",
        timer: 2000,
      });

      setPage(0);
      loadData();
    } catch (err) {
      const msg = err?.response?.data?.message || "Failed to assign surveyor.";
      Swal.fire({ title: "Assignment Failed", text: msg, icon: "error" });
    } finally {
      setAssigning(false);
    }
  };

  // Review & Adjudication Logic
  const openReviewModal = async (claim) => {
    setTargetClaimForReview(claim);
    setClaimAssessment(null);
    setApprovedAmount("");
    setReviewDecision("APPROVED");
    setLoadingAssessment(true);

    try {
      const resp = await axios.get(assessmentByClaimUrl + claim.id, {
        headers: { Authorization: "Bearer " + token },
      });

      // Normalize data in case backend returns array or single DTO
      const data = Array.isArray(resp.data) ? resp.data[0] : resp.data;
      setClaimAssessment(data);

      if (data && Number(data.estimatedCost) > 0) {
        setApprovedAmount(String(data.estimatedCost));
      } else if (claim.claimedAmount) {
        setApprovedAmount(String(claim.claimedAmount));
      }
    } catch (err) {
      console.warn("Could not retrieve claim assessment findings:", err?.response?.data || err);
      setClaimAssessment(null);
    } finally {
      setLoadingAssessment(false);
    }
  };

  // Enhanced Check: True if assessment was submitted (has estimatedCost > 0 OR status is COMPLETED/SUBMITTED)
  const isAssessmentReady = Boolean(
    claimAssessment &&
    (
      (claimAssessment.estimatedCost !== null &&
       claimAssessment.estimatedCost !== undefined &&
       Number(claimAssessment.estimatedCost) > 0) ||
      claimAssessment.status === "COMPLETED" ||
      claimAssessment.status === "SUBMITTED" ||
      claimAssessment.status === "ASSESSED"
    )
  );

  const handleSubmitReview = async (e) => {
    e.preventDefault();
    if (!targetClaimForReview || !targetClaimForReview.id) return;

    if (!isAssessmentReady) {
      Swal.fire({
        title: "Survey Pending",
        text: "You cannot approve or reject this claim until the assigned surveyor submits the damage assessment report.",
        icon: "warning",
        confirmButtonColor: "#0f172a",
      });
      return;
    }

    if (reviewDecision === "APPROVED" && (!approvedAmount || Number(approvedAmount) <= 0)) {
      Swal.fire({
        title: "Invalid Amount",
        text: "Please enter a valid approved settlement amount.",
        icon: "warning",
      });
      return;
    }

    setReviewing(true);
    const reviewUrl = `${API_SERVER_URL_DEV}/api/claim/review/${targetClaimForReview.id}`;
    const payload = {
      status: reviewDecision,
      approvedAmount: reviewDecision === "APPROVED" ? Number(approvedAmount) : 0.0,
    };

    try {
      await axios.put(reviewUrl, payload, {
        headers: { Authorization: "Bearer " + token },
      });

      const closeBtn = document.getElementById("closeReviewModalBtn");
      if (closeBtn) closeBtn.click();

      Swal.fire({
        title: reviewDecision === "APPROVED" ? "Claim Approved!" : "Claim Rejected",
        text: `Claim ${targetClaimForReview.claimNo} status updated.`,
        icon: reviewDecision === "APPROVED" ? "success" : "info",
        confirmButtonColor: "#0f172a",
        timer: 2000,
      });

      setPage(0);
      loadData();
    } catch (err) {
      const msg = err?.response?.data?.message || "Failed to finalize claim review.";
      Swal.fire({ title: "Review Error", text: msg, icon: "error" });
    } finally {
      setReviewing(false);
    }
  };

  // Payout Settlement Logic
  const handleProcessSettlement = async (claim) => {
    const confirm = await Swal.fire({
      title: "Confirm Claim Settlement?",
      html: `
        <div class="text-start small">
          <p class="mb-1"><strong>Claim:</strong> ${claim.claimNo}</p>
          <p class="mb-1"><strong>Policy:</strong> ${claim.policyNumber}</p>
          <p class="mb-0 text-success fs-5"><strong>Disbursement: ₹${Number(claim.approvedAmount || 0).toLocaleString("en-IN")}</strong></p>
        </div>
      `,
      icon: "question",
      showCancelButton: true,
      confirmButtonText: "💳 Disburse Payout",
      confirmButtonColor: "#059669",
      cancelButtonText: "Cancel",
    });

    if (!confirm.isConfirmed) return;

    setSettlingId(claim.id);
    try {
      await axios.post(
        settleClaimUrl + claim.id,
        {},
        { headers: { Authorization: "Bearer " + token } }
      );

      Swal.fire({
        title: "Payout Disbursed!",
        text: `Settlement funds for claim ${claim.claimNo} successfully transferred.`,
        icon: "success",
        confirmButtonColor: "#0f172a",
        timer: 2000,
      });

      loadData();
    } catch (err) {
      const msg = err?.response?.data?.message || "Failed to process claim settlement.";
      Swal.fire({ title: "Disbursement Failed", text: msg, icon: "error" });
    } finally {
      setSettlingId(null);
    }
  };

  // Fetch Receipt
  const handleOpenReceipt = async (claimId) => {
    setLoadingReceipt(true);
    setReceiptData(null);
    try {
      const resp = await axios.get(claimReceiptUrl + claimId, {
        headers: { Authorization: "Bearer " + token },
      });
      setReceiptData(resp.data);
    } catch (err) {
      Swal.fire({
        title: "Error",
        text: "Could not retrieve settlement receipt voucher.",
        icon: "error",
      });
    } finally {
      setLoadingReceipt(false);
    }
  };

  // Safe 1-Page Popup Print Handler
  const handlePrintReceipt = () => {
    const printContent = document.getElementById("officerPrintableReceiptArea");
    if (!printContent) return;

    const printWindow = window.open("", "_blank", "width=850,height=700");
    if (!printWindow) {
      window.print();
      return;
    }

    const html = `
      <!DOCTYPE html>
      <html>
        <head>
          <title>Claim Settlement Receipt - ${receiptData?.claimNo || "Voucher"}</title>
          <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
          <style>
            @page {
              size: A4 portrait;
              margin: 12mm;
            }
            body {
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
              color: #1e293b;
              background: #ffffff;
              padding: 20px;
              margin: 0;
            }
            .receipt-container {
              max-width: 620px;
              margin: 0 auto;
              border: 1px solid #cbd5e1;
              border-radius: 8px;
              padding: 24px;
            }
            .table-box {
              background: #f8fafc;
              border: 1px solid #e2e8f0;
              border-radius: 6px;
              padding: 12px 16px;
              margin-bottom: 16px;
            }
            .amount-box {
              background: #dcfce7;
              border: 1px solid #bbf7d0;
              border-radius: 6px;
              padding: 14px;
              text-align: center;
              margin-bottom: 16px;
            }
          </style>
        </head>
        <body>
          <div class="receipt-container">
            ${printContent.innerHTML}
          </div>
        </body>
      </html>
    `;

    printWindow.document.open();
    printWindow.document.write(html);
    printWindow.document.close();

    setTimeout(() => {
      printWindow.focus();
      printWindow.print();
    }, 400);
  };

  const renderStatusBadge = (status) => {
    switch (status) {
      case "FILED":
        return <span className="badge badge-claim-filed px-2 py-1">FILED</span>;
      case "UNDER_ASSESSMENT":
        return <span className="badge badge-claim-progress px-2 py-1">UNDER SURVEY</span>;
      case "APPROVED":
        return <span className="badge badge-claim-approved px-2 py-1">APPROVED</span>;
      case "REJECTED":
        return <span className="badge badge-claim-rejected px-2 py-1">REJECTED</span>;
      case "SETTLED":
        return (
          <span className="badge bg-success-subtle text-success border border-success-subtle px-2 py-1 fw-semibold">
            ✓ SETTLED
          </span>
        );
      default:
        return <span className="badge bg-secondary px-2 py-1">{status}</span>;
    }
  };

  const filteredClaims = claims.filter((c) => {
    const term = searchTerm.toLowerCase().trim();
    if (!term) return true;
    return (
      c.claimNo?.toLowerCase().includes(term) ||
      c.policyNumber?.toLowerCase().includes(term) ||
      c.incidentDescription?.toLowerCase().includes(term) ||
      c.officerName?.toLowerCase().includes(term) ||
      c.inspectorName?.toLowerCase().includes(term)
    );
  });

  return (
    <div className="officer-claim-panel">
      {/* Header Bar */}
      <div className="officer-claim-header d-flex justify-content-between align-items-center flex-wrap gap-2">
        <div>
          <h2 className="h5 fw-bold text-dark mb-1">Claims Adjudication Desk</h2>
          <p className="text-muted small mb-0">
            Dispatch licensed surveyors, verify repair appraisals, approve payouts, and issue settlement vouchers.
          </p>
        </div>

        <div className="d-flex align-items-center gap-2 flex-wrap">
          <input
            type="text"
            className="form-control form-control-sm"
            style={{ width: "260px" }}
            placeholder="Search claim, policy, officer, surveyor..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <button className="btn btn-outline-secondary btn-sm" onClick={loadData}>
            Refresh Queue
          </button>
        </div>
      </div>

      {/* Main Content: Card-Based Clean View */}
      <div className="p-3 p-md-4">
        {loading ? (
          <div className="p-5 text-center text-muted">
            <div className="spinner-border spinner-border-sm text-primary me-2"></div>
            Loading master claims repository...
          </div>
        ) : filteredClaims.length === 0 ? (
          <div className="p-5 text-center text-muted bg-light rounded-3 border">
            <div className="fs-1 mb-2">🛡️</div>
            <h6 className="fw-bold text-dark mb-1">No Claims in Ledger</h6>
            <p className="small text-muted mb-0">
              {searchTerm
                ? "No registered claims match your search query."
                : "No claims registered currently in the adjudication queue."}
            </p>
          </div>
        ) : (
          <div className="d-flex flex-column gap-3">
            {filteredClaims.map((item, idx) => {
              const isExpanded = !!expandedCards[item.id];

              return (
                <div key={item.claimNo || item.id || idx} className="claim-adjudication-card">
                  {/* Top Meta Bar */}
                  <div className="d-flex flex-wrap justify-content-between align-items-center border-bottom pb-2 mb-3 gap-2">
                    <div className="d-flex align-items-center gap-2 flex-wrap">
                      <span className="claim-no-badge">{item.claimNo}</span>
                      <span className="policy-number-chip">
                        <span className="text-muted">Policy:</span> <strong>{item.policyNumber}</strong>
                      </span>
                      <span className="text-secondary small font-monospace">Ref #{item.id}</span>
                    </div>
                    <div className="d-flex align-items-center gap-3">
                      <span className="text-muted small">
                        Incident Date: <strong className="text-dark">{item.incidentDate || "N/A"}</strong>
                      </span>
                      {renderStatusBadge(item.status)}
                    </div>
                  </div>

                  {/* 3-Column Enterprise Grid: 5 / 4 / 3 Ratio */}
                  <div className="row g-4 align-items-stretch">
                    {/* Column 1: Incident Description & Circumstances (5 cols) */}
                    <div className="col-12 col-md-5 claim-col claim-col-divider d-flex flex-column justify-content-between">
                      <div>
                        <div className="field-label mb-2">Accident Circumstances</div>
                        <div className="incident-callout-box">
                          <p className={`incident-text mb-0 ${!isExpanded ? "clamp-3" : ""}`}>
                            "{item.incidentDescription || "No incident description recorded by claimant."}"
                          </p>
                          {item.incidentDescription && item.incidentDescription.length > 130 && (
                            <button
                              type="button"
                              className="btn btn-link btn-sm p-0 mt-1 text-decoration-none fw-semibold shadow-none"
                              style={{ fontSize: "0.78rem" }}
                              onClick={() => toggleExpand(item.id)}
                            >
                              {isExpanded ? "▴ Show Less" : "▾ Read Full Statement"}
                            </button>
                          )}
                        </div>
                      </div>
                      <div className="d-flex align-items-center gap-1 text-muted small mt-2">
                        <span>Reported On:</span>
                        <strong className="text-secondary">
                          {item.createdAt ? new Date(item.createdAt).toLocaleDateString("en-IN") : "Recent"}
                        </strong>
                      </div>
                    </div>

                    {/* Column 2: Financial Appraisal & Assigned Roster (4 cols) */}
                    <div className="col-12 col-md-4 claim-col claim-col-divider d-flex flex-column justify-content-between">
                      <div>
                        <div className="field-label mb-2">Financial Appraisal & Roster</div>

                        <div className="metric-row">
                          <span className="metric-name">Claimed Damage</span>
                          <span className="metric-value text-dark">
                            ₹{Number(item.claimedAmount || 0).toLocaleString("en-IN")}
                          </span>
                        </div>

                        <div className="metric-row">
                          <span className="metric-name">Approved Settlement</span>
                          <span className="metric-value text-success">
                            {item.approvedAmount && Number(item.approvedAmount) > 0
                              ? `₹${Number(item.approvedAmount).toLocaleString("en-IN")}`
                              : "--"}
                          </span>
                        </div>

                        <div className="divider-subtle my-2"></div>

                        <div className="metric-row">
                          <span className="metric-name">Field Surveyor</span>
                          <span>
                            {item.inspectorName ? (
                              <span className="badge bg-light text-primary border border-primary-subtle px-2 py-1 fw-semibold">
                                🕵️ {item.inspectorName}
                              </span>
                            ) : (
                              <span className="badge bg-light text-muted border px-2 py-1">Not Assigned</span>
                            )}
                          </span>
                        </div>

                        <div className="metric-row mt-1">
                          <span className="metric-name">Reviewing Officer</span>
                          <span className="fw-semibold text-dark small">{item.officerName || "Unassigned"}</span>
                        </div>
                      </div>
                    </div>

                    {/* Column 3: Workflow Action Area (3 cols) */}
                    <div className="col-12 col-md-3 claim-col d-flex flex-column justify-content-between">
                      <div>
                        <div className="field-label mb-2">Stage Workflow Action</div>
                        <div className="p-2 bg-light border rounded small mb-3">
                          <span className="text-muted d-block text-uppercase" style={{ fontSize: "0.68rem" }}>
                            Current Lifecycle
                          </span>
                          <strong className="text-dark font-monospace">{item.status}</strong>
                        </div>
                      </div>

                      <div className="pt-2">
                        {/* Step 1: Assign */}
                        {item.status === "FILED" && (
                          <button
                            className="btn btn-primary btn-sm w-100 fw-semibold shadow-sm py-2"
                            data-bs-toggle="modal"
                            data-bs-target="#assignInspectorModal"
                            onClick={() => openAssignModal(item)}
                          >
                            🕵️ Dispatch Surveyor →
                          </button>
                        )}

                        {/* Step 2: Adjudicate */}
                        {item.status === "UNDER_ASSESSMENT" && (
                          <button
                            className="btn btn-warning btn-sm w-100 fw-semibold text-dark shadow-sm py-2"
                            data-bs-toggle="modal"
                            data-bs-target="#reviewClaimModal"
                            onClick={() => openReviewModal(item)}
                          >
                            ⚖️ Review & Adjudicate →
                          </button>
                        )}

                        {/* Step 3: Settle & Disburse */}
                        {item.status === "APPROVED" && (
                          <button
                            className="btn btn-success btn-sm w-100 fw-semibold shadow-sm py-2"
                            disabled={settlingId === item.id}
                            onClick={() => handleProcessSettlement(item)}
                          >
                            {settlingId === item.id ? "Disbursing..." : "💳 Disburse Settlement →"}
                          </button>
                        )}

                        {/* Step 4: View Voucher Receipt */}
                        {item.status === "SETTLED" && (
                          <button
                            className="btn btn-outline-dark btn-sm w-100 fw-semibold shadow-sm py-2 d-flex align-items-center justify-content-center gap-2"
                            data-bs-toggle="modal"
                            data-bs-target="#officerClaimSettlementReceiptModal"
                            onClick={() => handleOpenReceipt(item.id)}
                          >
                            <span>🧾</span>
                            <span>Print Settlement Voucher</span>
                          </button>
                        )}

                        {/* Fallback Audit */}
                        {item.status === "REJECTED" && (
                          <button
                            className="btn btn-outline-secondary btn-sm w-100 fw-semibold py-2"
                            data-bs-toggle="modal"
                            data-bs-target="#reviewClaimModal"
                            onClick={() => openReviewModal(item)}
                          >
                            👁 View Rejection Audit
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Pagination Strip */}
      <div className="d-flex justify-content-between align-items-center flex-wrap px-4 py-3 border-top bg-white">
        <div className="text-muted small">
          Showing <strong>{totalRecords === 0 ? 0 : page * pageSize + 1}</strong> to{" "}
          <strong>{Math.min((page + 1) * pageSize, totalRecords)}</strong> of{" "}
          <strong>{totalRecords}</strong> claims
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

      {/* Modal 1: Assign Field Surveyor */}
      <div className="modal fade" id="assignInspectorModal" tabIndex="-1" aria-hidden="true">
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content border-0 shadow">
            <div className="modal-header bg-dark text-white px-4 py-3">
              <h5 className="modal-title fs-6 fw-bold">Dispatch Surveyor for Claim</h5>
              <button
                type="button"
                id="closeAssignModalBtn"
                className="btn-close btn-close-white"
                data-bs-dismiss="modal"
              ></button>
            </div>
            <form onSubmit={handleAssignInspector}>
              <div className="modal-body p-4">
                {targetClaimForAssign && (
                  <div className="p-3 bg-light rounded border mb-3 small">
                    <div className="d-flex justify-content-between mb-1">
                      <span className="text-muted">Claim Number:</span>
                      <strong className="font-monospace text-primary">{targetClaimForAssign.claimNo}</strong>
                    </div>
                    <div className="d-flex justify-content-between mb-1">
                      <span className="text-muted">Policy Number:</span>
                      <strong className="font-monospace">{targetClaimForAssign.policyNumber}</strong>
                    </div>
                    <div className="d-flex justify-content-between">
                      <span className="text-muted">Claimed Damage:</span>
                      <strong className="text-dark">
                        ₹{Number(targetClaimForAssign.claimedAmount || 0).toLocaleString("en-IN")}
                      </strong>
                    </div>
                  </div>
                )}
                <label className="form-label small fw-bold text-uppercase text-secondary">
                  Select Certified Field Inspector <span className="text-danger">*</span>
                </label>
                <select
                  className="form-select"
                  value={selectedInspectorId}
                  onChange={(e) => setSelectedInspectorId(e.target.value)}
                  required
                >
                  <option value="">-- Choose Field Surveyor --</option>
                  {inspectors.map((insp) => (
                    <option key={insp.id} value={insp.id}>
                      {insp.name} {insp.contact ? `(📞 ${insp.contact})` : ""}
                    </option>
                  ))}
                </select>
              </div>
              <div className="modal-footer bg-light px-4 py-2">
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  data-bs-dismiss="modal"
                  disabled={assigning}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary btn-sm fw-semibold"
                  disabled={assigning || !selectedInspectorId}
                >
                  {assigning ? "Dispatching..." : "Assign & Create Task →"}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>

      {/* Modal 2: Adjudication Review Modal */}
      <div className="modal fade" id="reviewClaimModal" tabIndex="-1" aria-hidden="true">
        <div className="modal-dialog modal-dialog-centered modal-lg">
          <div className="modal-content border-0 shadow">
            <div className="modal-header bg-dark text-white px-4 py-3">
              <h5 className="modal-title fs-6 fw-bold">Claim Settlement Adjudication</h5>
              <button
                type="button"
                id="closeReviewModalBtn"
                className="btn-close btn-close-white"
                data-bs-dismiss="modal"
              ></button>
            </div>
            <form onSubmit={handleSubmitReview}>
              <div className="modal-body p-4">
                {targetClaimForReview && (
                  <div className="d-flex flex-column gap-3">
                    <div className="d-flex justify-content-between align-items-center p-3 bg-light rounded border">
                      <div>
                        <span className="text-muted small d-block">CLAIM IDENTIFIER</span>
                        <strong className="fs-6 text-primary font-monospace">{targetClaimForReview.claimNo}</strong>
                      </div>
                      <div className="text-end">
                        <span className="text-muted small d-block">CLAIMED AMOUNT</span>
                        <strong className="fs-6 text-dark">
                          ₹{Number(targetClaimForReview.claimedAmount || 0).toLocaleString("en-IN")}
                        </strong>
                      </div>
                    </div>

                    <div className="p-3 bg-light rounded border">
                      <span className="small text-muted d-block text-uppercase fw-bold mb-1">
                        Accident Circumstances
                      </span>
                      <p className="small text-dark fst-italic mb-0">"{targetClaimForReview.incidentDescription}"</p>
                    </div>

                    {/* Surveyor Technical Appraisal */}
                    {loadingAssessment ? (
                      <div className="text-center py-3 text-muted small">
                        <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                        Fetching surveyor assessment findings...
                      </div>
                    ) : isAssessmentReady ? (
                      <div className="p-3 border rounded bg-light border-primary-subtle">
                        <div className="d-flex justify-content-between align-items-center mb-2">
                          <span className="small fw-bold text-primary text-uppercase">
                            🕵️ Technical Surveyor Appraisal (
                            {claimAssessment.inspectorName || targetClaimForReview.inspectorName || "Surveyor"})
                          </span>
                          <span className="badge bg-primary">
                            Status: {claimAssessment.status || "COMPLETED"}
                          </span>
                        </div>
                        <div className="row g-2 mb-2">
                          <div className="col-6">
                            <span className="small text-muted d-block">Net Estimated Repair Cost:</span>
                            <strong className="text-dark fs-6">
                              ₹{Number(claimAssessment.estimatedCost || 0).toLocaleString("en-IN")}
                            </strong>
                          </div>
                          <div className="col-6">
                            <span className="small text-muted d-block">Evidence Photos:</span>
                            {claimAssessment.damagePhotosUrl ? (
                              <a
                                href={claimAssessment.damagePhotosUrl}
                                target="_blank"
                                rel="noreferrer"
                                className="btn btn-outline-primary btn-sm py-0 mt-1"
                                style={{ fontSize: "0.75rem" }}
                              >
                                Open Photos ↗
                              </a>
                            ) : (
                              <span className="text-muted small">No link attached</span>
                            )}
                          </div>
                        </div>
                        <span className="small text-muted d-block">Surveyor Observations:</span>
                        <p className="small text-dark mb-0">{claimAssessment.assessmentNotes || "No notes submitted."}</p>
                      </div>
                    ) : (
                      <div className="alert alert-warning py-3 small mb-0 d-flex align-items-start gap-2">
                        <span className="fs-5">⚠️</span>
                        <div>
                          <strong>Survey Assessment Pending:</strong> The assigned field surveyor (
                          <strong>{targetClaimForReview.inspectorName || "Field Surveyor"}</strong>) has not
                          submitted the damage assessment and repair appraisal yet.
                          <div className="text-muted mt-1">
                            Claims cannot be adjudicated or disbursed until the surveyor's appraisal report is submitted.
                          </div>
                        </div>
                      </div>
                    )}

                    {/* Decision Selection: Strictly disabled if assessment is missing */}
                    {targetClaimForReview.status === "UNDER_ASSESSMENT" && (
                      <div className={`p-3 border rounded ${!isAssessmentReady ? "bg-light-subtle opacity-75" : ""}`}>
                        <div className="d-flex justify-content-between align-items-center mb-3">
                          <h6 className="fw-bold text-dark small text-uppercase mb-0">Adjudication Verdict</h6>
                          {!isAssessmentReady && (
                            <span className="badge bg-secondary-subtle text-secondary small">
                              Disabled until survey submission
                            </span>
                          )}
                        </div>

                        <div className="row g-3">
                          <div className="col-6">
                            <label className="form-label small fw-bold text-secondary">Decision</label>
                            <select
                              className="form-select form-select-sm"
                              value={reviewDecision}
                              onChange={(e) => setReviewDecision(e.target.value)}
                              disabled={!isAssessmentReady}
                            >
                              <option value="APPROVED">APPROVE CLAIM</option>
                              <option value="REJECTED">REJECT CLAIM</option>
                            </select>
                          </div>
                          {reviewDecision === "APPROVED" && (
                            <div className="col-6">
                              <label className="form-label small fw-bold text-secondary">Approved Settlement (₹) *</label>
                              <input
                                type="number"
                                step="0.01"
                                min="1.00"
                                className="form-control form-control-sm"
                                value={approvedAmount}
                                onChange={(e) => setApprovedAmount(e.target.value)}
                                disabled={!isAssessmentReady}
                                required
                              />
                            </div>
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
              <div className="modal-footer bg-light px-4 py-2">
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  data-bs-dismiss="modal"
                  disabled={reviewing}
                >
                  Close
                </button>
                {targetClaimForReview?.status === "UNDER_ASSESSMENT" && (
                  <button
                    type="submit"
                    className={`btn btn-sm fw-semibold ${reviewDecision === "APPROVED" ? "btn-success" : "btn-danger"}`}
                    disabled={reviewing || !isAssessmentReady}
                  >
                    {reviewing ? "Submitting..." : !isAssessmentReady ? "Awaiting Survey Report" : `Confirm ${reviewDecision}`}
                  </button>
                )}
              </div>
            </form>
          </div>
        </div>
      </div>

      {/* Modal 3: Settlement Receipt Voucher Dialog */}
      <div className="modal fade" id="officerClaimSettlementReceiptModal" tabIndex="-1" aria-hidden="true">
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content border-0 shadow">
            <div className="modal-header bg-dark text-white px-4 py-3">
              <div>
                <h5 className="modal-title fs-6 fw-bold mb-0">Claim Settlement Voucher</h5>
                <small className="text-secondary font-monospace" style={{ fontSize: "0.75rem" }}>
                  Disbursement Ref: {receiptData?.settlementRef || "Processing..."}
                </small>
              </div>
              <button type="button" className="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>

            <div className="modal-body p-4">
              {loadingReceipt ? (
                <div className="text-center py-4 text-muted">
                  <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                  Generating official settlement voucher...
                </div>
              ) : receiptData ? (
                <div id="officerPrintableReceiptArea">
                  <div className="text-center pb-3 border-bottom mb-3">
                    <h5 className="fw-bold text-primary font-monospace mb-1">AutoSure Insurance Ltd.</h5>
                    <p className="text-muted small mb-0">Official Motor Claim Settlement & Discharge Voucher</p>
                  </div>

                  <div className="table-box p-3 bg-light rounded border mb-3 small">
                    <div className="d-flex justify-content-between mb-1">
                      <span className="text-muted">Claim Number:</span>
                      <strong className="font-monospace text-primary">{receiptData.claimNo}</strong>
                    </div>
                    <div className="d-flex justify-content-between mb-1">
                      <span className="text-muted">Policy Number:</span>
                      <strong className="font-monospace text-dark">{receiptData.policyNumber}</strong>
                    </div>
                    <div className="d-flex justify-content-between mb-1">
                      <span className="text-muted">Insured Customer:</span>
                      <strong>
                        {receiptData.customerName} ({receiptData.customerPhone})
                      </strong>
                    </div>
                    <div className="d-flex justify-content-between mb-1">
                      <span className="text-muted">Vehicle Registration:</span>
                      <strong className="font-monospace">{receiptData.registrationNo}</strong>
                    </div>
                    <div className="d-flex justify-content-between">
                      <span className="text-muted">Original Payment Gateway Ref:</span>
                      <span className="font-monospace small text-secondary">
                        {receiptData.originalPaymentTxnRef || "N/A"}
                      </span>
                    </div>
                  </div>

                  <div className="amount-box p-3 bg-success-subtle border border-success-subtle rounded text-center mb-3">
                    <span className="small text-success fw-bold d-block text-uppercase">Net Compensation Disbursed</span>
                    <strong className="fs-3 text-success">
                      ₹{Number(receiptData.approvedAmount || 0).toLocaleString("en-IN")}
                    </strong>
                    <div className="small text-muted mt-1 font-monospace" style={{ fontSize: "0.75rem" }}>
                      Settlement UTR: <strong>{receiptData.settlementRef}</strong>
                    </div>
                  </div>

                  <div className="d-flex justify-content-between small text-muted pt-2 border-top">
                    <span>
                      Authorized By: <strong>{receiptData.officerUsername || "Underwriting Desk"}</strong>
                    </span>
                    <span>
                      Date:{" "}
                      <strong>
                        {receiptData.settledDate ? receiptData.settledDate.replace("T", " ").substring(0, 16) : "Today"}
                      </strong>
                    </span>
                  </div>
                </div>
              ) : (
                <div className="text-danger small text-center">Receipt details currently unavailable.</div>
              )}
            </div>

            <div className="modal-footer bg-light px-4 py-2 d-flex justify-content-between">
              <button type="button" className="btn btn-secondary btn-sm" data-bs-dismiss="modal">
                Close
              </button>
              <button
                type="button"
                className="btn btn-primary btn-sm fw-semibold"
                disabled={!receiptData}
                onClick={handlePrintReceipt}
              >
                🖨️ Print / Save PDF
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default OfficerClaimSection;