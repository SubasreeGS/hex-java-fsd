import { Navigate, Route, Routes } from "react-router"
import Login from "./components/auth/Login"
import SignUp from "./components/customer/SignUp"
import PageNotFound from "./components/PageNotFound"

// Role Dashboard Layouts
import AdminDashboard from "./components/admin/AdminDashboard"
import OfficerDashboard from "./components/officer/OfficerDashboard"
import InspectorDashboard from "./components/inspector/InspectorDashboard"
import CustomerDashboard from "./components/customer/CustomerDashboard"

// Customer Sub-routes
import ProfileSection from "./components/customer/ProfileSection"
import ProposalSection from "./components/customer/ProposalSection"
import QuotePaymentSection from "./components/customer/QuotePaymentSection"
import PolicyVault from "./components/customer/PolicyVault"


// Officer Sub-routes (Step 2 - Step 4 modules)
import OfficerProposalSection from "./components/officer/OfficerProposalSection"
import OfficerClaimSection from "./components/officer/OfficerClaimSection"
import OfficerInspectorSection from "./components/officer/OfficerInspectorSection"
import OfficerPolicySection from "./components/officer/OfficerPolicySection"
import InspectorProposalSection from "./components/inspector/InspectorProposalSection"
import CustomerClaimSection from "./components/customer/CustomerClaimSection"
import ClaimAssessmentSection from "./components/inspector/ClaimAssessmentSection"
import OfficerOverviewSection from "./components/officer/OfficerOverviewSection"
import InspectorOverviewSection from "./components/inspector/InspectorOverviewSection"

function App() {
  return (
    <Routes>
      {/* 1. Public Authentication Routes */}
      <Route path="" element={<Login />} />
      <Route path="customer/signup" element={<SignUp />} />

      {/* 2. Admin  Route */}
      <Route path="admin/:token" element={<AdminDashboard />} />

      {/* 3. Customer Portal  */}
      <Route path="customer/:token" element={<CustomerDashboard />}>
        <Route index element={<Navigate to="profile" replace />} />
        <Route path="profile" element={<ProfileSection />} />
        <Route path="proposals" element={<ProposalSection />} />
        <Route path="quotes-payment" element={<QuotePaymentSection />} />
        <Route path="policies" element={<PolicyVault />} />
        <Route path="claims" element={<CustomerClaimSection />} />
      </Route>

      {/* 4. Inspector Portal */}
      <Route path="/inspector/:token" element={<InspectorDashboard />}>
        <Route index element={<InspectorOverviewSection />} />
        <Route path="pre-policy" element={<InspectorProposalSection />} />
        <Route path="claim-assessments" element={<ClaimAssessmentSection />} />
      </Route>

      {/* Officer*/}
      <Route path="/officer/:token" element={<OfficerDashboard />}>
       <Route index element={<OfficerOverviewSection />} />
       <Route path="overview" element={<OfficerOverviewSection />} />
        <Route path="proposals" element={<OfficerProposalSection />} />
        <Route path="claims" element={<OfficerClaimSection />} />

        <Route path="inspectors" element={<OfficerInspectorSection />} />
        <Route path="policies" element={<OfficerPolicySection />} />
        
      </Route>

      {/* 6. Access Control */}
      <Route
        path="/no-access"
        element={
          <div className="container py-5 text-center">
            <h2 className="text-danger fw-bold">403 - Forbidden Access</h2>
            <p className="text-secondary">
              You do not have administrative or officer authorization to view this resource.
            </p>
          </div>
        }
      />
      <Route path="*" element={<PageNotFound />} />
    </Routes>
  )
}

export default App