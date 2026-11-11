import { useState } from "react";
import { Link, useNavigate } from "react-router";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import Swal from "sweetalert2";
import "../../styles/SignUp.css";
import { parseBackendError } from "../../utils/errorHandler";

function SignUp() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [phoneNo, setPhoneNo] = useState("");
  const [address, setAddress] = useState("");
  const [dob, setDob] = useState("");
  const [aadhaarNo, setAadhaarNo] = useState("");
  const [panNo, setPanNo] = useState("");

  const [errMsg, setErrMsg] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const signUpApi = API_SERVER_URL_DEV + "/api/customer/add";

  const processSignup = async (e) => {
    e.preventDefault();
    setErrMsg("");
    setLoading(true);

    const customerDto = {
      username: username.trim(),
      password: password,
      name: name.trim(),
      phoneNo: phoneNo.trim(),
      address: address.trim(),
      dob: dob,
      aadhaarNo: aadhaarNo.trim(),
      panNo: panNo.trim().toUpperCase(),
    };

    try {
      await axios.post(signUpApi, customerDto);

      Swal.fire({
        title: "Account Created!",
        text: "Your customer profile has been registered successfully. Please sign in.",
        icon: "success",
        confirmButtonColor: "#2563eb",
        confirmButtonText: "Go to Login",
        timer: 3000,
        timerProgressBar: true,
      }).then(() => {
        navigate("/");
      });
    } catch (err) {
  const backendMessage = parseBackendError(err, "Registration failed. Please check your inputs.");
  setErrMsg(backendMessage);

  Swal.fire({
    title: "Registration Failed",
    text: backendMessage,
    icon: "error",
    confirmButtonColor: "#dc2626",
    confirmButtonText: "Close",
  });
} finally {
      setLoading(false);
    }
  };

  return (
    <div className="su-page">
      <div className="su-wrap">
        {/* Registration Card */}
        <div className="su-card">
          {/* Blue gradient accent bar */}
          <div className="su-card__bar"></div>

          {/* Header */}
          <header className="su-card__header">
            <div className="su-brand">
              <ShieldMark className="su-brand__icon" />
              <span className="su-brand__name">AutoSure</span>
            </div>
            <h2 className="su-card__title">Customer Enrollment</h2>
            <p className="su-card__subtitle">
              Register your account to manage policies, review surveyed quotes, and track claims
            </p>
          </header>

          {/* Body */}
          <div className="su-card__body">
            {errMsg && (
              <div className="su-alert" role="alert">
                <WarnIcon />
                <span>{errMsg}</span>
              </div>
            )}

            <form className="su-form" onSubmit={processSignup}>
              {/* Section 1: Account Credentials */}
              <div className="su-section">
                <span className="su-section__number">01</span>
                <span className="su-section__title">Login Credentials</span>
              </div>

              <div className="su-grid">
                <div className="su-field">
                  <label className="su-label" htmlFor="su-username">
                    Username <span className="su-required">*</span>
                  </label>
                  <div className="su-input-group">
                    <span className="su-input-group__icon"><UserIcon /></span>
                    <input
                      id="su-username"
                      type="text"
                      className="su-input su-input--icon"
                      placeholder="Choose a username"
                      autoComplete="username"
                      required
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                    />
                  </div>
                </div>

                <div className="su-field">
                  <label className="su-label" htmlFor="su-password">
                    Password <span className="su-required">*</span>
                  </label>
                  <div className="su-input-group">
                    <span className="su-input-group__icon"><LockIcon /></span>
                    <input
                      id="su-password"
                      type="password"
                      className="su-input su-input--icon"
                      placeholder="••••••••"
                      autoComplete="new-password"
                      required
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                    />
                  </div>
                </div>
              </div>

              {/* Section 2: Personal Details */}
              <div className="su-section">
                <span className="su-section__number">02</span>
                <span className="su-section__title">Personal Details</span>
              </div>

              <div className="su-grid">
                <div className="su-field">
                  <label className="su-label" htmlFor="su-name">
                    Full Name <span className="su-required">*</span>
                  </label>
                  <input
                    id="su-name"
                    type="text"
                    className="su-input"
                    placeholder="e.g. Rahul Sharma"
                    autoComplete="name"
                    required
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                  />
                </div>

                <div className="su-field">
                  <label className="su-label" htmlFor="su-phone">
                    Phone Number (10 digits) <span className="su-required">*</span>
                  </label>
                  <div className="su-input-group">
                    <span className="su-input-group__icon su-input-group__icon--text">+91</span>
                    <input
                      id="su-phone"
                      type="tel"
                      className="su-input su-input--prefix"
                      placeholder="9876543210"
                      maxLength="10"
                      pattern="[0-9]{10}"
                      autoComplete="tel-national"
                      required
                      value={phoneNo}
                      onChange={(e) => {
                        const val = e.target.value.replace(/\D/g, "");
                        setPhoneNo(val);
                      }}
                    />
                  </div>
                </div>

                <div className="su-field">
                  <label className="su-label" htmlFor="su-dob">
                    Date of Birth <span className="su-required">*</span>
                  </label>
                  <input
                    id="su-dob"
                    type="date"
                    className="su-input"
                    max={new Date().toISOString().split("T")[0]}
                    required
                    value={dob}
                    onChange={(e) => setDob(e.target.value)}
                  />
                </div>

                <div className="su-field">
                  <label className="su-label" htmlFor="su-pan">
                    PAN Number <span className="su-required">*</span>
                  </label>
                  <input
                    id="su-pan"
                    type="text"
                    className="su-input su-input--mono su-input--upper"
                    placeholder="ABCDE1234F"
                    maxLength="10"
                    required
                    value={panNo}
                    onChange={(e) => setPanNo(e.target.value.toUpperCase())}
                  />
                </div>

                <div className="su-field su-field--full">
                  <label className="su-label" htmlFor="su-aadhaar">
                    Aadhaar Number (12 digits) <span className="su-required">*</span>
                  </label>
                  <input
                    id="su-aadhaar"
                    type="text"
                    className="su-input su-input--mono"
                    placeholder="12-digit Aadhaar number"
                    maxLength="12"
                    pattern="[0-9]{12}"
                    required
                    value={aadhaarNo}
                    onChange={(e) => {
                      const val = e.target.value.replace(/\D/g, "");
                      setAadhaarNo(val);
                    }}
                  />
                </div>

                <div className="su-field su-field--full">
                  <label className="su-label" htmlFor="su-address">
                    Residential Address <span className="su-required">*</span>
                  </label>
                  <textarea
                    id="su-address"
                    className="su-input su-input--textarea"
                    rows="2"
                    placeholder="Flat / Door No, Street, Landmark, City, State, PIN"
                    autoComplete="street-address"
                    required
                    value={address}
                    onChange={(e) => setAddress(e.target.value)}
                  ></textarea>
                </div>
              </div>

              {/* Submit Button */}
              <button type="submit" className="su-button" disabled={loading}>
                {loading ? (
                  <>
                    <span className="su-spinner" role="status" aria-hidden="true"></span>
                    Registering Profile...
                  </>
                ) : (
                  "Create Customer Profile →"
                )}
              </button>

              {/* Footer Back Link */}
              <p className="su-footer">
                Already registered?{" "}
                <Link to="/" className="su-link">
                  Sign In here
                </Link>
              </p>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}

/* ---------- Small presentational icons ---------- */

function ShieldMark({ className }) {
  return (
    <svg className={className} viewBox="0 0 48 56" aria-hidden="true">
      <defs>
        <linearGradient id="su-shield-grad" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#60A5FA" />
          <stop offset="1" stopColor="#2563EB" />
        </linearGradient>
      </defs>
      <path d="M24 2 45 9v17c0 15-10 25-21 28C13 51 3 41 3 26V9Z" fill="url(#su-shield-grad)" />
      <path d="M14 28l7 7 13-14" fill="none" stroke="#fff" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function UserIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor"
      strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <circle cx="12" cy="8" r="4" />
      <path d="M4 20c0-4 3.6-6 8-6s8 2 8 6" />
    </svg>
  );
}

function LockIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor"
      strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <rect x="5" y="11" width="14" height="9" rx="2" />
      <path d="M8 11V8a4 4 0 0 1 8 0v3" />
    </svg>
  );
}

function WarnIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor"
      strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M12 3 2 20h20L12 3Z" />
      <path d="M12 10v4M12 17.5v.01" />
    </svg>
  );
}

export default SignUp;
