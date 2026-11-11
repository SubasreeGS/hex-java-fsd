import { useEffect, useState, useCallback } from "react";
import { useParams } from "react-router";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import Swal from "sweetalert2";
import "../../styles/OfficerProposalSection.css";

function OfficerProposalSection() {
  const params = useParams();
  const token = params.token || localStorage.getItem("token");

  const [activeTab, setActiveTab] = useState("SUBMITTED");

  // Pagination State
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(5);
  const [totalPages, setTotalPages] = useState(0);
  const [totalRecords, setTotalRecords] = useState(0);

  const [proposals, setProposals] = useState([]);
  const [inspectors, setInspectors] = useState([]);
  const [loading, setLoading] = useState(true);

  // Inspector Assignment Modal State
  const [selectedProposalForAssign, setSelectedProposalForAssign] = useState(null);
  const [selectedInspectorId, setSelectedInspectorId] = useState("");
  const [assigning, setAssigning] = useState(false);

  // IDV Approval & Quote / Rejection Modal State
  const [selectedProposalForApproval, setSelectedProposalForApproval] = useState(null);
  const [inspectionDetails, setInspectionDetails] = useState(null);
  const [loadingInspection, setLoadingInspection] = useState(false);
  const [approvedIdv, setApprovedIdv] = useState("");
  const [processingQuote, setProcessingQuote] = useState(false);
  const [rejectionRemarks, setRejectionRemarks] = useState("");
  const [rejecting, setRejecting] = useState(false);

  // View Quote Details Modal State
  const [viewingQuote, setViewingQuote] = useState(null);
  const [loadingQuote, setLoadingQuote] = useState(false);

  // Fetch proposals by active workflow status
  const fetchProposalsByStatus = useCallback(async () => {
    setLoading(true);
    try {
      const statusParam =
        activeTab === "SUBMITTED"
          ? "PROPOSAL_SUBMITTED"
          : activeTab === "INSPECTION_COMPLETED"
          ? "INSPECTION_COMPLETED"
          : "QUOTE_GENERATED";

      const resp = await axios.get(
        `${API_SERVER_URL_DEV}/api/proposal/status/${statusParam}?page=${page}&size=${pageSize}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      if (resp.data) {
        const rawList = resp.data.list || [];
        const sortedList = [...rawList].sort((a, b) => (b.id || 0) - (a.id || 0));
        setProposals(sortedList);

        if (resp.data.paginationInfo) {
          setTotalPages(resp.data.paginationInfo.totalPages || 0);
          setTotalRecords(resp.data.paginationInfo.totalRecords || 0);
        }
      } else {
        setProposals([]);
      }
    } catch (err) {
      console.error("Error loading proposals queue:", err?.response || err);
      setProposals([]);
    } finally {
      setLoading(false);
    }
  }, [activeTab, page, pageSize, token]);

  // Fetch inspectors for assignment dropdown
  const fetchInspectors = async () => {
    try {
      const resp = await axios.get(
        `${API_SERVER_URL_DEV}/api/inspector/my-inspectors/dropdown`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      setInspectors(resp.data || []);
    } catch (err) {
      console.error("Error loading inspectors:", err?.response || err);
      setInspectors([]);
    }
  };

  useEffect(() => {
    fetchProposalsByStatus();
  }, [fetchProposalsByStatus]);

  useEffect(() => {
    fetchInspectors();
  }, [token]);

  const handleTabChange = (tabName) => {
    setActiveTab(tabName);
    setPage(0);
  };

  // Assign Surveyor Submission
  const handleAssignInspector = async (e) => {
    e.preventDefault();
    if (!selectedProposalForAssign || !selectedInspectorId) return;

    setAssigning(true);
    try {
      await axios.put(
        `${API_SERVER_URL_DEV}/api/proposal/assign/${selectedInspectorId}/${selectedProposalForAssign.id}`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const closeBtn = document.getElementById("closeAssignModalBtn");
      if (closeBtn) closeBtn.click();

      setPage(0);
      fetchProposalsByStatus();

      Swal.fire({
        title: "Surveyor Assigned!",
        text: `Proposal #${selectedProposalForAssign.id} transferred to the field inspection queue.`,
        icon: "success",
        confirmButtonColor: "#0f172a",
        timer: 2000,
      });

      setSelectedProposalForAssign(null);
      setSelectedInspectorId("");
    } catch (err) {
      Swal.fire({
        title: "Assignment Failed",
        text: err?.response?.data?.message || "Could not assign surveyor.",
        icon: "error",
      });
    } finally {
      setAssigning(false);
    }
  };

  // Open Approval / Adjudication Modal
  const handleOpenApprovalModal = async (proposal) => {
    setSelectedProposalForApproval(proposal);
    setInspectionDetails(null);
    setApprovedIdv("");
    setRejectionRemarks("");
    setLoadingInspection(true);

    try {
      const resp = await axios.get(
        `${API_SERVER_URL_DEV}/api/inspection/proposal/v1/${proposal.id}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      setInspectionDetails(resp.data);
      if (resp.data?.recommendedIdv && Number(resp.data.recommendedIdv) > 0) {
        setApprovedIdv(resp.data.recommendedIdv);
      }
    } catch (err) {
      console.error("Error loading inspection findings:", err);
      Swal.fire({
        title: "Data Unavailable",
        text: "Could not retrieve the inspector's pre-policy survey report.",
        icon: "error",
      });
    } finally {
      setLoadingInspection(false);
    }
  };

  // Case 1: Survey Passed -> Approve IDV & Issue Quote
  const handleApproveIdvAndAutoGenerateQuote = async (e) => {
    e.preventDefault();
    if (!selectedProposalForApproval || !approvedIdv) return;

    setProcessingQuote(true);
    const proposalId = selectedProposalForApproval.id;

    try {
      await axios.put(
        `${API_SERVER_URL_DEV}/api/proposal/approve-idv/${proposalId}`,
        { approvedIdv: Number(approvedIdv) },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      await axios.post(
        `${API_SERVER_URL_DEV}/api/quote/generate/${proposalId}`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const quoteRes = await axios.get(
        `${API_SERVER_URL_DEV}/api/quote/proposal/${proposalId}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const closeBtn = document.getElementById("closeApprovalModalBtn");
      if (closeBtn) closeBtn.click();

      setPage(0);
      fetchProposalsByStatus();

      const q = quoteRes.data;
      Swal.fire({
        title: "IDV Approved & Quote Issued!",
        html: `
          <div style="text-align: left; font-size: 0.85rem; padding: 4px;">
            <p style="margin-bottom: 6px;"><strong>Approved IDV:</strong> ₹${Number(approvedIdv).toLocaleString("en-IN")}</p>
            <p style="margin-bottom: 6px;"><strong>Third Party Cover:</strong> ₹${Number(q.tpPremium).toLocaleString("en-IN")}</p>
            <p style="margin-bottom: 6px;"><strong>Own Damage Cover:</strong> ₹${Number(q.odPremium).toLocaleString("en-IN")}</p>
            <p style="margin-bottom: 6px;"><strong>Add-on Covers:</strong> ₹${(Number(q.zeroDepPremium || 0) + Number(q.engineProtectPremium || 0) + Number(q.roadsideAssistPremium || 0)).toLocaleString("en-IN")}</p>
            <p style="margin-bottom: 6px;"><strong>GST (18%):</strong> ₹${Number(q.taxAmount).toLocaleString("en-IN")}</p>
            <hr style="margin: 8px 0;"/>
            <p style="font-size: 1rem; font-weight: bold; color: #166534; margin: 0;">
              Total Premium: ₹${Number(q.totalPremium).toLocaleString("en-IN")}
            </p>
          </div>
        `,
        icon: "success",
        confirmButtonColor: "#0f172a",
        confirmButtonText: "Done",
      });

      setSelectedProposalForApproval(null);
      setApprovedIdv("");
    } catch (err) {
      Swal.fire({
        title: "Underwriting Failed",
        text: err?.response?.data?.message || "Failed to finalize IDV and compute quote.",
        icon: "error",
      });
    } finally {
      setProcessingQuote(false);
    }
  };

  // Case 2: Survey Failed or Underwriting Issue -> Reject Proposal
  const handleRejectProposal = async () => {
    if (!selectedProposalForApproval) return;
    const remarks = rejectionRemarks.trim() || "Survey findings failed roadworthiness standards.";

    const confirm = await Swal.fire({
      title: "Reject Proposal?",
      text: `Are you sure you want to reject proposal #${selectedProposalForApproval.id}? This decision is final.`,
      icon: "warning",
      showCancelButton: true,
      confirmButtonText: "Yes, Reject Proposal",
      confirmButtonColor: "#dc2626",
      cancelButtonText: "Cancel",
    });

    if (!confirm.isConfirmed) return;

    setRejecting(true);
    try {
      await axios.put(
        `${API_SERVER_URL_DEV}/api/proposal/reject/${selectedProposalForApproval.id}`,
        { remarks },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const closeBtn = document.getElementById("closeApprovalModalBtn");
      if (closeBtn) closeBtn.click();

      Swal.fire({
        title: "Proposal Repudiated",
        text: `Proposal #${selectedProposalForApproval.id} has been formally rejected.`,
        icon: "info",
        confirmButtonColor: "#0f172a",
        timer: 2000,
      });

      setPage(0);
      fetchProposalsByStatus();
      setSelectedProposalForApproval(null);
    } catch (err) {
      Swal.fire({
        title: "Rejection Error",
        text: err?.response?.data?.message || "Failed to repudiate proposal.",
        icon: "error",
      });
    } finally {
      setRejecting(false);
    }
  };

  // View existing Quote
  const handleViewQuoteBreakdown = async (proposalId) => {
    setLoadingQuote(true);
    setViewingQuote(null);
    try {
      const resp = await axios.get(
        `${API_SERVER_URL_DEV}/api/quote/proposal/${proposalId}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setViewingQuote(resp.data);
    } catch (err) {
      Swal.fire({
        title: "Quote Not Found",
        text: "Could not retrieve the quote receipt for this proposal.",
        icon: "error",
      });
    } finally {
      setLoadingQuote(false);
    }
  };

  const isSurveyFailed = inspectionDetails?.result === "FAILED";

  return (
    <div className="officer-prop-panel">
      {/* Header */}
      <div className="officer-prop-header d-flex flex-wrap justify-content-between align-items-center gap-2">
        <div>
          <h2 className="h5 fw-bold text-dark mb-1">Underwriting Proposals & Policy Quotes</h2>
          <p className="text-muted small mb-0">
            Verify inspection surveys, approve IDV valuation, and issue actuarial premium quotes.
          </p>
        </div>
        <button
          className="btn btn-outline-secondary btn-sm"
          onClick={() => {
            setPage(0);
            fetchProposalsByStatus();
          }}
        >
          ↻ Refresh Queue
        </button>
      </div>

      {/* Workflow Tabs */}
      <div className="officer-workflow-nav">
        <button
          className={`officer-tab-btn ${activeTab === "SUBMITTED" ? "active" : ""}`}
          onClick={() => handleTabChange("SUBMITTED")}
        >
          <span>📋</span>
          <span>New Submissions (Assign Surveyor)</span>
        </button>
        <button
          className={`officer-tab-btn ${activeTab === "INSPECTION_COMPLETED" ? "active" : ""}`}
          onClick={() => handleTabChange("INSPECTION_COMPLETED")}
        >
          <span>⚖️</span>
          <span>Awaiting IDV & Quote</span>
        </button>
        <button
          className={`officer-tab-btn ${activeTab === "QUOTE_GENERATED" ? "active" : ""}`}
          onClick={() => handleTabChange("QUOTE_GENERATED")}
        >
          <span>🛡️</span>
          <span>Issued Quotes</span>
        </button>
      </div>

      {/* Main Content */}
      <div className="p-3 p-md-4">
        {loading ? (
          <div className="p-5 text-center text-muted">
            <div className="spinner-border spinner-border-sm text-primary me-2"></div>
            Loading underwriting records...
          </div>
        ) : proposals.length === 0 ? (
          <div className="p-5 text-center text-muted bg-light rounded-3 border">
            <div className="fs-2 mb-2">📑</div>
            <h6 className="fw-bold text-dark mb-1">Queue is Clear</h6>
            <p className="small text-muted mb-0">
              No applications currently pending in this underwriting stage.
            </p>
          </div>
        ) : (
          <div className="d-flex flex-column gap-3">
            {proposals.map((item) => (
              <div key={item.id} className="underwriting-card">
                <div className="d-flex flex-wrap justify-content-between align-items-center border-bottom pb-2 mb-3 gap-2">
                  <div className="d-flex align-items-center gap-2">
                    <span className="reg-badge">{item.registrationNo}</span>
                    <span className="fw-bold text-dark">Proposal #{item.id}</span>
                  </div>
                  <div className="d-flex align-items-center gap-2">
                    <span className="badge bg-light text-dark border px-2 py-1">
                      {item.policyType}
                    </span>
                    <span
                      className={`badge px-2 py-1 ${
                        activeTab === "QUOTE_GENERATED"
                          ? "bg-success-subtle text-success border border-success-subtle"
                          : activeTab === "INSPECTION_COMPLETED"
                          ? "bg-info-subtle text-info border border-info-subtle"
                          : "bg-warning-subtle text-dark border border-warning-subtle"
                      }`}
                    >
                      {item.status || activeTab}
                    </span>
                  </div>
                </div>

                <div className="row g-3 align-items-start">
                  <div className="col-12 col-md-4 card-divider-col">
                    <div className="field-caption">Applicant Profile</div>
                    <div className="fw-bold text-dark fs-6">{item.name}</div>
                    <div className="small text-secondary fw-semibold mt-1">
                      📞 {item.phoneNo}
                    </div>
                    {item.rcDocUrl ? (
                      <div className="mt-2">
                        <a
                          href={item.rcDocUrl}
                          target="_blank"
                          rel="noreferrer"
                          className="small text-decoration-none fw-semibold"
                        >
                          📄 View Vehicle RC Document ↗
                        </a>
                      </div>
                    ) : (
                      <div className="text-muted small mt-2">No RC document uploaded</div>
                    )}
                  </div>

                  <div className="col-12 col-md-4 card-divider-col">
                    <div className="field-caption">Vehicle Details</div>
                    <div className="fw-bold text-dark fs-6">
                      {item.vehicleMake} {item.vehicleModel}
                    </div>
                    <div className="small text-secondary mt-1">
                      {item.manufactureYear && (
                        <span className="me-2">Mfg: {item.manufactureYear}</span>
                      )}
                      <span>{item.fuelType || "Petrol/Diesel"}</span>
                    </div>
                    {item.vinNo && (
                      <div className="small text-muted font-monospace mt-1">
                        VIN: {item.vinNo}
                      </div>
                    )}
                  </div>

                  <div className="col-12 col-md-4 d-flex flex-column justify-content-between h-100">
                    <div>
                      <div className="field-caption">Requested Add-ons</div>
                      <div className="d-flex flex-wrap gap-1 mb-3">
                        {item.isZeroDep && (
                          <span className="badge bg-light text-dark border">Zero Dep</span>
                        )}
                        {item.isEngineProtect && (
                          <span className="badge bg-light text-dark border">Engine Protect</span>
                        )}
                        {item.isRoadsideAssist && (
                          <span className="badge bg-light text-dark border">24x7 RSA</span>
                        )}
                        {!item.isZeroDep && !item.isEngineProtect && !item.isRoadsideAssist && (
                          <span className="text-muted small">Standard Coverage</span>
                        )}
                      </div>
                    </div>

                    <div className="pt-2">
                      {activeTab === "SUBMITTED" && (
                        <button
                          className="btn btn-primary btn-sm w-100 fw-semibold"
                          data-bs-toggle="modal"
                          data-bs-target="#assignInspectorModal"
                          onClick={() => {
                            setSelectedProposalForAssign(item);
                            setSelectedInspectorId("");
                          }}
                        >
                          Assign Field Surveyor →
                        </button>
                      )}

                      {activeTab === "INSPECTION_COMPLETED" && (
                        <button
                          className="btn btn-success btn-sm w-100 fw-semibold"
                          data-bs-toggle="modal"
                          data-bs-target="#approveIdvModal"
                          onClick={() => handleOpenApprovalModal(item)}
                        >
                          Review Survey Findings →
                        </button>
                      )}

                      {activeTab === "QUOTE_GENERATED" && (
                        <button
                          className="btn btn-outline-dark btn-sm w-100 fw-semibold"
                          data-bs-toggle="modal"
                          data-bs-target="#viewQuoteModal"
                          onClick={() => handleViewQuoteBreakdown(item.id)}
                        >
                          View Quote Breakdown ↗
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Pagination Strip */}
      <div className="d-flex justify-content-between align-items-center flex-wrap px-4 py-3 border-top bg-white">
        <div className="text-muted small">
          Showing <strong>{totalRecords === 0 ? 0 : page * pageSize + 1}</strong> to{" "}
          <strong>{Math.min((page + 1) * pageSize, totalRecords)}</strong> of{" "}
          <strong>{totalRecords}</strong> proposals
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

      {/* Modal 1: Assign Field Surveyor (Dedicated) */}
      <div
        className="modal fade"
        id="assignInspectorModal"
        tabIndex="-1"
        aria-hidden="true"
        data-bs-backdrop="static"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content border-0 shadow">
            <div className="modal-header bg-dark text-white px-4 py-3">
              <h5 className="modal-title fs-6 fw-bold">
                Assign Field Surveyor (Proposal #{selectedProposalForAssign?.id})
              </h5>
              <button
                type="button"
                id="closeAssignModalBtn"
                className="btn-close btn-close-white"
                data-bs-dismiss="modal"
              ></button>
            </div>
            <form onSubmit={handleAssignInspector}>
              <div className="modal-body p-4">
                {selectedProposalForAssign && (
                  <div className="p-3 bg-light rounded border mb-3 small">
                    <div className="d-flex justify-content-between mb-1">
                      <span className="text-muted">Applicant:</span>
                      <strong className="text-dark">{selectedProposalForAssign.name}</strong>
                    </div>
                    <div className="d-flex justify-content-between mb-1">
                      <span className="text-muted">Vehicle Registration:</span>
                      <strong className="font-monospace text-primary">{selectedProposalForAssign.registrationNo}</strong>
                    </div>
                    <div className="d-flex justify-content-between">
                      <span className="text-muted">Vehicle Model:</span>
                      <span className="fw-semibold text-secondary">
                        {selectedProposalForAssign.vehicleMake} {selectedProposalForAssign.vehicleModel}
                      </span>
                    </div>
                  </div>
                )}
                <div className="mb-3">
                  <label className="form-label small fw-bold text-uppercase text-secondary">
                    Select Technical Field Inspector <span className="text-danger">*</span>
                  </label>
                  <select
                    className="form-select"
                    value={selectedInspectorId}
                    onChange={(e) => setSelectedInspectorId(e.target.value)}
                    required
                  >
                    <option value="">-- Choose Inspector from Directory --</option>
                    {inspectors.map((insp) => (
                      <option key={insp.id} value={insp.id}>
                        {insp.name} {insp.contact ? `(📞 ${insp.contact})` : ""}
                      </option>
                    ))}
                  </select>
                </div>
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
                  {assigning ? "Assigning..." : "Confirm Assignment →"}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>

      {/* Modal 2: Approve IDV & Issue Quote OR Repudiate Proposal */}
      <div
        className="modal fade"
        id="approveIdvModal"
        tabIndex="-1"
        aria-hidden="true"
        data-bs-backdrop="static"
      >
        <div className="modal-dialog modal-lg modal-dialog-centered">
          <div className="modal-content border-0 shadow">
            <div className="modal-header bg-dark text-white px-4 py-3">
              <h5 className="modal-title fs-6 fw-bold">
                Underwriting Adjudication Desk (Proposal #{selectedProposalForApproval?.id})
              </h5>
              <button
                type="button"
                id="closeApprovalModalBtn"
                className="btn-close btn-close-white"
                data-bs-dismiss="modal"
              ></button>
            </div>
            <form onSubmit={handleApproveIdvAndAutoGenerateQuote}>
              <div className="modal-body p-4">
                {loadingInspection ? (
                  <div className="text-center py-4 text-muted">
                    <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                    Fetching surveyor inspection findings...
                  </div>
                ) : (
                  <>
                    {selectedProposalForApproval && (
                      <div className="survey-report-preview p-3 mb-3 bg-light rounded border">
                        <div className="row g-2 small">
                          <div className="col-12 col-sm-6">
                            <span className="text-muted">Applicant:</span>{" "}
                            <strong>{selectedProposalForApproval.name}</strong>
                          </div>
                          <div className="col-12 col-sm-6">
                            <span className="text-muted">Vehicle:</span>{" "}
                            <strong className="text-primary font-monospace">
                              {selectedProposalForApproval.registrationNo}
                            </strong>{" "}
                            ({selectedProposalForApproval.vehicleMake} {selectedProposalForApproval.vehicleModel})
                          </div>
                        </div>
                      </div>
                    )}

                    {inspectionDetails && (
                      <div className={`border rounded p-3 mb-3 ${isSurveyFailed ? "bg-danger-subtle border-danger" : "bg-light"}`}>
                        <div className="d-flex justify-content-between align-items-center mb-2">
                          <span className="fw-bold small text-uppercase">
                            Technical Survey Summary
                          </span>
                          <span
                            className={`badge ${
                              inspectionDetails.result === "PASSED"
                                ? "bg-success text-white"
                                : "bg-danger text-white"
                            }`}
                          >
                            Outcome: {inspectionDetails.result}
                          </span>
                        </div>

                        <div className="row g-2 small">
                          <div className="col-12 col-sm-6">
                            <span className="text-muted">Recommended IDV:</span>{" "}
                            <strong>
                              ₹{Number(inspectionDetails.recommendedIdv || 0).toLocaleString("en-IN")}
                            </strong>
                          </div>
                          <div className="col-12 col-sm-6">
                            <span className="text-muted">Photos Archive:</span>{" "}
                            {inspectionDetails.inspectionPhotosUrl ? (
                              <a
                                href={inspectionDetails.inspectionPhotosUrl}
                                target="_blank"
                                rel="noreferrer"
                                className="fw-semibold text-decoration-none"
                              >
                                View Photos Archive ↗
                              </a>
                            ) : (
                              "None provided"
                            )}
                          </div>
                          <div className="col-12 mt-2">
                            <span className="text-muted d-block">Surveyor Findings:</span>
                            <span className="text-secondary fst-italic">
                              "{inspectionDetails.inspectionNotes || "No specific remarks recorded."}"
                            </span>
                          </div>
                        </div>
                      </div>
                    )}

                    {/* Conditional Form: PASSED vs FAILED */}
                    {!isSurveyFailed ? (
                      <div className="mb-3">
                        <label className="form-label small fw-semibold text-secondary">
                          Approved IDV Valuation (₹) <span className="text-danger">*</span>
                        </label>
                        <input
                          type="number"
                          className="form-control"
                          placeholder="e.g. 520000"
                          min="1000"
                          required
                          value={approvedIdv}
                          onChange={(e) => setApprovedIdv(e.target.value)}
                        />
                        <div className="form-text small">
                          Locks IDV and generates quote (TP + OD + Add-ons + 18% GST).
                        </div>
                      </div>
                    ) : (
                      <div className="p-3 border border-danger rounded bg-light mb-3">
                        <div className="d-flex align-items-center gap-2 text-danger mb-2">
                          <strong>⚠️ Vehicle Failed Pre-Policy Inspection</strong>
                        </div>
                        <p className="small text-muted mb-2">
                          The surveyor reported roadworthiness or structural discrepancies. Enter formal underwriting remarks below to repudiate this proposal.
                        </p>
                        <label className="form-label small fw-semibold text-secondary">
                          Officer Repudiation Remarks <span className="text-danger">*</span>
                        </label>
                        <textarea
                          className="form-control form-control-sm"
                          rows="2"
                          placeholder="State why this proposal is rejected (visible to customer)..."
                          value={rejectionRemarks}
                          onChange={(e) => setRejectionRemarks(e.target.value)}
                        ></textarea>
                      </div>
                    )}
                  </>
                )}
              </div>

              <div className="modal-footer bg-light px-4 py-2 d-flex justify-content-between">
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  data-bs-dismiss="modal"
                  disabled={processingQuote || rejecting}
                >
                  Cancel
                </button>

                {!isSurveyFailed ? (
                  <button
                    type="submit"
                    className="btn btn-success btn-sm fw-semibold"
                    disabled={processingQuote || !approvedIdv || loadingInspection}
                  >
                    {processingQuote ? "Calculating & Issuing..." : "Approve & Issue Quote →"}
                  </button>
                ) : (
                  <button
                    type="button"
                    className="btn btn-danger btn-sm fw-semibold"
                    onClick={handleRejectProposal}
                    disabled={rejecting || loadingInspection}
                  >
                    {rejecting ? "Rejecting..." : "✕ Formalize Proposal Rejection"}
                  </button>
                )}
              </div>
            </form>
          </div>
        </div>
      </div>

      {/* Modal 3: View Quote Breakdown */}
      <div className="modal fade" id="viewQuoteModal" tabIndex="-1" aria-hidden="true">
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content border-0 shadow">
            <div className="modal-header bg-dark text-white px-4 py-3">
              <h5 className="modal-title fs-6 fw-bold mb-0">Insurance Policy Quote Breakdown</h5>
              <button type="button" className="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div className="modal-body p-4">
              {loadingQuote ? (
                <div className="text-center py-4 text-muted">
                  <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                  Loading quote breakdown...
                </div>
              ) : viewingQuote ? (
                <div className="quote-summary-card">
                  <div className="quote-row">
                    <span className="text-muted">Third Party (TP) Cover:</span>
                    <strong className="text-dark">₹{Number(viewingQuote.tpPremium).toLocaleString("en-IN")}</strong>
                  </div>
                  <div className="quote-row">
                    <span className="text-muted">Own Damage (OD) Cover:</span>
                    <strong className="text-dark">₹{Number(viewingQuote.odPremium).toLocaleString("en-IN")}</strong>
                  </div>
                  <div className="quote-row">
                    <span className="text-muted">Zero Depreciation:</span>
                    <strong className="text-dark">₹{Number(viewingQuote.zeroDepPremium || 0).toLocaleString("en-IN")}</strong>
                  </div>
                  <div className="quote-row">
                    <span className="text-muted">Engine Protector:</span>
                    <strong className="text-dark">₹{Number(viewingQuote.engineProtectPremium || 0).toLocaleString("en-IN")}</strong>
                  </div>
                  <div className="quote-row">
                    <span className="text-muted">24x7 Roadside Assistance:</span>
                    <strong className="text-dark">₹{Number(viewingQuote.roadsideAssistPremium || 0).toLocaleString("en-IN")}</strong>
                  </div>
                  <div className="quote-row fw-bold text-dark pt-2">
                    <span>Net Base Premium:</span>
                    <span>₹{Number(viewingQuote.netPremium).toLocaleString("en-IN")}</span>
                  </div>
                  <div className="quote-row text-muted">
                    <span>GST (18%):</span>
                    <span>₹{Number(viewingQuote.taxAmount).toLocaleString("en-IN")}</span>
                  </div>

                  <div className="quote-total-box d-flex justify-content-between align-items-center mt-2 p-2 bg-success-subtle rounded border">
                    <span className="fw-bold text-success">Total Annual Premium:</span>
                    <span className="fs-5 fw-bold text-success">
                      ₹{Number(viewingQuote.totalPremium).toLocaleString("en-IN")}
                    </span>
                  </div>
                </div>
              ) : (
                <p className="text-muted text-center mb-0">No quote data available.</p>
              )}
            </div>
            <div className="modal-footer bg-light px-4 py-2">
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

export default OfficerProposalSection;