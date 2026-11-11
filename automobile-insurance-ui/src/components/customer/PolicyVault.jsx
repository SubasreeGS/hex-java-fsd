import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import "../../styles/PolicyVault.css";

function PolicyVault() {
  const navigate = useNavigate();
  const params = useParams();
  const token = params.token || localStorage.getItem("token");

  const policiesUrl = API_SERVER_URL_DEV + "/api/policy/customer/my-policies/v4";

  const [policies, setPolicies] = useState([]);
  const [filteredPolicies, setFilteredPolicies] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchPolicies = async () => {
      setLoading(true);
      try {
        const resp = await axios.get(policiesUrl, {
          headers: { Authorization: "Bearer " + token },
        });

        const data = resp.data || [];
        setPolicies(data);
        setFilteredPolicies(data);
      } catch (err) {
        console.error("Failed to load customer policies:", err);
        setPolicies([]);
        setFilteredPolicies([]);
      } finally {
        setLoading(false);
      }
    };

    fetchPolicies();
  }, [token]);

  const handleSearch = (e) => {
    const term = e.target.value.toLowerCase().trim();
    setSearchTerm(term);
    if (!term) {
      setFilteredPolicies(policies);
    } else {
      setFilteredPolicies(
        policies.filter(
          (p) =>
            (p.policyNumber && p.policyNumber.toLowerCase().includes(term)) ||
            (p.registrationNo && p.registrationNo.toLowerCase().includes(term)) ||
            (p.vehicleModel && p.vehicleModel.toLowerCase().includes(term)) ||
            (p.vehicleMake && p.vehicleMake.toLowerCase().includes(term))
        )
      );
    }
  };

  // Instant Native Print / Save to PDF Generator (Zero-delay, no network hang)
  const downloadCertificate = (policy) => {
    const printWindow = window.open("", "_blank", "width=850,height=750");

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
              This document is a computer-generated, IRDAI-compliant certificate issued by AutoSure Insurance Co. Ltd.
            </div>
          </div>
        </body>
      </html>
    `;

    printWindow.document.open();
    printWindow.document.write(htmlContent);
    printWindow.document.close();

    // Trigger instant print/save as PDF immediately without hanging
    setTimeout(() => {
      printWindow.focus();
      printWindow.print();
    }, 200);
  };

  return (
    <div className="policy-vault-shell">
      {/* Top Header Panel with Search */}
      <div className="vault-header-panel d-flex justify-content-between align-items-center flex-wrap gap-3">
        <div>
          <h2 className="h5 fw-bold text-dark mb-1">Active Policy Vault</h2>
          <p className="text-muted small mb-0">
            Access verified car insurance certificates, schedule renewals, and review cover details.
          </p>
        </div>

        <div className="policy-quick-search w-100 w-md-auto">
          <input
            type="text"
            className="form-control form-control-sm"
            placeholder="Search Policy No / Reg No..."
            value={searchTerm}
            onChange={handleSearch}
          />
        </div>
      </div>

      {/* Main Content Area */}
      {loading ? (
        <div className="text-center p-5 text-muted bg-white rounded-3 border">
          <div className="spinner-border spinner-border-sm text-primary me-2"></div>
          Loading your insurance policies...
        </div>
      ) : filteredPolicies.length === 0 ? (
        <div className="text-center p-5 text-muted bg-white rounded-3 border">
          <h5 className="h6 fw-bold text-dark mb-2">No Policies Found</h5>
          <p className="small mb-3">
            {searchTerm
              ? "No policies matched your search criteria."
              : "You do not have any active policies issued yet."}
          </p>
          <button
            className="btn btn-primary btn-sm"
            onClick={() => navigate(`/customer/${token}/proposals`)}
          >
            Apply for Policy Coverage
          </button>
        </div>
      ) : (
        <div className="row g-4">
          {filteredPolicies.map((p, idx) => (
            <div className="col-12 col-xl-6" key={p.policyNumber || p.id || idx}>
              <div className="policy-cert-card h-100 flex-column">
                {/* Header Banner */}
                <div className="policy-cert-header d-flex justify-content-between align-items-center">
                  <div>
                    <span
                      className="small text-secondary d-block"
                      style={{ fontSize: "0.7rem", letterSpacing: "0.04em" }}
                    >
                      OFFICIAL POLICY NUMBER
                    </span>
                    <strong className="fs-6 text-light font-monospace">
                      {p.policyNumber || `POL-AUTO-${p.id}`}
                    </strong>
                  </div>

                  <span
                    className={
                      p.status === "EXPIRED" ? "cert-expired-badge" : "cert-stamp-badge"
                    }
                  >
                    {p.status || "ACTIVE"}
                  </span>
                </div>

                {/* Card Content Grid */}
                <div className="policy-card-body flex-grow-1">
                  <div className="row g-3">
                    <div className="col-6">
                      <div className="policy-meta-label">Vehicle Registration</div>
                      <div className="policy-meta-value text-primary font-monospace">
                        {p.registrationNo || p.vehicle?.registrationNo || "--"}
                      </div>
                    </div>

                    <div className="col-6">
                      <div className="policy-meta-label">Make & Model</div>
                      <div className="policy-meta-value text-truncate">
                        {p.vehicleMake || p.vehicle?.make || ""}{" "}
                        {p.vehicleModel || p.vehicle?.model || ""}
                      </div>
                    </div>

                    <div className="col-6">
                      <div className="policy-meta-label">Plan Type</div>
                      <div className="policy-meta-value">
                        <span className="badge bg-light text-dark border">
                          {p.policyType || "COMPREHENSIVE"}
                        </span>
                        {p.zeroDep && (
                          <span className="badge bg-info-subtle text-info border border-info-subtle ms-1 small">
                            Zero Dep
                          </span>
                        )}
                      </div>
                    </div>

                    <div className="col-6">
                      <div className="policy-meta-label">Total Insured IDV</div>
                      <div className="policy-meta-value text-dark fw-bold">
                        ₹{Number(p.idv || 0).toLocaleString("en-IN")}
                      </div>
                    </div>
                  </div>

                  {/* Coverage Schedule Box */}
                  <div className="coverage-schedule-box d-flex justify-content-between align-items-center">
                    <div className="schedule-date-item">
                      <span className="schedule-label">Cover Commences</span>
                      <span className="schedule-val">{p.startDate || "--"}</span>
                    </div>

                    <div className="text-muted small px-2">➔</div>

                    <div className="schedule-date-item text-end">
                      <span className="schedule-label">Cover Expires</span>
                      <span className="schedule-val">{p.endDate || "--"}</span>
                    </div>
                  </div>

                  {/* Actions Row */}
                  <div className="policy-card-footer d-flex justify-content-between align-items-center">
                    <div>
                      <span
                        className="small text-muted d-block"
                        style={{ fontSize: "0.7rem", letterSpacing: "0.04em" }}
                      >
                        PREMIUM PAID
                      </span>
                      <strong className="text-success fs-5">
                        ₹{Number(p.totalPremium || 0).toLocaleString("en-IN")}
                      </strong>
                    </div>

                    <button
                      className="btn btn-download-cert d-flex align-items-center gap-1"
                      onClick={() => downloadCertificate(p)}
                    >
                      <span>Download PDF</span>
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
  );
}

export default PolicyVault;