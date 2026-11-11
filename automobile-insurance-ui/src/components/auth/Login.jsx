import { useState } from "react"
import { Link, useNavigate } from "react-router"
import { API_SERVER_URL_DEV } from "../../../config"
import axios from "axios"

import "../../styles/login.css";
import  carIllustration from"../../assets/autosure-car.svg";
import { parseBackendError } from "../../utils/errorHandler";

function Login() {
    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [showPassword, setShowPassword] = useState(false)
    const [errMsg, setErrMsg] = useState("")
    const [errComment, setErrComment] = useState("")

    const apiUrl = API_SERVER_URL_DEV + '/api/auth/login'
    const userDetailsUrl = API_SERVER_URL_DEV + '/api/auth/user-details'

    const navigate = useNavigate()
    const processLogin = async (e) => {
        e.preventDefault()
        // Clear any previous error before a new attempt
        setErrMsg("")
        setErrComment("")
        // Step 1. Fetch the jwt token
        const token = 'Basic ' + window.btoa(username + ':' + password)
        try {
            const response = await axios.get(apiUrl, {
                headers: { 'Authorization': token }
            })

            localStorage.setItem('token', response.data.token)
            localStorage.setItem('username', username)

            // Step 2. Fetch User Details using jwtToken
            const resp = await axios.get(userDetailsUrl, {
                headers: { 'Authorization': 'Bearer ' + response.data.token }
            })
            localStorage.setItem('role', resp.data.role)
            // Based on the role of this user load appropriate dashboard
            switch (resp.data.role) {
                case 'CUSTOMER':
                    navigate(`/customer/${response.data.token}`)
                    break;
                case 'ADMIN':
                    navigate(`/admin/${response.data.token}`)
                    break;
                case 'OFFICER':
                    navigate(`/officer/${response.data.token}`)
                    break;
                case 'INSPECTOR':
                    navigate(`/inspector/${response.data.token}`)
                    break;
                default:
                    setErrMsg('Something went wrong...')
                    break;
            }
        }
        catch (err) {
  const message = parseBackendError(err, "Invalid credentials or login failed.");
  setErrMsg(message);
  setErrComment(err?.response?.data?.comments || "");
}
    }

    return (
        <div className="as-page">

            {/* ===== Hero / brand side ===== */}
            <section className="as-hero">
                <div className="as-hero__content">
                    <p className="as-hero__brand">
                        <ShieldMark className="as-hero__brand-mark" />
                        AutoSure
                    </p>
                    <h2 className="as-hero__headline">Drive worry-free.<br />Every mile covered.</h2>
                    <p className="as-hero__text">
                        Insure your car, manage your policy and follow your claims, all in one secure place.
                    </p>
                    <ul className="as-hero__features">
                        <li>Simple policy management</li>
                        <li>Transparent claim tracking</li>
                        <li>Secure access for every role</li>
                    </ul>
                </div>
                <img className="as-hero__image" src={carIllustration} alt="" />
            </section>

            {/* ===== Login side ===== */}
            <main className="as-panel">
                <div className="as-card">

                    {/* Branding / Header */}
                    <header className="as-card__header">
                        <ShieldMark className="as-card__mark" />
                        <h1 className="as-card__title">AutoSure</h1>
                        <p className="as-card__subtitle">Please enter your details to sign in.</p>
                    </header>

                    {/* Error message */}
                    <div className="as-card__feedback" role="alert" aria-live="polite">
                        <p className="as-card__error">{errMsg}</p>
                        <p className="as-card__error-comment">{errComment}</p>
                    </div>

                    {/* Login Form */}
                    <form className="as-form" onSubmit={(e) => processLogin(e)}>

                        {/* Username Field */}
                        <div className="as-field">
                            <label className="as-field__label" htmlFor="as-username">Username</label>
                            <input id="as-username" type="text" className="as-field__input"
                                autoComplete="username" required
                                onChange={(e) => setUsername(e.target.value)} />
                        </div>

                        {/* Password Field */}
                        <div className="as-field">
                            <div className="as-field__label-row">
                                <label className="as-field__label" htmlFor="as-password">Password</label>
                                
                            </div>
                            <div className="as-field__control">
                                <input id="as-password" type={showPassword ? "text" : "password"}
                                    className="as-field__input as-field__input--has-toggle"
                                    placeholder="••••••••" autoComplete="current-password" required
                                    onChange={(e) => setPassword(e.target.value)} />
                                <button type="button" className="as-field__toggle"
                                    aria-label={showPassword ? "Hide password" : "Show password"}
                                    onClick={() => setShowPassword(!showPassword)}>
                                    <EyeIcon off={showPassword} />
                                </button>
                            </div>
                        </div>

                        {/* Submit Button */}
                        <button type="submit" className="as-button">Sign in</button>

                        {/* Footer Link */}
                        <p className="as-form__footer">
                            Don't have an account? <Link to="/customer/signup" className="as-link">Sign up</Link>
                        </p>
                    </form>

                </div>
            </main>
        </div>
    )
}

/* ---------- Small presentational icons ---------- */

function ShieldMark({ className }) {
    return (
        <svg className={className} viewBox="0 0 48 56" aria-hidden="true">
            <defs>
                <linearGradient id="as-shield-grad" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0" stopColor="#60A5FA" />
                    <stop offset="1" stopColor="#2563EB" />
                </linearGradient>
            </defs>
            <path d="M24 2 45 9v17c0 15-10 25-21 28C13 51 3 41 3 26V9Z" fill="url(#as-shield-grad)" />
            <path d="M14 28l7 7 13-14" fill="none" stroke="#fff" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
    )
}

function EyeIcon({ off }) {
    return (
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor"
            strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z" />
            <circle cx="12" cy="12" r="3" />
            {off && <path d="M4 4l16 16" />}
        </svg>
    )
}

export default Login
