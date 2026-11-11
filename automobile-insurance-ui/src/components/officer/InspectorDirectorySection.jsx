import { useState } from "react";
import { useSelector, useDispatch } from "react-redux";
import { setSelectedInspector, deleteInspector } from "../../store/inspectorSlice";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import Swal from "sweetalert2";
import { parseBackendError } from "../../utils/errorHandler";

function InspectorDirectorySection({
  page,
  pageSize,
  setPage,
  setPageSize,
  totalPages,
  totalRecords,
  onRefresh,
}) {
  const token = localStorage.getItem("token");
  const dispatch = useDispatch();

  const inspectors = useSelector((state) => state.inspector.list);
  const loading = useSelector((state) => state.inspector.loading);
  const [searchTerm, setSearchTerm] = useState("");

  const handleDelete = async (id, name) => {
    const confirm = await Swal.fire({
      title: "Delete Inspector?",
      text: `Are you sure you want to remove ${name}? This will also revoke their login credentials.`,
      icon: "warning",
      showCancelButton: true,
      confirmButtonColor: "#dc2626",
      cancelButtonColor: "#64748b",
      confirmButtonText: "Yes, Delete",
    });

    if (!confirm.isConfirmed) return;

    try {
      await axios.delete(`${API_SERVER_URL_DEV}/api/inspector/${id}`, {
        headers: { Authorization: "Bearer " + token },
      });
      dispatch(deleteInspector(id));
      Swal.fire({
        title: "Deleted!",
        text: "Inspector record removed successfully.",
        icon: "success",
        timer: 1800,
        showConfirmButton: false,
      });
      if (onRefresh) onRefresh();
    } 
    catch (err) {
  const message = parseBackendError(err, "Failed to delete inspector record.");
  Swal.fire({
    title: "Deletion Error",
    text: message,
    icon: "error",
  });
}
    
  };

  const filteredInspectors = (inspectors || []).filter((insp) => {
    const term = searchTerm.toLowerCase().trim();
    if (!term) return true;
    return (
      insp.name?.toLowerCase().includes(term) ||
      insp.username?.toLowerCase().includes(term) ||
      insp.email?.toLowerCase().includes(term) ||
      insp.contact?.toLowerCase().includes(term)
    );
  });

  return (
    <div className="inspector-directory-card shadow-sm border rounded">
      {/* Search and Action Bar */}
      <div className="directory-header d-flex flex-wrap justify-content-between align-items-center gap-2 p-3 bg-white border-bottom">
        <div>
          <h6 className="fw-bold text-dark mb-0">Inspector Directory Queue</h6>
          <span className="text-muted small">Active Technical Verification Personnel</span>
        </div>
        <div className="d-flex align-items-center gap-2 flex-wrap">
          <input
            type="text"
            className="form-control form-control-sm"
            style={{ width: "240px" }}
            placeholder="Search inspector, email, user..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <button
            className="btn btn-outline-secondary btn-sm"
            onClick={onRefresh}
            disabled={loading}
          >
            ↻ Refresh
          </button>
        </div>
      </div>

      {/* Main Content: Card-Job Layout */}
      <div className="p-3 p-md-4 bg-light-subtle">
        {loading ? (
          <div className="p-5 text-center text-muted">
            <div className="spinner-border spinner-border-sm text-primary me-2" role="status"></div>
            Loading certified surveyors...
          </div>
        ) : filteredInspectors.length === 0 ? (
          <div className="p-5 text-center text-muted bg-white rounded-3 border">
            <div className="fs-1 mb-2">🕵️‍♂️</div>
            <h6 className="fw-bold text-dark mb-1">No Inspectors Found</h6>
            <p className="small text-muted mb-0">
              {searchTerm
                ? "No surveyors match your search criteria on this page."
                : "No field surveyors enrolled currently in this view."}
            </p>
          </div>
        ) : (
          <div className="d-flex flex-column gap-3">
            {filteredInspectors.map((insp) => (
              <div key={insp.id} className="surveyor-profile-card bg-white border rounded p-3 shadow-sm">
                {/* Meta Header */}
                <div className="d-flex flex-wrap justify-content-between align-items-center border-bottom pb-2 mb-3 gap-2">
                  <div className="d-flex align-items-center gap-2">
                    <span className="surveyor-badge">ID #{insp.id}</span>
                    <span className="fw-bold text-dark fs-6">{insp.name}</span>
                    <span className="badge bg-primary-subtle text-primary border border-primary-subtle">
                      Licensed Field Surveyor
                    </span>
                  </div>
                  <div className="text-secondary font-monospace small">
                    Username: <strong className="text-dark">{insp.username}</strong>
                  </div>
                </div>

                {/* 3-Column Enterprise Structured Layout */}
                <div className="row g-3 align-items-center">
                  {/* Column 1: Contact Dossier */}
                  <div className="col-12 col-md-5 surveyor-col-divider">
                    <div className="field-label mb-1">Contact Information</div>
                    <div className="small text-dark fw-semibold">
                      ✉️ {insp.email || "No email registered"}
                    </div>
                    <div className="small text-secondary font-monospace mt-1">
                      📞 {insp.contact || "No telephone provided"}
                    </div>
                  </div>

                  {/* Column 2: Role & Authentication */}
                  <div className="col-12 col-md-4 surveyor-col-divider">
                    <div className="field-label mb-1">Technical Authority</div>
                    <div className="small text-dark">
                      Status: <strong className="text-success">Active & Verified</strong>
                    </div>
                    <div className="small text-muted mt-1">
                      Jurisdiction: Pre-Policy Vehicle & Accident Claim Surveys
                    </div>
                  </div>

                  {/* Column 3: Management Operations */}
                  <div className="col-12 col-md-3">
                    <div className="field-label mb-2">Actions</div>
                    <div className="d-flex gap-2">
                      <button
                        className="btn btn-outline-primary btn-sm w-100 fw-semibold"
                        onClick={() => {
                          dispatch(setSelectedInspector(insp));
                          window.scrollTo({ top: 0, behavior: "smooth" });
                        }}
                      >
                        ✏️ Edit Profile
                      </button>
                      <button
                        className="btn btn-outline-danger btn-sm w-100 fw-semibold"
                        onClick={() => handleDelete(insp.id, insp.name)}
                      >
                        🗑️ Delete
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Pagination Strip (‹ Prev, Numbers, Next ›) */}
      <div className="d-flex justify-content-between align-items-center flex-wrap px-4 py-3 border-top bg-white">
        <div className="text-muted small">
          Showing <strong>{totalRecords === 0 ? 0 : page * pageSize + 1}</strong> to{" "}
          <strong>{Math.min((page + 1) * pageSize, totalRecords)}</strong> of{" "}
          <strong>{totalRecords}</strong> inspectors
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
    </div>
  );
}

export default InspectorDirectorySection;