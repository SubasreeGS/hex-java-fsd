import { useState, useEffect } from "react";
import { useSelector, useDispatch } from "react-redux";
import axios from "axios";
import Swal from "sweetalert2";
import { API_SERVER_URL_DEV } from "../../../config";
import {
  addInspector,
  updateInspector,
  setSelectedInspector,
} from "../../store/inspectorSlice";

function AddInspector({ onInspectorAdded }) {
  const token = localStorage.getItem("token");
  const dispatch = useDispatch();

  const selectedInspector = useSelector(
    (state) => state.inspector.selectedInspector
  );

  const [formData, setFormData] = useState({
    name: "",
    email: "",
    contact: "",
    username: "",
    password: "",
  });

  const [loading, setLoading] = useState(false);
  const [errMsg, setErrMsg] = useState("");

  const isEditing = Boolean(selectedInspector && selectedInspector.id);

  // Helper to extract error message directly without external file
  const extractErrorMessage = (err, defaultMsg) => {
    if (!err?.response?.data) {
      return defaultMsg || "Network or server connection issue.";
    }
    const data = err.response.data;

    // 1. Handles custom ErrorDto: { message: "...", comments: "..." }
    if (typeof data.message === "string" && data.message.trim() !== "") {
      return data.message;
    }

    // 2. Handles Bean Validation Map: { name: "must not be blank", email: "invalid format" }
    if (typeof data === "object" && !Array.isArray(data)) {
      const fieldErrors = Object.entries(data)
        .filter(([k]) => k !== "timestamp" && k !== "status" && k !== "error")
        .map(([field, msg]) => `${field}: ${msg}`);
      if (fieldErrors.length > 0) {
        return fieldErrors.join(" | ");
      }
    }

    // 3. String error response fallback
    if (typeof data === "string" && data.trim() !== "") {
      return data;
    }

    return defaultMsg || "An unexpected error occurred.";
  };

  // Populate form when an inspector is chosen for editing
  useEffect(() => {
    if (selectedInspector) {
      setFormData({
        name: selectedInspector.name || "",
        email: selectedInspector.email || "",
        contact: selectedInspector.contact || "",
        username: selectedInspector.username || "",
        password: "", // Password not editable during profile update
      });
      setErrMsg("");
    } else {
      resetForm();
    }
  }, [selectedInspector]);

  const resetForm = () => {
    setFormData({
      name: "",
      email: "",
      contact: "",
      username: "",
      password: "",
    });
    setErrMsg("");
    dispatch(setSelectedInspector(null));
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrMsg("");
    setLoading(true);

    try {
      if (isEditing) {
        // UPDATE MODE: Matches InspectorUpdateDto (name, email, contact)
        const updatePayload = {
          name: formData.name.trim(),
          email: formData.email.trim(),
          contact: formData.contact.trim(),
        };

        const resp = await axios.put(
          `${API_SERVER_URL_DEV}/api/inspector/${selectedInspector.id}`,
          updatePayload,
          {
            headers: { Authorization: "Bearer " + token },
          }
        );

        dispatch(updateInspector(resp.data));

        Swal.fire({
          title: "Profile Updated!",
          text: `Inspector details for ${resp.data.name} updated successfully.`,
          icon: "success",
          confirmButtonColor: "#0f172a",
          timer: 2000,
          showConfirmButton: false,
        });

        resetForm();
        if (onInspectorAdded) onInspectorAdded();
      } else {
        // CREATE MODE: Matches InspectorDto (name, email, contact, username, password)
        const createPayload = {
          name: formData.name.trim(),
          email: formData.email.trim(),
          contact: formData.contact.trim(),
          username: formData.username.trim(),
          password: formData.password,
        };

        const resp = await axios.post(
          `${API_SERVER_URL_DEV}/api/inspector/add`,
          createPayload,
          {
            headers: { Authorization: "Bearer " + token },
          }
        );

        dispatch(addInspector(resp.data));

        Swal.fire({
          title: "Inspector Onboarded!",
          text: `Surveyor ${resp.data.name} is now registered.`,
          icon: "success",
          confirmButtonColor: "#0f172a",
          timer: 2000,
          showConfirmButton: false,
        });

        resetForm();
        if (onInspectorAdded) onInspectorAdded();
      }
    } catch (err) {
      const message = extractErrorMessage(
        err,
        isEditing
          ? "Failed to update inspector profile."
          : "Failed to onboard new inspector."
      );
      setErrMsg(message);

      Swal.fire({
        title: isEditing ? "Update Error" : "Onboarding Error",
        text: message,
        icon: "error",
        confirmButtonColor: "#dc2626",
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card shadow-sm border rounded">
      <div className="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h6 className="fw-bold text-dark mb-0">
            {isEditing ? "✏️ Edit Field Inspector" : "➕ Onboard New Field Inspector"}
          </h6>
          <span className="text-muted small">
            {isEditing
              ? `Updating profile for ${selectedInspector?.name} (ID: #${selectedInspector?.id})`
              : "Register and allocate portal credentials for technical surveyor"}
          </span>
        </div>
        {isEditing && (
          <button
            type="button"
            className="btn btn-outline-secondary btn-sm"
            onClick={resetForm}
          >
            ✕ Cancel Edit
          </button>
        )}
      </div>

      <div className="card-body p-4">
        {errMsg && <div className="alert alert-danger py-2 small mb-3">{errMsg}</div>}

        <form onSubmit={handleSubmit}>
          <div className="row g-3">
            <div className="col-12 col-md-4">
              <label className="form-label small fw-bold text-secondary">
                Full Name <span className="text-danger">*</span>
              </label>
              <input
                type="text"
                name="name"
                className="form-control form-control-sm"
                placeholder="e.g. Ramesh Kumar"
                value={formData.name}
                onChange={handleInputChange}
                required
              />
            </div>

            <div className="col-12 col-md-4">
              <label className="form-label small fw-bold text-secondary">
                Official Email <span className="text-danger">*</span>
              </label>
              <input
                type="email"
                name="email"
                className="form-control form-control-sm"
                placeholder="ramesh.surveyor@autosure.com"
                value={formData.email}
                onChange={handleInputChange}
                required
              />
            </div>

            <div className="col-12 col-md-4">
              <label className="form-label small fw-bold text-secondary">
                Mobile Number (10 Digits) <span className="text-danger">*</span>
              </label>
              <input
                type="tel"
                name="contact"
                pattern="[0-9]{10}"
                maxLength="10"
                className="form-control form-control-sm"
                placeholder="9876543210"
                value={formData.contact}
                onChange={handleInputChange}
                required
              />
            </div>

            {!isEditing ? (
              <>
                <div className="col-12 col-md-6">
                  <label className="form-label small fw-bold text-secondary">
                    Portal Username <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    name="username"
                    className="form-control form-control-sm font-monospace"
                    placeholder="surveyor_ramesh"
                    value={formData.username}
                    onChange={handleInputChange}
                    required
                  />
                </div>

                <div className="col-12 col-md-6">
                  <label className="form-label small fw-bold text-secondary">
                    Initial Password <span className="text-danger">*</span>
                  </label>
                  <input
                    type="password"
                    name="password"
                    className="form-control form-control-sm"
                    placeholder="Min 6 characters"
                    value={formData.password}
                    onChange={handleInputChange}
                    required
                  />
                </div>
              </>
            ) : (
              <div className="col-12">
                <div className="alert alert-light border small text-muted mb-0">
                  Login username (<strong>{formData.username}</strong>) and password credentials cannot be modified from the profile update form.
                </div>
              </div>
            )}
          </div>

          <div className="d-flex justify-content-end gap-2 mt-4 pt-3 border-top">
            <button
              type="button"
              className="btn btn-outline-secondary btn-sm px-3"
              onClick={resetForm}
              disabled={loading}
            >
              Reset
            </button>
            <button
              type="submit"
              className={`btn btn-sm px-4 fw-semibold ${
                isEditing ? "btn-warning text-dark" : "btn-primary"
              }`}
              disabled={loading}
            >
              {loading
                ? isEditing
                  ? "Updating..."
                  : "Onboarding..."
                : isEditing
                ? "Save Changes"
                : "Register Surveyor"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AddInspector;