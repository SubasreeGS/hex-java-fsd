import { useEffect, useState, useCallback, useMemo } from "react";
import { useParams } from "react-router";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import Swal from "sweetalert2";
import "../../styles/InspectorProposalSection.css";

function InspectorProposalSection() {
  const params = useParams();
  const token = params.token || localStorage.getItem("token");

  // Pagination State
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(5);
  const [totalPages, setTotalPages] = useState(0);
  const [totalRecords, setTotalRecords] = useState(0);

  const [proposals, setProposals] = useState([]);
  const [scheduledIds, setScheduledIds] = useState(new Set());
  const [loading, setLoading] = useState(true);
  const [selectedProposal, setSelectedProposal] = useState(null);

  // Synchronized with SubmitInspectionReportDto
  const [inspectionData, setInspectionData] = useState({
    result: "PASSED",
    recommendedIdv: "",
    inspectionPhotosUrl: "",
    inspectionNotes: "",
  });
  const [submitting, setSubmitting] = useState(false);

  const fetchInspectionTasks = useCallback(async () => {
    setLoading(true);
    try {
      const inspectorProposalsUrl = `${API_SERVER_URL_DEV}/api/proposal/inspector?page=${page}&size=${pageSize}`;
      const scheduledInspectionsUrl = `${API_SERVER_URL_DEV}/api/inspection/status/v2/SCHEDULED`;

      const [proposalsRes, scheduledRes] = await Promise.allSettled([
        axios.get(inspectorProposalsUrl, {
          headers: { Authorization: `Bearer ${token}` },
        }),
        axios.get(scheduledInspectionsUrl, {
          headers: { Authorization: `Bearer ${token}` },
        }),
      ]);

      if (proposalsRes.status === "fulfilled" && proposalsRes.value.data) {
        const payload = proposalsRes.value.data;
        setProposals(payload.list || []);
        if (payload.paginationInfo) {
          setTotalPages(payload.paginationInfo.totalPages || 0);
          setTotalRecords(payload.paginationInfo.totalRecords || 0);
        }
      } else {
        setProposals([]);
      }

      const scheduledList =
        scheduledRes.status === "fulfilled" ? scheduledRes.value.data || [] : [];
      const scheduledSet = new Set(scheduledList.map((insp) => insp.proposalId));
      setScheduledIds(scheduledSet);
    } catch (err) {
      console.error("Error loading assigned surveys:", err?.response || err);
      setProposals([]);
    } finally {
      setLoading(false);
    }
  }, [page, pageSize, token]);

  useEffect(() => {
    fetchInspectionTasks();
  }, [fetchInspectionTasks]);

  // Priority Sort: Pending surveys first (newest IDs at top), completed pushed to bottom
  const prioritizedProposals = useMemo(() => {
    return [...proposals].sort((a, b) => {
      const isAPending = scheduledIds.has(a.id);
      const isBPending = scheduledIds.has(b.id);

      // Pending takes precedence over completed
      if (isAPending && !isBPending) return -1;
      if (!isAPending && isBPending) return 1;

      // Within the same group, show newest first
      return b.id - a.id;
    });
  }, [proposals, scheduledIds]);

  const handleOpenReportModal = (item) => {
    setSelectedProposal(item);
    setInspectionData({
      result: "PASSED",
      recommendedIdv: "",
      inspectionPhotosUrl: "",
      inspectionNotes: "",
    });
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setInspectionData((prev) => ({ ...prev, [name]: value }));
  };

  const handleReportSubmit = async (e) => {
    e.preventDefault();
    if (!selectedProposal) return;

    if (
      inspectionData.result === "PASSED" &&
      (!inspectionData.recommendedIdv || Number(inspectionData.recommendedIdv) <= 0)
    ) {
      Swal.fire({
        title: "Invalid IDV",
        text: "Please enter a valid Recommended IDV amount for a PASSED inspection.",
        icon: "warning",
        confirmButtonColor: "#0f172a",
      });
      return;
    }

    setSubmitting(true);

    try {
      const submitReportUrl = `${API_SERVER_URL_DEV}/api/inspection/submit/${selectedProposal.id}`;
      const payload = {
        result: inspectionData.result,
        recommendedIdv:
          inspectionData.result === "PASSED" ? Number(inspectionData.recommendedIdv) : null,
        inspectionPhotosUrl: inspectionData.inspectionPhotosUrl.trim(),
        inspectionNotes: inspectionData.inspectionNotes.trim(),
      };

      await axios.put(submitReportUrl, payload, {
        headers: { Authorization: `Bearer ${token}` },
      });

      const closeBtn = document.getElementById("closeInspectionReportModalBtn");
      if (closeBtn) closeBtn.click();

      setScheduledIds((prev) => {
        const next = new Set(prev);
        next.delete(selectedProposal.id);
        return next;
      });

      Swal.fire({
        title: "Survey Report Submitted!",
        text: `Pre-policy survey for proposal #${selectedProposal.id} recorded with status: ${inspectionData.result}.`,
        icon: "success",
        confirmButtonColor: "#0f172a",
        confirmButtonText: "OK",
        timer: 2500,
        timerProgressBar: true,
      });

      setSelectedProposal(null);
      fetchInspectionTasks();
    } catch (err) {
      const errorMsg =
        err?.response?.data?.message || "Failed to submit survey assessment report.";
      Swal.fire({
        title: "Submission Error",
        text: errorMsg,
        icon: "error",
        confirmButtonColor: "#dc2626",
      });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="inspector-panel">
      {/* Header Bar without 'Pending on page' pill */}
      <div className="inspector-panel-header d-flex flex-wrap justify-content-between align-items-center gap-2">
        <div>
          <h2 className="h5 fw-bold text-dark mb-1">Assigned Vehicle Inspections</h2>
          <p className="text-muted small mb-0">
            Physical pre-policy survey queue assigned to your technical verification roster.
          </p>
        </div>
        <div>
          <button
            className="btn btn-outline-secondary btn-sm fw-semibold shadow-sm px-3"
            onClick={fetchInspectionTasks}
          >
            ↻ Refresh Queue
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div className="p-3 p-md-4">
        {loading ? (
          <div className="p-5 text-center text-muted">
            <div className="spinner-border spinner-border-sm text-primary me-2" role="status"></div>
            Loading assigned vehicle survey tasks...
          </div>
        ) : prioritizedProposals.length === 0 ? (
          <div className="p-5 text-center text-muted bg-light rounded-3 border">
            <div className="fs-1 mb-2">🚗</div>
            <h6 className="fw-bold text-dark mb-1">No Inspections Assigned</h6>
            <p className="small text-muted mb-0">
              Your field survey queue is clear for this page.
            </p>
          </div>
        ) : (
          <div className="d-flex flex-column gap-3">
            {prioritizedProposals.map((item) => {
              const isPending = scheduledIds.has(item.id);

              return (
                <div
                  key={item.id}
                  className={`survey-job-card ${!isPending ? "border-success-subtle bg-light-subtle opacity-85" : "border-primary-subtle shadow-sm"}`}
                >
                  <div className="d-flex flex-wrap justify-content-between align-items-center border-bottom pb-2 mb-3 gap-2">
                    <div className="d-flex align-items-center gap-2">
                      <span className="reg-no-badge">{item.registrationNo}</span>
                      <span className="fw-bold text-dark fs-6">Proposal #{item.id}</span>
                      {isPending ? (
                        <span className="badge bg-warning text-dark px-2 py-1 small fw-semibold">
                          ⚡ Action Required (Pending)
                        </span>
                      ) : (
                        <span className="badge bg-success-subtle text-success border border-success-subtle px-2 py-1 small fw-semibold">
                          ✓ Survey Completed
                        </span>
                      )}
                    </div>
                    <div className="d-flex align-items-center gap-2">
                      <span className="plan-type-pill">{item.policyType}</span>
                      {item.rcDocUrl ? (
                        <a
                          href={item.rcDocUrl}
                          target="_blank"
                          rel="noreferrer"
                          className="btn btn-outline-primary btn-sm px-2 py-1 text-decoration-none fw-semibold"
                          style={{ fontSize: "0.78rem" }}
                        >
                          📄 View RC
                        </a>
                      ) : (
                        <span className="badge bg-secondary-subtle text-secondary border">
                          No RC Uploaded
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="row g-3 align-items-start">
                    <div className="col-12 col-md-4 survey-col survey-col-divider">
                      <div className="field-label mb-1">Applicant Contact</div>
                      <div className="fw-bold text-dark fs-6">{item.customerName}</div>
                      <div className="small text-secondary fw-semibold mt-1">
                        📞 {item.customerPhoneNo}
                      </div>
                      <div className="small text-muted mt-2 text-break">
                        📍 {item.customerAddress}
                      </div>
                    </div>

                    <div className="col-12 col-md-4 survey-col survey-col-divider">
                      <div className="field-label mb-1">Vehicle Specifications</div>
                      <div className="fw-bold text-dark fs-6">
                        {item.make} {item.model}
                      </div>
                      <div className="small text-secondary mt-1">
                        <strong>Mfg Year:</strong> {item.manufactureYear} •{" "}
                        <strong>Fuel:</strong> {item.fuelType}
                      </div>
                      <div className="small text-secondary mt-1">
                        <strong>Displacement:</strong> {item.engineCapacity} cc
                      </div>
                      <div className="small text-muted font-monospace mt-1">
                        <strong>VIN:</strong> {item.vinNo}
                      </div>
                    </div>

                    <div className="col-12 col-md-4 survey-col d-flex flex-column justify-content-between h-100">
                      <div>
                        <div className="field-label mb-2">Requested Add-on Covers</div>
                        <div className="d-flex flex-wrap gap-1 mb-3">
                          {item.isZeroDep && (
                            <span className="badge bg-info-subtle text-info border border-info-subtle">
                              Zero Depreciation
                            </span>
                          )}
                          {item.isEngineProtect && (
                            <span className="badge bg-warning-subtle text-dark border border-warning-subtle">
                              Engine Protect
                            </span>
                          )}
                          {item.isRoadsideAssist && (
                            <span className="badge bg-secondary-subtle text-secondary border">
                              24x7 RSA
                            </span>
                          )}
                          {!item.isZeroDep && !item.isEngineProtect && !item.isRoadsideAssist && (
                            <span className="text-muted small">Standard Coverage (No add-ons)</span>
                          )}
                        </div>
                      </div>

                      <div className="pt-2">
                        {isPending ? (
                          <button
                            className="btn btn-primary w-100 fw-semibold shadow-sm py-2"
                            data-bs-toggle="modal"
                            data-bs-target="#submitReportModal"
                            onClick={() => handleOpenReportModal(item)}
                          >
                            Conduct Physical Survey →
                          </button>
                        ) : (
                          <div className="p-2 rounded bg-success-subtle text-success text-center border border-success-subtle">
                            <span className="fw-semibold small d-block">
                              ✓ Survey Report Submitted
                            </span>
                            <span className="text-muted" style={{ fontSize: "0.75rem" }}>
                              Dispatched to Underwriting Desk
                            </span>
                          </div>
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
          <strong>{totalRecords}</strong> assigned surveys
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

      {/* Conduct Survey Assessment Modal */}
      <div
        className="modal fade"
        id="submitReportModal"
        tabIndex="-1"
        aria-hidden="true"
        data-bs-backdrop="static"
      >
        <div className="modal-dialog modal-lg">
          <div className="modal-content border-0 shadow">
            <div className="modal-header modal-survey-header">
              <h5 className="modal-title fs-6 fw-bold">
                Vehicle Physical Survey Report (Proposal #{selectedProposal?.id})
              </h5>
              <button
                type="button"
                id="closeInspectionReportModalBtn"
                className="btn-close"
                data-bs-dismiss="modal"
                onClick={(e) => e.currentTarget.blur()}
              ></button>
            </div>

            <form onSubmit={handleReportSubmit}>
              <div className="modal-body p-4">
                {selectedProposal && (
                  <div className="modal-vehicle-preview p-3 mb-4">
                    <div className="row g-2 small">
                      <div className="col-12 col-sm-6">
                        <span className="text-muted">Applicant:</span>{" "}
                        <strong className="text-dark">
                          {selectedProposal.customerName}
                        </strong>{" "}
                        ({selectedProposal.customerPhoneNo})
                      </div>
                      <div className="col-12 col-sm-6">
                        <span className="text-muted">Registration:</span>{" "}
                        <strong className="text-primary">
                          {selectedProposal.registrationNo}
                        </strong>
                      </div>
                      <div className="col-12 col-sm-6">
                        <span className="text-muted">Model:</span>{" "}
                        <strong className="text-dark">
                          {selectedProposal.make} {selectedProposal.model} (
                          {selectedProposal.manufactureYear})
                        </strong>
                      </div>
                      <div className="col-12 col-sm-6">
                        <span className="text-muted">VIN / Chassis:</span>{" "}
                        <strong className="font-monospace text-secondary">
                          {selectedProposal.vinNo}
                        </strong>
                      </div>
                    </div>
                  </div>
                )}

                <div className="row g-3">
                  <div className="col-12 col-sm-6">
                    <label className="form-label small fw-semibold text-secondary">
                      Inspection Outcome <span className="text-danger">*</span>
                    </label>
                    <select
                      name="result"
                      className="form-select"
                      required
                      value={inspectionData.result}
                      onChange={handleInputChange}
                    >
                      <option value="PASSED">PASSED (Vehicle Roadworthy)</option>
                      <option value="FAILED">FAILED (Major Discrepancy)</option>
                    </select>
                  </div>

                  <div className="col-12 col-sm-6">
                    <label className="form-label small fw-semibold text-secondary">
                      Recommended IDV (₹){" "}
                      {inspectionData.result === "PASSED" && (
                        <span className="text-danger">*</span>
                      )}
                    </label>
                    <input
                      type="number"
                      name="recommendedIdv"
                      className="form-control"
                      placeholder={
                        inspectionData.result === "PASSED"
                          ? "e.g. 525000"
                          : "Disabled for FAILED"
                      }
                      min="1000"
                      disabled={inspectionData.result === "FAILED"}
                      required={inspectionData.result === "PASSED"}
                      value={
                        inspectionData.result === "PASSED"
                          ? inspectionData.recommendedIdv
                          : ""
                      }
                      onChange={handleInputChange}
                    />
                  </div>

                  <div className="col-12">
                    <label className="form-label small fw-semibold text-secondary">
                      Inspection Photos Archive URL <span className="text-danger">*</span>
                    </label>
                    <input
                      type="url"
                      name="inspectionPhotosUrl"
                      className="form-control"
                      placeholder="e.g. https://storage.autosure.com/inspections/photos-tn30qr0123.zip"
                      required
                      value={inspectionData.inspectionPhotosUrl}
                      onChange={handleInputChange}
                    />
                  </div>

                  <div className="col-12">
                    <label className="form-label small fw-semibold text-secondary">
                      Surveyor Technical Findings & Remarks
                    </label>
                    <textarea
                      name="inspectionNotes"
                      rows="3"
                      className="form-control"
                      placeholder="Detail body condition, engine bay check, odometer reading, tyre condition..."
                      maxLength={4000}
                      value={inspectionData.inspectionNotes}
                      onChange={handleInputChange}
                    ></textarea>
                  </div>
                </div>
              </div>

              <div className="modal-footer bg-light">
                <button
                  type="button"
                  className="btn btn-outline-secondary btn-sm"
                  data-bs-dismiss="modal"
                  disabled={submitting}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary btn-sm fw-semibold"
                  disabled={submitting}
                >
                  {submitting ? "Submitting..." : "Submit Inspection Report"}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}

export default InspectorProposalSection;