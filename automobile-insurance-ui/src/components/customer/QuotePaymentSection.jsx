import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import Swal from "sweetalert2";
import "../../styles/QuotePaymentSection.css";

function QuotePaymentSection() {
  const navigate = useNavigate();
  const params = useParams();
  const token = params.token || localStorage.getItem("token");

  const proposalsUrl = API_SERVER_URL_DEV + "/api/proposal/customer/v2";
  const quoteUrl = API_SERVER_URL_DEV + "/api/quote/proposal/";
  const processPaymentUrl = API_SERVER_URL_DEV + "/api/payment/process/";

  const [proposals, setProposals] = useState([]);
  const [selectedProposal, setSelectedProposal] = useState(null);
  const [quoteDetails, setQuoteDetails] = useState(null);
  const [loadingProposals, setLoadingProposals] = useState(true);
  const [loadingQuote, setLoadingQuote] = useState(false);
  const [processing, setProcessing] = useState(false);

  // Payment Form State
  const [paymentMode, setPaymentMode] = useState("UPI");
  const [upiId, setUpiId] = useState("");
  const [cardNumber, setCardNumber] = useState("");
  const [expiry, setExpiry] = useState("");
  const [cvv, setCvv] = useState("");

  useEffect(() => {
    const fetchProposals = async () => {
      setLoadingProposals(true);
      try {
        const resp = await axios.get(proposalsUrl, {
          headers: { Authorization: "Bearer " + token },
        });

        // Filter proposals ready for checkout (matching backend ProposalStatus)
        const available = (resp.data || []).filter(
          (p) =>
            p.status === "QUOTE_GENERATED" ||
            p.status === "PAYMENT_PENDING"
        );

        setProposals(available);
        if (available.length > 0) {
          selectProposal(available[0]);
        }
      } catch (err) {
        console.error("Could not load proposals for quote:", err);
      } finally {
        setLoadingProposals(false);
      }
    };

    fetchProposals();
  }, [token]);

  // Fetch full quote breakdown for chosen proposal
  const selectProposal = async (proposal) => {
    setSelectedProposal(proposal);
    setLoadingQuote(true);

    try {
      const resp = await axios.get(quoteUrl + proposal.id, {
        headers: { Authorization: "Bearer " + token },
      });
      setQuoteDetails(resp.data);
    } catch (err) {
      console.error("Error fetching quote details:", err);
      setQuoteDetails(null);
    } finally {
      setLoadingQuote(false);
    }
  };

  const handlePayment = async (e) => {
    e.preventDefault();
    if (!quoteDetails) return;

    setProcessing(true);

    // Generate unique transaction reference
    const txnRef = "TXN" + Date.now() + Math.floor(100 + Math.random() * 900);

    // Map strictly to backend ProcessPaymentDto
    const payload = {
      transactionRef: txnRef,
      amount: quoteDetails.totalPremium,
      paymentMode: paymentMode, // UPI | CARD | NETBANKING
    };

    try {
      // POST /api/payment/process/{quoteId}
      await axios.post(processPaymentUrl + quoteDetails.quoteId, payload, {
        headers: { Authorization: "Bearer " + token },
      });

      Swal.fire({
        title: "Payment Received!",
        html: `
          <div class="text-start small p-2">
            <p class="mb-1"><strong>Txn Reference:</strong> ${txnRef}</p>
            <p class="mb-1"><strong>Amount Paid:</strong> ₹${Number(quoteDetails.totalPremium).toLocaleString("en-IN")}</p>
            <p class="mb-1"><strong>Payment Mode:</strong> ${paymentMode}</p>
            <hr class="my-2"/>
            <p class="text-success fw-bold mb-0">Your policy has been successfully issued and activated in your Vault!</p>
          </div>
        `,
        icon: "success",
        confirmButtonColor: "#0f172a",
        confirmButtonText: "Go to Policy Vault →",
      }).then((result) => {
        if (result.isConfirmed) {
          navigate(`/customer/${token}/policies`);
        }
      });
    } catch (err) {
      const errorMsg =
        err?.response?.data?.message || "Transaction declined by the payment gateway.";
      Swal.fire({
        title: "Payment Failed",
        text: errorMsg,
        icon: "error",
        confirmButtonColor: "#dc2626",
      });
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div className="checkout-panel">
      {/* Header */}
      <div className="checkout-panel-header d-flex justify-content-between align-items-center">
        <div>
          <h2 className="h5 fw-bold text-dark mb-1">Quotes & Premium Checkout</h2>
          <p className="text-muted small mb-0">
            Review your approved IDV calculation and complete premium payment to issue coverage.
          </p>
        </div>
      </div>

      <div className="p-4">
        {loadingProposals ? (
          <div className="text-center p-5 text-muted">
            <div className="spinner-border spinner-border-sm text-primary me-2"></div>
            Loading active quote requests...
          </div>
        ) : proposals.length === 0 ? (
          <div className="text-center p-5 text-muted">
            <h5 className="h6 fw-bold text-dark">No Pending Quotes Found</h5>
            <p className="small mb-3">
              Quotes will appear here as soon as an Underwriting Officer reviews your vehicle survey report.
            </p>
            <button
              className="btn btn-primary btn-sm"
              onClick={() => navigate(`/customer/${token}/proposals`)}
            >
              Go to My Proposals
            </button>
          </div>
        ) : (
          <div className="row g-4">
            {/* Left Column: Proposals List & Detailed Quote Breakdown */}
            <div className="col-12 col-lg-5">
              <h6 className="fw-bold text-secondary text-uppercase small mb-3">
                 Approved Application
              </h6>

              {proposals.map((p) => (
                <div
                  key={p.id}
                  className={`quote-item-card ${
                    selectedProposal?.id === p.id ? "selected" : ""
                  }`}
                  onClick={() => selectProposal(p)}
                >
                  <div className="d-flex justify-content-between align-items-center mb-1">
                    <span className="fw-bold text-dark">
                      {p.registrationNo || `Proposal #${p.id}`}
                    </span>
                    <span className="badge bg-success-subtle text-success border border-success-subtle small">
                      {p.status}
                    </span>
                  </div>
                  <div className="text-muted small">
                    {p.make || p.vehicleMake} {p.model || p.vehicleModel} <br /> {p.policyType}
                  </div>
                </div>
              ))}

              {/* Live Quote Breakdown Card */}
              {loadingQuote ? (
                <div className="text-center p-4 text-muted small">
                  <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                  Calculating quote details...
                </div>
              ) : quoteDetails ? (
                <div className="card border rounded-3 p-3 mt-3 bg-light">
                  <div className="d-flex justify-content-between align-items-center mb-3 pb-2 border-bottom">
                    <div>
                      <span className="fw-bold text-dark small text-uppercase d-block">
                        {quoteDetails.policyType} Coverage
                      </span>
                      <span className="text-muted small" style={{ fontSize: "0.75rem" }}>
                        Quote #{quoteDetails.quoteId} <br />Proposal #{quoteDetails.proposalId}
                      </span>
                    </div>
                    <span className="badge bg-primary fs-6">
                      IDV: ₹{Number(quoteDetails.idv || 0).toLocaleString("en-IN")}
                    </span>
                  </div>

                  <div className="breakdown-row">
                    <span>Third Party (TP) Premium</span>
                    <span className="fw-semibold">
                      ₹{Number(quoteDetails.tpPremium || 0).toLocaleString("en-IN")}
                    </span>
                  </div>

                  <div className="breakdown-row">
                    <span>Own Damage (OD) Premium</span>
                    <span className="fw-semibold">
                      ₹{Number(quoteDetails.odPremium || 0).toLocaleString("en-IN")}
                    </span>
                  </div>

                  {Number(quoteDetails.zeroDepPremium) > 0 && (
                    <div className="breakdown-row">
                      <span>Zero Depreciation Add-on</span>
                      <span className="fw-semibold">
                        ₹{Number(quoteDetails.zeroDepPremium).toLocaleString("en-IN")}
                      </span>
                    </div>
                  )}

                  {Number(quoteDetails.engineProtectPremium) > 0 && (
                    <div className="breakdown-row">
                      <span>Engine Protection Add-on</span>
                      <span className="fw-semibold">
                        ₹{Number(quoteDetails.engineProtectPremium).toLocaleString("en-IN")}
                      </span>
                    </div>
                  )}

                  {Number(quoteDetails.roadsideAssistPremium) > 0 && (
                    <div className="breakdown-row">
                      <span>24x7 Roadside Assistance</span>
                      <span className="fw-semibold">
                        ₹{Number(quoteDetails.roadsideAssistPremium).toLocaleString("en-IN")}
                      </span>
                    </div>
                  )}

                  <div className="breakdown-row text-muted mt-2 pt-2 border-top">
                    <span>Applicable GST (18%)</span>
                    <span>₹{Number(quoteDetails.taxAmount || 0).toLocaleString("en-IN")}</span>
                  </div>

                  <div className="breakdown-total d-flex justify-content-between align-items-center mt-3 pt-2 border-top">
                    <span className="fw-bold fs-6">Total Amount Payable</span>
                    <span className="text-success fs-5 fw-bold">
                      ₹{Number(quoteDetails.totalPremium || 0).toLocaleString("en-IN")}
                    </span>
                  </div>
                </div>
              ) : null}
            </div>

            {/* Right Column: Checkout Payment Form */}
            <div className="col-12 col-lg-7">
              <div className="card border-0 shadow-sm rounded-3 p-4">
                <h6 className="fw-bold text-secondary text-uppercase small mb-3">
                  2. Authorize Payment
                </h6>

                <form onSubmit={handlePayment}>
                  <label className="form-label small fw-semibold text-secondary">
                    Select Payment Gateway Mode
                  </label>

                  {/* Mode 1: UPI */}
                  <div
                    className={`payment-method-box ${paymentMode === "UPI" ? "active" : ""}`}
                    onClick={() => setPaymentMode("UPI")}
                  >
                    <input
                      type="radio"
                      name="payMode"
                      className="form-check-input"
                      checked={paymentMode === "UPI"}
                      onChange={() => setPaymentMode("UPI")}
                    />
                    <span className="fw-semibold small">
                      UPI (Google Pay / PhonePe / Paytm / BHIM)
                    </span>
                  </div>

                  {paymentMode === "UPI" && (
                    <div className="mb-3 ps-4">
                      <label className="form-label small text-muted">
                        Virtual Payment Address (VPA)
                      </label>
                      <input
                        type="text"
                        className="form-control form-control-sm"
                        placeholder="yourname@okaxis"
                        value={upiId}
                        onChange={(e) => setUpiId(e.target.value)}
                        required={paymentMode === "UPI"}
                      />
                    </div>
                  )}

                  {/* Mode 2: Card */}
                  <div
                    className={`payment-method-box ${paymentMode === "CARD" ? "active" : ""}`}
                    onClick={() => setPaymentMode("CARD")}
                  >
                    <input
                      type="radio"
                      name="payMode"
                      className="form-check-input"
                      checked={paymentMode === "CARD"}
                      onChange={() => setPaymentMode("CARD")}
                    />
                    <span className="fw-semibold small">
                      Debit / Credit Card (Visa, RuPay, MasterCard)
                    </span>
                  </div>

                  {paymentMode === "CARD" && (
                    <div className="row g-2 mb-3 ps-4">
                      <div className="col-12">
                        <label className="form-label small text-muted">Card Number</label>
                        <input
                          type="text"
                          maxLength="16"
                          className="form-control form-control-sm"
                          placeholder="4111 2222 3333 4444"
                          value={cardNumber}
                          onChange={(e) => setCardNumber(e.target.value)}
                          required={paymentMode === "CARD"}
                        />
                      </div>
                      <div className="col-6">
                        <label className="form-label small text-muted">Expiry (MM/YY)</label>
                        <input
                          type="text"
                          maxLength="5"
                          className="form-control form-control-sm"
                          placeholder="12/28"
                          value={expiry}
                          onChange={(e) => setExpiry(e.target.value)}
                          required={paymentMode === "CARD"}
                        />
                      </div>
                      <div className="col-6">
                        <label className="form-label small text-muted">CVV</label>
                        <input
                          type="password"
                          maxLength="3"
                          className="form-control form-control-sm"
                          placeholder="•••"
                          value={cvv}
                          onChange={(e) => setCvv(e.target.value)}
                          required={paymentMode === "CARD"}
                        />
                      </div>
                    </div>
                  )}

                  {/* Mode 3: Net Banking */}
                  <div
                    className={`payment-method-box ${paymentMode === "NETBANKING" ? "active" : ""}`}
                    onClick={() => setPaymentMode("NETBANKING")}
                  >
                    <input
                      type="radio"
                      name="payMode"
                      className="form-check-input"
                      checked={paymentMode === "NETBANKING"}
                      onChange={() => setPaymentMode("NETBANKING")}
                    />
                    <span className="fw-semibold small">Net Banking (All Scheduled Banks)</span>
                  </div>

                  <div className="d-grid mt-4">
                    <button
                      type="submit"
                      className="btn btn-primary btn-lg fw-semibold"
                      disabled={processing || !quoteDetails}
                    >
                      {processing
                        ? "Connecting Gateway..."
                        : `Pay ₹${Number(quoteDetails?.totalPremium || 0).toLocaleString("en-IN")}`}
                    </button>
                  </div>

                  <div className="text-center mt-3">
                    <span className="text-muted small" style={{ fontSize: "0.75rem" }}>
                      🔒 256-Bit Encrypted IRDAI Certified Payment Gateway
                    </span>
                  </div>
                </form>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default QuotePaymentSection;