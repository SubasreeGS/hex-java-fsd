import { useEffect, useState, useCallback } from "react";
import { useParams } from "react-router";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import Swal from "sweetalert2";
import "../../styles/ClaimAssessmentSection.css";

function ClaimAssessmentSection() {
  const params = useParams();
  const token = params.token || localStorage.getItem("token");

  // Pagination State
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(5);
  const [totalPages, setTotalPages] = useState(0);
  const [totalRecords, setTotalRecords] = useState(0);

  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedTask, setSelectedTask] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");

  const submitAssessmentUrl = `${API_SERVER_URL_DEV}/api/assessment/submit/`;

  // Modal appraisal input states
  const [partsCost, setPartsCost] = useState("");
  const [laborCost, setLaborCost] = useState("");
  const [salvageDeduction, setSalvageDeduction] = useState("");
  const [damagePhotosUrl, setDamagePhotosUrl] = useState("");
  const [assessmentNotes, setAssessmentNotes] = useState("");

  // Live net estimate calculation
  const netEstimatedCost = Math.max(
    0,
    (parseFloat(partsCost) || 0) +
      (parseFloat(laborCost) || 0) -
      (parseFloat(salvageDeduction) || 0)
  );

  const fetchTasks = useCallback(async () => {
    setLoading(true);
    try {
      const tasksUrl = `${API_SERVER_URL_DEV}/api/assessment/inspector/my-tasks?page=${page}&size=${pageSize}`;
      const resp = await axios.get(tasksUrl, {
        headers: { Authorization: "Bearer " + token },
      });

      if (resp.data) {
        setTasks(resp.data.list || []);
        if (resp.data.paginationInfo) {
          setTotalPages(resp.data.paginationInfo.totalPages || 0);
          setTotalRecords(resp.data.paginationInfo.totalRecords || 0);
        }
      } else {
        setTasks([]);
      }
    } catch (err) {
      console.error("Failed to load surveyor claim tasks:", err);
      setTasks([]);
    } finally {
      setLoading(false);
    }
  }, [page, pageSize, token]);

  useEffect(() => {
    fetchTasks();
  }, [fetchTasks]);

  const openInspectionModal = (task) => {
    setSelectedTask(task);
    setPartsCost("");
    setLaborCost("");
    setSalvageDeduction("");
    setDamagePhotosUrl("");
    setAssessmentNotes("");
  };

  const handleSubmitAssessment = async (e) => {
    e.preventDefault();
    const assessmentRefId = selectedTask?.id || selectedTask?.assessmentId;
    if (!selectedTask || !assessmentRefId) return;

    if (netEstimatedCost <= 0) {
      Swal.fire({
        title: "Invalid Estimate",
        text: "Calculated net repair estimate must be greater than ₹0.",
        icon: "warning",
        confirmButtonColor: "#0f172a",
      });
      return;
    }

    setSubmitting(true);

    const breakdownText = `[Estimate Breakdown: Parts=₹${partsCost || 0}, Labor=₹${laborCost || 0}, Salvage Deduction=₹${salvageDeduction || 0}] - ${assessmentNotes.trim()}`;

    const payload = {
      estimatedCost: Number(netEstimatedCost),
      damagePhotosUrl: damagePhotosUrl.trim(),
      assessmentNotes: breakdownText,
    };

    try {
      await axios.put(submitAssessmentUrl + assessmentRefId, payload, {
        headers: { Authorization: "Bearer " + token },
      });

      const closeBtn = document.getElementById("closeClaimAssessmentModalBtn");
      if (closeBtn) closeBtn.click();

      Swal.fire({
        title: "Assessment Report Submitted!",
        text: `Claim ${selectedTask.claimNo} damage evaluation of ₹${netEstimatedCost.toLocaleString("en-IN")} recorded.`,
        icon: "success",
        confirmButtonColor: "#0f172a",
        timer: 2200,
        timerProgressBar: true,
      });

      fetchTasks();
    } catch (err) {
      const msg = err?.response?.data?.message || "Failed to submit surveyor assessment report.";
      Swal.fire({
        title: "Submission Error",
        text: msg,
        icon: "error",
        confirmButtonColor: "#dc2626",
      });
    } finally {
      setSubmitting(false);
    }
  };

  const filteredTasks = tasks.filter((t) => {
    const term = searchTerm.toLowerCase().trim();
    if (!term) return true;
    return (
      t.claimNo?.toLowerCase().includes(term) ||
      t.registrationNo?.toLowerCase().includes(term) ||
      t.customerName?.toLowerCase().includes(term) ||
      t.model?.toLowerCase().includes(term)
    );
  });

  return (
    <div className="inspector-panel">
      {/* Header Bar */}
      <div className="inspector-panel-header d-flex flex-wrap justify-content-between align-items-center gap-2">
        <div>
          <h2 className="h5 fw-bold text-dark mb-1">Assigned Claim Assessments</h2>
          <p className="text-muted small mb-0">
            Physical accident damage assessments assigned to your surveyor verification roster.
          </p>
        </div>
        <div className="d-flex align-items-center gap-2 flex-wrap">
          <input
            type="text"
            className="form-control form-control-sm"
            style={{ width: "230px" }}
            placeholder="Search claim, reg no, owner..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <button className="btn btn-outline-secondary btn-sm" onClick={fetchTasks}>
            Refresh Queue
          </button>
        </div>
      </div>

      {/* Main Content: Card-Job Grid View */}
      <div className="p-3 p-md-4">
        {loading ? (
          <div className="p-5 text-center text-muted">
            <div className="spinner-border spinner-border-sm text-primary me-2" role="status"></div>
            Loading assigned claim assessments...
          </div>
        ) : filteredTasks.length === 0 ? (
          <div className="p-5 text-center text-muted bg-light rounded-3 border">
            <div className="fs-1 mb-2">💥</div>
            <h6 className="fw-bold text-dark mb-1">No Claim Assessments Found</h6>
            <p className="small text-muted mb-0">
              {searchTerm
                ? "No accident survey tasks match your search filter."
                : "Your accident survey queue is clear for this page."}
            </p>
          </div>
        ) : (
          <div className="d-flex flex-column gap-3">
            {filteredTasks.map((item) => {
              const currentStatus = item.status || item.assessmentStatus || "PENDING";
              const isCompleted = currentStatus === "COMPLETED";
              const targetId = item.id || item.assessmentId;

              return (
                <div
                  key={targetId}
                  className={`survey-job-card ${isCompleted ? "border-success-subtle bg-light-subtle" : ""}`}
                >
                  {/* Top Meta Header */}
                  <div className="d-flex flex-wrap justify-content-between align-items-center border-bottom pb-2 mb-3 gap-2">
                    <div className="d-flex align-items-center gap-2 flex-wrap">
                      <span className="reg-no-badge">{item.registrationNo}</span>
                      <span className="fw-bold text-dark fs-6 font-monospace">{item.claimNo}</span>
                      <span className="text-muted small">Task #{targetId}</span>
                      {!isCompleted ? (
                        <span className="badge bg-warning text-dark px-2 py-1 small">
                          Assessment Pending
                        </span>
                      ) : (
                        <span className="badge bg-success text-white px-2 py-1 small">
                          ✓ Assessment Completed
                        </span>
                      )}
                    </div>
                    <div className="d-flex align-items-center gap-2">
                      <span className="claimed-amount-pill">
                        Claimed: ₹{Number(item.claimedAmount || 0).toLocaleString("en-IN")}
                      </span>
                      {item.incidentDate && (
                        <span className="text-muted small">
                          Incident: {item.incidentDate}
                        </span>
                      )}
                    </div>
                  </div>

                  {/* 3-Column Body Row */}
                  <div className="row g-3 align-items-start">
                    {/* Column 1: Applicant Details */}
                    <div className="col-12 col-md-4 survey-col survey-col-divider">
                      <div className="field-label mb-1">Insured Customer</div>
                      <div className="fw-bold text-dark fs-6">{item.customerName || "Customer on File"}</div>
                      <div className="small text-secondary fw-semibold mt-1">
                        📞 {item.customerPhone || "Not Disclosed"}
                      </div>
                      <div className="small text-muted mt-2 text-break">
                        📍 {item.customerAddress || "Registered Policyholder Address"}
                      </div>
                    </div>

                    {/* Column 2: Vehicle Specifications & Incident Statement */}
                    <div className="col-12 col-md-4 survey-col survey-col-divider">
                      <div className="field-label mb-1">Vehicle Specifications</div>
                      <div className="fw-bold text-dark fs-6">
                        {item.make} {item.model}
                      </div>
                      <div className="small text-secondary mt-1">
                        <strong>Mfg Year:</strong> {item.manufactureYear || "--"} •{" "}
                        <strong>Fuel:</strong> {item.fuelType || "PETROL"}
                      </div>
                      <div className="small text-muted font-monospace mt-1">
                        <strong>VIN:</strong> {item.vinNo || "--"}
                      </div>

                      {item.incidentDescription && (
                        <div className="mt-2 p-2 bg-light rounded border small fst-italic text-secondary text-truncate-2">
                          "{item.incidentDescription}"
                        </div>
                      )}
                    </div>

                    {/* Column 3: Appraisal Action Area */}
                    <div className="col-12 col-md-4 survey-col d-flex flex-column justify-content-between h-100">
                      <div>
                        <div className="field-label mb-2">Claim Damage Status</div>
                        <div className="small text-muted mb-3">
                          Total Claimed Impact:{" "}
                          <strong className="text-danger">
                            ₹{Number(item.claimedAmount || 0).toLocaleString("en-IN")}
                          </strong>
                        </div>
                      </div>

                      <div className="pt-2">
                        {!isCompleted ? (
                          <button
                            className="btn btn-primary w-100 fw-semibold shadow-sm"
                            data-bs-toggle="modal"
                            data-bs-target="#claimInspectionModal"
                            onClick={() => openInspectionModal(item)}
                          >
                            🛠️ Conduct Damage Appraisal →
                          </button>
                        ) : (
                          <div className="p-2 rounded bg-success-subtle text-success text-center border border-success-subtle">
                            <span className="fw-semibold small d-block">
                              ✓ Damage Report Dispatched
                            </span>
                            <span className="text-muted" style={{ fontSize: "0.75rem" }}>
                              Submitted to Claims Underwriting
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
          <strong>{totalRecords}</strong> claim tasks
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

      {/* Modal: Technical Claim Damage Appraisal Form */}
      <div
        className="modal fade"
        id="claimInspectionModal"
        tabIndex="-1"
        aria-hidden="true"
        data-bs-backdrop="static"
      >
        <div className="modal-dialog modal-lg modal-dialog-centered">
          <div className="modal-content border-0 shadow">
            <div className="modal-header bg-dark text-white px-4 py-3">
              <div>
                <h5 className="modal-title fs-6 fw-bold mb-0">
                  Technical Claim Damage Assessment
                </h5>
                <span className="small text-secondary font-monospace" style={{ fontSize: "0.75rem" }}>
                  Task Ref: #{selectedTask?.id || selectedTask?.assessmentId} • Claim {selectedTask?.claimNo}
                </span>
              </div>
              <button
                type="button"
                id="closeClaimAssessmentModalBtn"
                className="btn-close btn-close-white"
                data-bs-dismiss="modal"
                aria-label="Close"
                onClick={(e) => e.currentTarget.blur()}
              ></button>
            </div>

            <form onSubmit={handleSubmitAssessment}>
              <div className="modal-body p-4">
                {selectedTask && (
                  <div className="modal-vehicle-preview p-3 mb-4">
                    <div className="row g-2 small">
                      <div className="col-12 col-sm-6">
                        <span className="text-muted">Owner:</span>{" "}
                        <strong className="text-dark">
                          {selectedTask.customerName}
                        </strong>{" "}
                        ({selectedTask.customerPhone || "--"})
                      </div>
                      <div className="col-12 col-sm-6">
                        <span className="text-muted">Vehicle:</span>{" "}
                        <strong className="text-primary font-monospace">
                          {selectedTask.registrationNo}
                        </strong>{" "}
                        ({selectedTask.make} {selectedTask.model})
                      </div>
                      <div className="col-12 col-sm-6">
                        <span className="text-muted">Incident Date:</span>{" "}
                        <strong className="text-dark">
                          {selectedTask.incidentDate}
                        </strong>
                      </div>
                      <div className="col-12 col-sm-6">
                        <span className="text-muted">Claimed:</span>{" "}
                        <strong className="text-danger fw-bold">
                          ₹{Number(selectedTask.claimedAmount || 0).toLocaleString("en-IN")}
                        </strong>
                      </div>
                      {selectedTask.incidentDescription && (
                        <div className="col-12 mt-2 pt-2 border-top">
                          <span className="text-muted d-block mb-1">Reported Statement:</span>
                          <span className="fst-italic text-secondary">
                            "{selectedTask.incidentDescription}"
                          </span>
                        </div>
                      )}
                    </div>
                  </div>
                )}

                {/* Appraisal Breakdown */}
                <div className="cost-card p-3 mb-3 rounded border">
                  <span className="dossier-heading text-primary fw-bold small text-uppercase d-block mb-2">
                    Damage Appraisal Cost Breakdown
                  </span>
                  <div className="row g-3">
                    <div className="col-12 col-sm-6">
                      <label className="form-label small fw-semibold text-secondary mb-1">
                        Parts Replacement Cost (₹) <span className="text-danger">*</span>
                      </label>
                      <input
                        type="number"
                        min="0"
                        step="0.01"
                        className="form-control"
                        placeholder="e.g. 25000"
                        value={partsCost}
                        onChange={(e) => setPartsCost(e.target.value)}
                        required
                      />
                    </div>
                    <div className="col-12 col-sm-6">
                      <label className="form-label small fw-semibold text-secondary mb-1">
                        Labor / Repair Cost (₹) <span className="text-danger">*</span>
                      </label>
                      <input
                        type="number"
                        min="0"
                        step="0.01"
                        className="form-control"
                        placeholder="e.g. 6000"
                        value={laborCost}
                        onChange={(e) => setLaborCost(e.target.value)}
                        required
                      />
                    </div>
                    <div className="col-12">
                      <label className="form-label small fw-semibold text-secondary mb-1">
                        Scrap / Salvage Deduction (₹)
                      </label>
                      <input
                        type="number"
                        min="0"
                        step="0.01"
                        className="form-control"
                        placeholder="e.g. 1500"
                        value={salvageDeduction}
                        onChange={(e) => setSalvageDeduction(e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="net-estimate-banner d-flex justify-content-between align-items-center mt-3 p-2 bg-light rounded border">
                    <span className="small fw-bold text-dark">Calculated Net Estimate:</span>
                    <strong className="fs-5 text-primary">
                      ₹{netEstimatedCost.toLocaleString("en-IN")}
                    </strong>
                  </div>
                </div>

                {/* Proof & Findings */}
                <div className="row g-3">
                  <div className="col-12">
                    <label className="form-label small fw-semibold text-secondary mb-1">
                      Damage Photos Archive URL <span className="text-danger">*</span>
                    </label>
                    <input
                      type="url"
                      maxLength={500}
                      className="form-control"
                      placeholder="e.g. https://storage.autosure.com/claims/damages-tn30qr0123.zip"
                      value={damagePhotosUrl}
                      onChange={(e) => setDamagePhotosUrl(e.target.value)}
                      required
                    />
                  </div>

                  <div className="col-12">
                    <label className="form-label small fw-semibold text-secondary mb-1">
                      Surveyor Technical Findings & Remarks <span className="text-danger">*</span>
                    </label>
                    <textarea
                      className="form-control"
                      rows="3"
                      placeholder="Detail collision impact inspection, mechanical check, and verified parts damage..."
                      value={assessmentNotes}
                      onChange={(e) => setAssessmentNotes(e.target.value)}
                      required
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
                  {submitting ? "Submitting..." : "Submit Assessment Report →"}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ClaimAssessmentSection;