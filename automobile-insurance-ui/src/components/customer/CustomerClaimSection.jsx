import { useEffect, useState } from "react";
import { useParams } from "react-router";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import Swal from "sweetalert2";
import "../../styles/CustomerClaimSection.css";

function CustomerClaimSection() {
  const params = useParams();
  const token = params.token || localStorage.getItem("token");

  const myClaimsUrl = API_SERVER_URL_DEV + "/api/claim/customer/my-claims/v4";
  const myPoliciesUrl = API_SERVER_URL_DEV + "/api/policy/customer/my-policies/v4";
  const policyByNumberUrl = API_SERVER_URL_DEV + "/api/policy/number/v1/";
  const fileClaimUrl = API_SERVER_URL_DEV + "/api/claim/file";
  const claimReceiptUrl = API_SERVER_URL_DEV + "/api/claim/settlement/receipt/";

  const [claims, setClaims] = useState([]);
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");

  const [selectedClaim, setSelectedClaim] = useState(null);

  // Settlement receipt state
  const [receiptData, setReceiptData] = useState(null);
  const [loadingReceipt, setLoadingReceipt] = useState(false);

  const [formData, setFormData] = useState({
    selectedPolicyNumber: "",
    resolvedPolicyId: null,
    incidentDate: "",
    incidentDescription: "",
    claimedAmount: "",
  });

  const [selectedPolicyObj, setSelectedPolicyObj] = useState(null);
  const [formErr, setFormErr] = useState("");

  // 1. Fetch claims filed by this customer
  const fetchMyClaims = async () => {
    setLoading(true);
    try {
      const resp = await axios.get(myClaimsUrl, {
        headers: { Authorization: "Bearer " + token },
      });
      setClaims(resp.data || []);
    } catch (err) {
      console.error("Failed to load claims:", err);
      setClaims([]);
    } finally {
      setLoading(false);
    }
  };

  // 2. Fetch policies for customer
  const fetchPolicies = async () => {
    try {
      const resp = await axios.get(myPoliciesUrl, {
        headers: { Authorization: "Bearer " + token },
      });
      // Keep only active policies for filing claims
      const activeOnly = (resp.data || []).filter(
        (p) => !p.status || p.status === "ACTIVE"
      );
      setPolicies(activeOnly);
    } catch (err) {
      console.error("Failed to load policies for claim:", err);
      setPolicies([]);
    }
  };

  useEffect(() => {
    fetchMyClaims();
    fetchPolicies();
  }, [token]);

  const resetForm = () => {
    setFormData({
      selectedPolicyNumber: "",
      resolvedPolicyId: null,
      incidentDate: "",
      incidentDescription: "",
      claimedAmount: "",
    });
    setSelectedPolicyObj(null);
    setFormErr("");
  };

  // When policy is selected from dropdown:
  const handlePolicySelect = async (e) => {
    const polNum = e.target.value;
    setFormErr("");

    if (!polNum) {
      setSelectedPolicyObj(null);
      setFormData((prev) => ({
        ...prev,
        selectedPolicyNumber: "",
        resolvedPolicyId: null,
      }));
      return;
    }

    const found = policies.find((p) => p.policyNumber === polNum);
    setSelectedPolicyObj(found || null);

    if (found && found.id) {
      setFormData((prev) => ({
        ...prev,
        selectedPolicyNumber: polNum,
        resolvedPolicyId: found.id,
      }));
      return;
    }

    try {
      const resp = await axios.get(policyByNumberUrl + polNum, {
        headers: { Authorization: "Bearer " + token },
      });
      if (resp.data && resp.data.id) {
        setFormData((prev) => ({
          ...prev,
          selectedPolicyNumber: polNum,
          resolvedPolicyId: resp.data.id,
        }));
      }
    } catch (err) {
      console.error("Failed to resolve policy id by number:", err);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleFileClaim = async (e) => {
    e.preventDefault();
    setFormErr("");

    let policyIdToSubmit = formData.resolvedPolicyId;

    if (!policyIdToSubmit && formData.selectedPolicyNumber) {
      try {
        const resp = await axios.get(policyByNumberUrl + formData.selectedPolicyNumber, {
          headers: { Authorization: "Bearer " + token },
        });
        policyIdToSubmit = resp.data?.id;
      } catch (err) {
        setFormErr("Could not retrieve Policy ID. Please re-select the policy.");
        return;
      }
    }

    if (!policyIdToSubmit) {
      setFormErr("Please select an active policy.");
      return;
    }

    if (selectedPolicyObj) {
      const incDate = new Date(formData.incidentDate);
      const start = new Date(selectedPolicyObj.startDate);
      const end = new Date(selectedPolicyObj.endDate);

      if (incDate < start || incDate > end) {
        setFormErr(
          `Incident date must fall within coverage term: ${selectedPolicyObj.startDate} to ${selectedPolicyObj.endDate}`
        );
        return;
      }
    }

    setSubmitting(true);

    const payload = {
      policyId: Number(policyIdToSubmit),
      incidentDate: formData.incidentDate,
      incidentDescription: formData.incidentDescription.trim(),
      claimedAmount: Number(formData.claimedAmount),
    };

    try {
      await axios.post(fileClaimUrl, payload, {
        headers: { Authorization: "Bearer " + token },
      });

      const closeBtn = document.getElementById("closeClaimModalBtn");
      if (closeBtn) closeBtn.click();

      resetForm();
      fetchMyClaims();

      Swal.fire({
        title: "Claim Registered!",
        text: "Your insurance claim has been logged successfully.",
        icon: "success",
        confirmButtonColor: "#0f172a",
        confirmButtonText: "OK",
        timer: 2500,
      });
    } catch (err) {
      const msg = err?.response?.data?.message || "Failed to submit insurance claim.";
      setFormErr(msg);
      Swal.fire({
        title: "Claim Submission Failed",
        text: msg,
        icon: "error",
        confirmButtonColor: "#dc2626",
      });
    } finally {
      setSubmitting(false);
    }
  };

  // Fetch Settlement Receipt
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
        title: "Receipt Unavailable",
        text: "Could not fetch the settlement receipt. Please check back shortly.",
        icon: "error",
      });
    } finally {
      setLoadingReceipt(false);
    }
  };

  // Bulletproof 1-Page Print Handler
  const handlePrintReceipt = () => {
    const printContent = document.getElementById("customerPrintableReceiptArea");
    if (!printContent) return;

    const printWindow = window.open("", "_blank", "width=800,height=650");
    if (!printWindow) {
      window.print();
      return;
    }

    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
        <head>
          <title>Claim Settlement Receipt - ${receiptData?.claimNo || "Voucher"}</title>
          <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
          <style>
            @page {
              size: A4 portrait;
              margin: 15mm;
            }
            body {
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
              color: #1e293b;
              background: #ffffff;
              padding: 20px;
              margin: 0;
            }
            .receipt-container {
              max-width: 600px;
              margin: 0 auto;
              border: 1px solid #cbd5e1;
              border-radius: 8px;
              padding: 25px;
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
          <script>
            window.onload = function() {
              window.print();
              window.onafterprint = function() {
                window.close();
              };
            };
          </script>
        </body>
      </html>
    `);
    printWindow.document.close();
  };

  const renderStatusBadge = (status) => {
    switch (status) {
      case "FILED":
        return <span className="badge badge-claim-filed px-2 py-1">FILED</span>;
      case "UNDER_ASSESSMENT":
      case "ASSESSMENT_COMPLETED":
        return <span className="badge badge-claim-progress px-2 py-1">UNDER SURVEY</span>;
      case "APPROVED":
        return <span className="badge badge-claim-approved px-2 py-1">APPROVED</span>;
      case "REJECTED":
        return (
          <span className="badge bg-danger-subtle text-danger border border-danger-subtle px-2 py-1 fw-semibold">
            ✕ REJECTED
          </span>
        );
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
      c.incidentDescription?.toLowerCase().includes(term)
    );
  });

  return (
    <div className="claim-panel">
      {/* Header */}
      <div className="claim-panel-header d-flex justify-content-between align-items-center flex-wrap gap-2">
        <div>
          <h2 className="h5 fw-bold text-dark mb-1">Insurance Claims</h2>
          <p className="text-muted small mb-0">
            Submit accident or damage claims on active policies, track survey progress, and access settlement receipts.
          </p>
        </div>

        <div className="d-flex align-items-center gap-2">
          <input
            type="text"
            className="form-control form-control-sm"
            style={{ maxWidth: "240px" }}
            placeholder="Search claim no / policy..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <button
            className="btn btn-primary btn-sm fw-semibold px-4 py-2 text-nowrap d-inline-flex align-items-center gap-1 shadow-sm"
            data-bs-toggle="modal"
            data-bs-target="#fileClaimModal"
            onClick={resetForm}
            style={{ minWidth: "max-content", whiteSpace: "nowrap" }}
          >
            <span>+</span> File New Claim
          </button>
        </div>
      </div>

      {/* Claims Ledger Table */}
      <div className="p-0">
        {loading ? (
          <div className="p-5 text-center text-muted">
            <div className="spinner-border spinner-border-sm text-primary me-2"></div>
            Loading your insurance claims...
          </div>
        ) : filteredClaims.length === 0 ? (
          <div className="p-5 text-center text-muted">
            <p className="mb-2">No insurance claims filed yet.</p>
            <button
              className="btn btn-outline-primary btn-sm"
              data-bs-toggle="modal"
              data-bs-target="#fileClaimModal"
              onClick={resetForm}
            >
              Report an Accident / Damage
            </button>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="claim-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Claim Number</th>
                  <th>Linked Policy</th>
                  <th>Incident Date</th>
                  <th>Claimed Amount</th>
                  <th>Settled Amount</th>
                  <th>Assigned Officer</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredClaims.map((item, idx) => (
                  <tr key={item.claimNo || item.id || idx}>
                    <td className="fw-semibold text-secondary">#{idx + 1}</td>
                    <td className="fw-bold text-primary font-monospace">{item.claimNo}</td>
                    <td className="font-monospace text-dark">{item.policyNumber}</td>
                    <td className="small text-secondary">{item.incidentDate}</td>
                    <td className="fw-semibold text-dark">
                      ₹{Number(item.claimedAmount || 0).toLocaleString("en-IN")}
                    </td>
                    <td className="fw-bold">
                      {item.status === "REJECTED" ? (
                        <span className="text-danger small">₹0 (Repudiated)</span>
                      ) : item.approvedAmount && Number(item.approvedAmount) > 0 ? (
                        <span className="text-success">₹{Number(item.approvedAmount).toLocaleString("en-IN")}</span>
                      ) : (
                        <span className="text-muted small">--</span>
                      )}
                    </td>
                    <td className="small text-secondary">
                      {item.officerName || "Pending Desk Assignment"}
                    </td>
                    <td>{renderStatusBadge(item.status)}</td>
                    <td>
                      <div className="d-flex align-items-center gap-1">
                        <button
                          className={`btn btn-sm px-2 py-1 fw-semibold ${
                            item.status === "REJECTED"
                              ? "btn-outline-danger"
                              : "btn-outline-secondary"
                          }`}
                          style={{ fontSize: "0.78rem" }}
                          data-bs-toggle="modal"
                          data-bs-target="#viewClaimModal"
                          onClick={() => setSelectedClaim(item)}
                        >
                          {item.status === "REJECTED" ? "✕ Repudiation Notice" : "👁 Details"}
                        </button>

                        {/* Display settlement receipt trigger ONLY when claim has reached SETTLED status */}
                        {item.status === "SETTLED" && (
                          <button
                            className="btn btn-success btn-sm px-2 py-1 fw-semibold"
                            style={{ fontSize: "0.78rem" }}
                            data-bs-toggle="modal"
                            data-bs-target="#customerClaimReceiptModal"
                            onClick={() => handleOpenReceipt(item.id)}
                          >
                            🧾 Receipt
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal 1: File New Claim */}
      <div
        className="modal fade"
        id="fileClaimModal"
        tabIndex="-1"
        aria-hidden="true"
        data-bs-backdrop="static"
      >
        <div className="modal-dialog modal-lg">
          <div className="modal-content border-0 shadow">
            <div className="modal-header bg-dark text-white">
              <h5 className="modal-title fs-6 fw-bold">File Vehicle Insurance Claim</h5>
              <button
                type="button"
                id="closeClaimModalBtn"
                className="btn-close btn-close-white"
                data-bs-dismiss="modal"
              ></button>
            </div>

            <form onSubmit={handleFileClaim}>
              <div className="modal-body p-4">
                {formErr && <div className="alert alert-danger py-2 small mb-3">{formErr}</div>}

                <div className="row g-3">
                  <div className="col-12">
                    <label className="form-label small fw-bold text-secondary text-uppercase">
                      Select Insured Policy <span className="text-danger">*</span>
                    </label>
                    <select
                      className="form-select"
                      value={formData.selectedPolicyNumber}
                      onChange={handlePolicySelect}
                      required
                    >
                      <option value="">-- Choose Active Policy --</option>
                      {policies.map((p) => (
                        <option key={p.policyNumber} value={p.policyNumber}>
                          {p.policyNumber} — {p.registrationNo} ({p.vehicleMake} {p.vehicleModel})
                        </option>
                      ))}
                    </select>
                  </div>

                  {selectedPolicyObj && (
                    <div className="col-12">
                      <div className="policy-picker-box d-flex justify-content-between align-items-center">
                        <div>
                          <span className="fw-bold text-dark d-block">
                            {selectedPolicyObj.vehicleMake} {selectedPolicyObj.vehicleModel} (
                            {selectedPolicyObj.registrationNo})
                          </span>
                          <span className="policy-period-tag">
                            Active Coverage: {selectedPolicyObj.startDate} to {selectedPolicyObj.endDate}
                          </span>
                        </div>
                        <span className="badge bg-primary">
                          IDV: ₹{Number(selectedPolicyObj.idv || 0).toLocaleString("en-IN")}
                        </span>
                      </div>
                    </div>
                  )}

                  <div className="col-12 col-md-6">
                    <label className="form-label small fw-bold text-secondary text-uppercase">
                      Date of Incident <span className="text-danger">*</span>
                    </label>
                    <input
                      type="date"
                      name="incidentDate"
                      className="form-control"
                      value={formData.incidentDate}
                      onChange={handleInputChange}
                      required
                    />
                  </div>

                  <div className="col-12 col-md-6">
                    <label className="form-label small fw-bold text-secondary text-uppercase">
                      Claimed Estimation (₹) <span className="text-danger">*</span>
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      min="1.00"
                      name="claimedAmount"
                      className="form-control"
                      placeholder="e.g. 15000"
                      value={formData.claimedAmount}
                      onChange={handleInputChange}
                      required
                    />
                  </div>

                  <div className="col-12">
                    <label className="form-label small fw-bold text-secondary text-uppercase">
                      Accident / Damage Details <span className="text-danger">*</span>
                    </label>
                    <textarea
                      name="incidentDescription"
                      rows="4"
                      maxLength="1000"
                      className="form-control"
                      placeholder="Describe what occurred, location of accident, visible vehicle damages..."
                      value={formData.incidentDescription}
                      onChange={handleInputChange}
                      required
                    ></textarea>
                    <span className="form-text small">
                      Provide clear details to accelerate surveyor inspection assignment.
                    </span>
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
                  disabled={submitting || !formData.selectedPolicyNumber}
                >
                  {submitting ? "Submitting Claim..." : "Submit Claim →"}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>

      {/* Modal 2: View Claim Details */}
      <div className="modal fade" id="viewClaimModal" tabIndex="-1" aria-hidden="true">
        <div className="modal-dialog">
          <div className="modal-content border-0 shadow">
            <div className="modal-header bg-dark text-white">
              <h5 className="modal-title fs-6 fw-bold">
                {selectedClaim?.status === "REJECTED" ? "Claim Repudiation Notice" : "Insurance Claim Schedule"}
              </h5>
              <button type="button" className="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div className="modal-body p-4">
              {selectedClaim ? (
                <div className="d-flex flex-column gap-3">
                  <div className="d-flex justify-content-between align-items-center pb-2 border-bottom">
                    <div>
                      <span className="text-muted small d-block">CLAIM IDENTIFIER</span>
                      <strong className="fs-6 text-primary font-monospace">
                        {selectedClaim.claimNo}
                      </strong>
                    </div>
                    {renderStatusBadge(selectedClaim.status)}
                  </div>

                  {selectedClaim.status === "REJECTED" && (
                    <div className="alert alert-danger py-2 small mb-0">
                      <strong>Claim Not Approved:</strong> This claim has been repudiated after technical assessment and underwriter review. No disbursements are scheduled.
                    </div>
                  )}

                  <div className="claim-spec-card">
                    <div className="claim-spec-label">Policy Reference</div>
                    <div className="fw-semibold text-dark font-monospace">
                      {selectedClaim.policyNumber}
                    </div>
                  </div>

                  <div className="row g-2">
                    <div className="col-6">
                      <div className="claim-spec-card">
                        <div className="claim-spec-label">Incident Date</div>
                        <div className="fw-semibold text-dark">{selectedClaim.incidentDate}</div>
                      </div>
                    </div>
                    <div className="col-6">
                      <div className="claim-spec-card">
                        <div className="claim-spec-label">Assigned Officer</div>
                        <div className="fw-semibold text-dark">
                          {selectedClaim.officerName || "In Underwriting Queue"}
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="claim-spec-card">
                    <div className="claim-spec-label">Accident Description</div>
                    <p className="small text-secondary mb-0">
                      "{selectedClaim.incidentDescription}"
                    </p>
                  </div>

                  <div className="d-flex justify-content-between align-items-center p-3 bg-light rounded border">
                    <div>
                      <span className="text-muted small d-block">Claimed Valuation</span>
                      <strong className="fs-6 text-dark">
                        ₹{Number(selectedClaim.claimedAmount || 0).toLocaleString("en-IN")}
                      </strong>
                    </div>
                    <div className="text-end">
                      <span className="text-muted small d-block">Approved Settlement</span>
                      {selectedClaim.status === "REJECTED" ? (
                        <strong className="fs-6 text-danger">₹0.00 (Rejected)</strong>
                      ) : (
                        <strong className="fs-5 text-success">
                          {selectedClaim.approvedAmount && Number(selectedClaim.approvedAmount) > 0
                            ? `₹${Number(selectedClaim.approvedAmount).toLocaleString("en-IN")}`
                            : "Pending Survey"}
                        </strong>
                      )}
                    </div>
                  </div>
                </div>
              ) : null}
            </div>
            <div className="modal-footer bg-light">
              <button type="button" className="btn btn-secondary btn-sm" data-bs-dismiss="modal">
                Close
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Modal 3: Claim Settlement Receipt Voucher */}
      <div
        className="modal fade"
        id="customerClaimReceiptModal"
        tabIndex="-1"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content border-0 shadow">
            <div className="modal-header bg-dark text-white px-4 py-3">
              <div>
                <h5 className="modal-title fs-6 fw-bold mb-0">Settlement Voucher</h5>
                <small className="text-secondary font-monospace" style={{ fontSize: "0.75rem" }}>
                  Disbursement Ref: {receiptData?.settlementRef || "Processing..."}
                </small>
              </div>
              <button
                type="button"
                className="btn-close btn-close-white"
                data-bs-dismiss="modal"
              ></button>
            </div>

            <div className="modal-body p-4">
              {loadingReceipt ? (
                <div className="text-center py-4 text-muted">
                  <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                  Generating official settlement receipt...
                </div>
              ) : receiptData ? (
                <div id="customerPrintableReceiptArea">
                  <div className="text-center pb-3 border-bottom mb-3">
                    <h5 className="fw-bold text-primary font-monospace mb-1">
                      AutoSure Insurance Ltd.
                    </h5>
                    <p className="text-muted small mb-0">
                      Motor Claims Settlement & Discharge Receipt
                    </p>
                  </div>

                  <div className="table-box p-3 bg-light rounded border mb-3 small">
                    <div className="d-flex justify-content-between mb-1">
                      <span className="text-muted">Claim Number:</span>
                      <strong className="font-monospace text-primary">
                        {receiptData.claimNo}
                      </strong>
                    </div>
                    <div className="d-flex justify-content-between mb-1">
                      <span className="text-muted">Policy Number:</span>
                      <strong className="font-monospace text-dark">
                        {receiptData.policyNumber}
                      </strong>
                    </div>
                    <div className="d-flex justify-content-between mb-1">
                      <span className="text-muted">Insured Customer:</span>
                      <strong>
                        {receiptData.customerName} ({receiptData.customerPhone})
                      </strong>
                    </div>
                    <div className="d-flex justify-content-between mb-1">
                      <span className="text-muted">Vehicle Registration:</span>
                      <strong className="font-monospace">
                        {receiptData.registrationNo}
                      </strong>
                    </div>
                    <div className="d-flex justify-content-between">
                      <span className="text-muted">Original Payment Gateway Ref:</span>
                      <span className="font-monospace small text-secondary">
                        {receiptData.originalPaymentTxnRef || "N/A"}
                      </span>
                    </div>
                  </div>

                  <div className="amount-box p-3 bg-success-subtle border border-success-subtle rounded text-center mb-3">
                    <span className="small text-success fw-bold d-block text-uppercase">
                      Net Compensation Credited
                    </span>
                    <strong className="fs-3 text-success">
                      ₹{Number(receiptData.approvedAmount || 0).toLocaleString("en-IN")}
                    </strong>
                    <div
                      className="small text-muted mt-1 font-monospace"
                      style={{ fontSize: "0.75rem" }}
                    >
                      Settlement UTR: <strong>{receiptData.settlementRef}</strong>
                    </div>
                  </div>

                  <div className="d-flex justify-content-between small text-muted pt-2 border-top">
                    <span>
                      Authorized By:{" "}
                      <strong>{receiptData.officerUsername || "Underwriting Desk"}</strong>
                    </span>
                    <span>
                      Date:{" "}
                      <strong>
                        {receiptData.settledDate
                          ? receiptData.settledDate.replace("T", " ").substring(0, 16)
                          : "Today"}
                      </strong>
                    </span>
                  </div>
                </div>
              ) : (
                <div className="text-danger small text-center">
                  Receipt details currently unavailable.
                </div>
              )}
            </div>

            <div className="modal-footer bg-light px-4 py-2 d-flex justify-content-between">
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                data-bs-dismiss="modal"
              >
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

export default CustomerClaimSection;