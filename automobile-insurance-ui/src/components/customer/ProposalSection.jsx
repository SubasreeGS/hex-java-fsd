import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import Swal from "sweetalert2";
import "../../styles/ProposalSection.css";
import { parseBackendError } from "../../utils/errorHandler";

function ProposalSection() {
  const navigate = useNavigate();
  const params = useParams();
  const token = params.token || localStorage.getItem("token");

  const proposalsUrl = API_SERVER_URL_DEV + "/api/proposal/customer/v2";
  const submitProposalUrl = API_SERVER_URL_DEV + "/api/proposal/submit";

  const [proposals, setProposals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [errMsg, setErrMsg] = useState("");

  // Rejection Details Modal State
  const [selectedRejectedProposal, setSelectedRejectedProposal] = useState(null);

  const [formData, setFormData] = useState({
    registrationNo: "",
    rcDocUrl: "",
    policyType: "COMPREHENSIVE",
    isZeroDep: false,
    isEngineProtect: false,
    isRoadsideAssist: false,
  });

  const fetchProposals = async () => {
    setLoading(true);
    try {
      const resp = await axios.get(proposalsUrl, {
        headers: { Authorization: "Bearer " + token },
      });
      setProposals(resp.data || []);
    } catch (err) {
      setProposals([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProposals();
  }, [token]);

  const resetForm = () => {
    setFormData({
      registrationNo: "",
      rcDocUrl: "",
      policyType: "COMPREHENSIVE",
      isZeroDep: false,
      isEngineProtect: false,
      isRoadsideAssist: false,
    });
    setErrMsg("");
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;

    if (name === "policyType") {
      setFormData((prev) => ({
        ...prev,
        policyType: value,
        isZeroDep: value === "THIRD_PARTY" ? false : prev.isZeroDep,
        isEngineProtect: value === "THIRD_PARTY" ? false : prev.isEngineProtect,
        isRoadsideAssist: value === "THIRD_PARTY" ? false : prev.isRoadsideAssist,
      }));
      return;
    }

    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmitProposal = async (e) => {
    e.preventDefault();
    setErrMsg("");
    setSubmitting(true);

    const payload = {
      registrationNo: formData.registrationNo.trim().toUpperCase(),
      rcDocUrl: formData.rcDocUrl.trim(),
      policyType: formData.policyType,
      isZeroDep: formData.policyType === "COMPREHENSIVE" ? formData.isZeroDep : false,
      isEngineProtect: formData.policyType === "COMPREHENSIVE" ? formData.isEngineProtect : false,
      isRoadsideAssist: formData.policyType === "COMPREHENSIVE" ? formData.isRoadsideAssist : false,
    };

    try {
      await axios.post(submitProposalUrl, payload, {
        headers: { Authorization: "Bearer " + token },
      });

      const closeBtn = document.getElementById("closeProposalModalBtn");
      if (closeBtn) closeBtn.click();

      resetForm();
      fetchProposals();

      Swal.fire({
        title: "Proposal Submitted!",
        text: "Your proposal has been successfully registered.",
        icon: "success",
        confirmButtonColor: "#0f172a",
        confirmButtonText: "OK",
        timer: 2000,
        timerProgressBar: true,
      });
    } catch (err) {
      const backendMessage = parseBackendError(err, "Failed to submit insurance proposal.");
      setErrMsg(backendMessage);

      Swal.fire({
        title: "Submission Error",
        text: backendMessage,
        icon: "error",
        confirmButtonColor: "#dc2626",
      });
    } finally {
      setSubmitting(false);
    }
  };

  const renderStatusBadge = (status) => {
    switch (status) {
      case "PROPOSAL_SUBMITTED":
      case "UNDER_REVIEW":
        return <span className="badge badge-status-submitted px-2 py-1">PROPOSAL SUBMITTED</span>;
      case "INSPECTOR_ASSIGNED":
        return <span className="badge badge-status-progress px-2 py-1">SURVEYOR ASSIGNED</span>;
      case "INSPECTION_COMPLETED":
      case "IDV_APPROVED":
        return <span className="badge bg-info-subtle text-info border border-info-subtle px-2 py-1">IDV APPROVED</span>;
      case "QUOTE_GENERATED":
      case "PAYMENT_PENDING":
        return <span className="badge bg-warning-subtle text-dark border border-warning px-2 py-1">QUOTE READY</span>;
      case "POLICY_ISSUED":
        return <span className="badge bg-success-subtle text-success border border-success-subtle px-2 py-1 fw-semibold">✓ POLICY ISSUED</span>;
      case "REJECTED":
        return (
          <span className="badge bg-danger-subtle text-danger border border-danger-subtle px-2 py-1 fw-semibold">
            ✕ REJECTED
          </span>
        );
      default:
        return <span className="badge bg-secondary px-2 py-1">{status}</span>;
    }
  };

  // Contextual Action based strictly on lifecycle status
  const renderActionColumn = (item) => {
    // 1. Rejected Lifecycle Path: View Underwriter Repudiation Reason
    if (item.status === "REJECTED") {
      return (
        <button
          className="btn btn-outline-danger btn-sm fw-semibold text-nowrap px-2 py-1"
          style={{ fontSize: "0.78rem" }}
          data-bs-toggle="modal"
          data-bs-target="#proposalRejectionModal"
          onClick={() => setSelectedRejectedProposal(item)}
        >
          ✕ Repudiation Notice
        </button>
      );
    }

    // 2. Active Policy Ready
    if (item.status === "POLICY_ISSUED") {
      return (
        <button
          className="btn btn-outline-primary btn-sm fw-semibold text-nowrap"
          onClick={() => navigate(`/customer/${token}/policies`)}
        >
          View Policy →
        </button>
      );
    }

    // 3. Ready for Premium Payment
    if (item.status === "QUOTE_GENERATED" || item.status === "PAYMENT_PENDING") {
      return (
        <button
          className="btn btn-success btn-sm fw-semibold text-nowrap"
          onClick={() => navigate(`/customer/${token}/quotes-payment`)}
        >
          💳 View Quote & Pay
        </button>
      );
    }

    // 4. Survey Completed - IDV Computed
    if (item.status === "IDV_APPROVED" || item.status === "INSPECTION_COMPLETED") {
      return (
        <span className="badge bg-light text-primary border border-primary-subtle px-2 py-1 small">
          ⚡ Calculating Quote
        </span>
      );
    }

    // 5. Normal In-Review Progress
    return <span className="text-muted small">🔍 Technical Verification</span>;
  };

  return (
    <div className="proposal-panel">
      {/* Header */}
      <div className="proposal-panel-header d-flex justify-content-between align-items-center flex-wrap gap-2">
        <div>
          <h2 className="h5 fw-bold text-dark mb-1">Insurance Proposals</h2>
          <p className="text-muted small mb-0">
            Submit vehicle insurance proposals and track their underwriting status.
          </p>
        </div>
        <button
          className="btn btn-primary btn-sm fw-semibold px-4 py-2 text-nowrap d-inline-flex align-items-center gap-1 shadow-sm"
          data-bs-toggle="modal"
          data-bs-target="#newProposalModal"
          onClick={resetForm}
          style={{ minWidth: "max-content", whiteSpace: "nowrap" }}
        >
          <span>+</span> Submit New Proposal
        </button>
      </div>

      {/* Table */}
      <div className="p-0">
        {loading ? (
          <div className="p-5 text-center text-muted">
            <div className="spinner-border spinner-border-sm text-primary me-2" role="status"></div>
            Loading submitted proposals...
          </div>
        ) : proposals.length === 0 ? (
          <div className="p-5 text-center text-muted">
            <p className="mb-2">No insurance proposals found.</p>
            <button
              className="btn btn-outline-primary btn-sm"
              data-bs-toggle="modal"
              data-bs-target="#newProposalModal"
              onClick={resetForm}
            >
              Submit Your First Proposal
            </button>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="proposal-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Registration No</th>
                  <th>Plan Type</th>
                  <th>Add-ons</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {proposals.map((item, idx) => (
                  <tr key={item.id || idx}>
                    <td className="fw-semibold text-secondary">#{item.id || idx + 1}</td>
                    <td className="fw-bold text-dark font-monospace">{item.registrationNo}</td>
                    <td>
                      <span className="fw-semibold text-primary">
                        {item.policyType === "THIRD_PARTY" ? "Third Party Only" : "Comprehensive"}
                      </span>
                    </td>
                    <td>
                      {item.policyType === "THIRD_PARTY" ? (
                        <span className="text-muted small">None</span>
                      ) : (
                        <div className="d-flex flex-wrap gap-1">
                          {item.isZeroDep && (
                            <span className="badge bg-info-subtle text-info border border-info-subtle small">
                              Zero Dep
                            </span>
                          )}
                          {item.isEngineProtect && (
                            <span className="badge bg-primary-subtle text-primary border border-primary-subtle small">
                              Engine Protect
                            </span>
                          )}
                          {item.isRoadsideAssist && (
                            <span className="badge bg-secondary-subtle text-secondary border border-secondary-subtle small">
                              24x7 RSA
                            </span>
                          )}
                          {!item.isZeroDep && !item.isEngineProtect && !item.isRoadsideAssist && (
                            <span className="text-muted small">Standard</span>
                          )}
                        </div>
                      )}
                    </td>
                    <td>{renderStatusBadge(item.status)}</td>
                    <td>{renderActionColumn(item)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal 1: Submit New Proposal */}
      <div
        className="modal fade"
        id="newProposalModal"
        tabIndex="-1"
        aria-labelledby="newProposalModalLabel"
        aria-hidden="true"
        data-bs-backdrop="static"
        data-bs-keyboard="false"
      >
        <div className="modal-dialog modal-lg">
          <div className="modal-content border-0 shadow">
            <div className="modal-header modal-header-primary">
              <h1 className="modal-title fs-5" id="newProposalModalLabel">
                Submit Automobile Insurance Proposal
              </h1>
              <button
                type="button"
                id="closeProposalModalBtn"
                className="btn-close"
                data-bs-dismiss="modal"
                aria-label="Close"
                onClick={(e) => e.currentTarget.blur()}
              ></button>
            </div>

            <form onSubmit={handleSubmitProposal}>
              <div className="modal-body p-4">
                {errMsg && <div className="alert alert-danger py-2 small mb-3">{errMsg}</div>}

                <div className="row g-3">
                  <div className="col-12 col-md-6">
                    <label className="form-label-bold">Vehicle Registration Number</label>
                    <input
                      type="text"
                      name="registrationNo"
                      className="form-control"
                      placeholder="e.g. TN-30-AB-1234"
                      value={formData.registrationNo}
                      onChange={handleInputChange}
                      required
                    />
                  </div>

                  <div className="col-12 col-md-6">
                    <label className="form-label-bold">RC Document Verification URL</label>
                    <input
                      type="url"
                      name="rcDocUrl"
                      className="form-control"
                      placeholder="https://example.com/rc_document.pdf"
                      value={formData.rcDocUrl}
                      onChange={handleInputChange}
                      required
                    />
                  </div>

                  <div className="col-12">
                    <label className="form-label-bold">Policy Coverage Type</label>
                    <select
                      name="policyType"
                      className="form-select"
                      value={formData.policyType}
                      onChange={handleInputChange}
                      required
                    >
                      <option value="COMPREHENSIVE">COMPREHENSIVE</option>
                      <option value="THIRD_PARTY">THIRD_PARTY</option>
                    </select>
                  </div>

                  {formData.policyType === "COMPREHENSIVE" ? (
                    <div className="col-12 mt-3">
                      <div className="addon-card-container">
                        <span className="fw-bold text-dark small text-uppercase d-block mb-2">
                          Comprehensive Add-ons
                        </span>

                        <div className="row g-2">
                          <div className="col-12 col-md-4">
                            <div className="addon-toggle-box">
                              <div className="form-check">
                                <input
                                  className="form-check-input"
                                  type="checkbox"
                                  id="zeroDepCheck"
                                  name="isZeroDep"
                                  checked={formData.isZeroDep}
                                  onChange={handleInputChange}
                                />
                                <label className="form-check-label fw-bold text-dark small" htmlFor="zeroDepCheck">
                                  Zero Depreciation
                                </label>
                              </div>
                              <span className="addon-info-text mt-1">
                                No deduction on replacement of parts.
                              </span>
                            </div>
                          </div>

                          <div className="col-12 col-md-4">
                            <div className="addon-toggle-box">
                              <div className="form-check">
                                <input
                                  className="form-check-input"
                                  type="checkbox"
                                  id="engineProtectCheck"
                                  name="isEngineProtect"
                                  checked={formData.isEngineProtect}
                                  onChange={handleInputChange}
                                />
                                <label className="form-check-label fw-bold text-dark small" htmlFor="engineProtectCheck">
                                  Engine Protection
                                </label>
                              </div>
                              <span className="addon-info-text mt-1">
                                Coverage for internal engine damage.
                              </span>
                            </div>
                          </div>

                          <div className="col-12 col-md-4">
                            <div className="addon-toggle-box">
                              <div className="form-check">
                                <input
                                  className="form-check-input"
                                  type="checkbox"
                                  id="rsaCheck"
                                  name="isRoadsideAssist"
                                  checked={formData.isRoadsideAssist}
                                  onChange={handleInputChange}
                                />
                                <label className="form-check-label fw-bold text-dark small" htmlFor="rsaCheck">
                                  Roadside Assistance
                                </label>
                              </div>
                              <span className="addon-info-text mt-1">
                                24x7 emergency roadside breakdown support.
                              </span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div className="col-12 mt-2">
                      <div className="alert alert-light border small text-muted mb-0">
                        Add-ons are not available for Third-Party policies. Select <strong>COMPREHENSIVE</strong> to add coverage options.
                      </div>
                    </div>
                  )}
                </div>
              </div>

              <div className="modal-footer bg-light">
                <button
                  type="button"
                  className="btn btn-outline-secondary btn-sm px-3"
                  data-bs-dismiss="modal"
                  onClick={(e) => e.currentTarget.blur()}
                  disabled={submitting}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary btn-sm px-3 fw-semibold"
                  disabled={submitting}
                >
                  {submitting ? "Submitting..." : "Submit Proposal"}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>

      {/* Modal 2: Proposal Rejection Notice (Customer View) */}
      <div
        className="modal fade"
        id="proposalRejectionModal"
        tabIndex="-1"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content border-0 shadow">
            <div className="modal-header bg-dark text-white px-4 py-3">
              <h5 className="modal-title fs-6 fw-bold mb-0">Proposal Repudiation Notice</h5>
              <button
                type="button"
                className="btn-close btn-close-white"
                data-bs-dismiss="modal"
              ></button>
            </div>
            <div className="modal-body p-4">
              {selectedRejectedProposal ? (
                <div className="d-flex flex-column gap-3">
                  <div className="d-flex justify-content-between align-items-center pb-2 border-bottom">
                    <div>
                      <span className="text-muted small d-block">PROPOSAL REFERENCE</span>
                      <strong className="fs-6 text-primary font-monospace">
                        #{selectedRejectedProposal.id}
                      </strong>
                    </div>
                    <span className="badge bg-danger-subtle text-danger border border-danger-subtle px-2 py-1 fw-semibold">
                      ✕ REJECTED
                    </span>
                  </div>

                  <div className="p-3 bg-light rounded border small">
                    <div className="d-flex justify-content-between mb-1">
                      <span className="text-muted">Vehicle Registration:</span>
                      <strong className="font-monospace text-dark">
                        {selectedRejectedProposal.registrationNo}
                      </strong>
                    </div>
                    <div className="d-flex justify-content-between">
                      <span className="text-muted">Coverage Requested:</span>
                      <span className="fw-semibold text-secondary">
                        {selectedRejectedProposal.policyType}
                      </span>
                    </div>
                  </div>

                  {/* Underwriting Repudiation Remarks Box */}
                  <div className="p-3 border border-danger-subtle bg-danger-subtle rounded">
                    <span className="small text-danger fw-bold text-uppercase d-block mb-1">
                      Official Underwriting Repudiation Rationale:
                    </span>
                    <p className="small text-dark mb-0 fw-medium">
                      "{
                        (selectedRejectedProposal.rejectionRemarks && selectedRejectedProposal.rejectionRemarks.trim() !== "")
                          ? selectedRejectedProposal.rejectionRemarks
                          : (selectedRejectedProposal.remarks && selectedRejectedProposal.remarks.trim() !== "")
                          ? selectedRejectedProposal.remarks
                          : (selectedRejectedProposal.comments && selectedRejectedProposal.comments.trim() !== "")
                          ? selectedRejectedProposal.comments
                          : "The application was repudiated after technical survey due to physical discrepancy or non-compliance with underwriting roadworthiness norms."
                      }"
                    </p>
                  </div>

                  <div className="alert alert-secondary small text-muted mb-0 py-2">
                    ℹ️ If you believe this evaluation is erroneous, please submit a fresh proposal with updated vehicle documentation and valid verification details.
                  </div>
                </div>
              ) : null}
            </div>
            <div className="modal-footer bg-light px-4 py-2">
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                data-bs-dismiss="modal"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProposalSection;