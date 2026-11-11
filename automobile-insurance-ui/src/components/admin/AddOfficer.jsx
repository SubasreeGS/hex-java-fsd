import { useState } from "react"
import { API_SERVER_URL_DEV } from "../../../config"
import axios from "axios"
import Swal from "sweetalert2"
import "../../styles/AddOfficer.css"
import { parseBackendError } from "../../utils/errorHandler"

function AddOfficer({ onOfficerAdded }) {
  const [name, setName] = useState("")
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [errMsg, setErrMsg] = useState("")

  const addOfficerUrl = API_SERVER_URL_DEV + "/api/officer/add"

  const addOfficer = async () => {
    setErrMsg("")
    const memToken = localStorage.getItem("token")

    let officerDto = {
      username: username.trim(),
      password: password,
      name: name.trim(),
    }

    try {
      const resp = await axios.post(addOfficerUrl, officerDto, {
        headers: { Authorization: "Bearer " + memToken },
      })

      // Update parent state in AdminDashboard
      if (onOfficerAdded) {
        onOfficerAdded(resp.data || officerDto)
      }

      // SweetAlert2 notification
      Swal.fire({
        title: "Officer Registered!",
        text: `${name.trim()} has been added successfully.`,
        icon: "success",
        confirmButtonColor: "#0f172a",
        confirmButtonText: "OK",
        timer: 2500,
        timerProgressBar: true,
      })

      // Clear inputs
      setName("")
      setUsername("")
      setPassword("")
    } 
    catch (err) {
  const backendMessage = parseBackendError(err, "Failed to register officer.");
  setErrMsg(backendMessage);

  Swal.fire({
    title: "Registration Failed",
    text: backendMessage,
    icon: "error",
    confirmButtonColor: "#dc2626",
    confirmButtonText: "Close",
  });
}
  }

  return (
    <div className="add-officer-btn-container">
      {/* Button to open Modal */}
      <button
        className="btn btn-primary fw-semibold"
        onClick={() => {
          setName("")
          setUsername("")
          setPassword("")
          setErrMsg("")
        }}
        data-bs-toggle="modal"
        data-bs-target="#officerBackdrop"
      >
        + Add Officer
      </button>

      {/* Modal Dialog */}
      <div
        className="modal fade"
        id="officerBackdrop"
        tabIndex="-1"
        aria-labelledby="officerBackdropLabel"
        aria-hidden="true"
        data-bs-backdrop="static"
        data-bs-keyboard="false"
      >
        <div className="modal-dialog">
          <div className="modal-content">
            <div className="modal-header modal-header-admin">
              <h1 className="modal-title fs-5" id="officerBackdropLabel">
                Add Officer
              </h1>
              <button
                type="button"
                className="btn-close"
                data-bs-dismiss="modal"
                aria-label="Close"
                onClick={(e) => e.currentTarget.blur()}
              ></button>
            </div>
            <div className="modal-body">
              {errMsg && (
                <div className="alert alert-danger py-2 small mb-3">
                  {errMsg}
                </div>
              )}

              <div className="mb-3">
                <label className="officer-form-label">Full Name</label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="form-control officer-form-control"
                  placeholder="Enter officer full name"
                  required
                />
              </div>

              <div className="mb-3">
                <label className="officer-form-label">Username</label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="form-control officer-form-control"
                  placeholder="Enter officer username"
                  required
                />
              </div>

              <div className="mb-3">
                <label className="officer-form-label">Password</label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="form-control officer-form-control"
                  placeholder="••••••••"
                  required
                />
              </div>
            </div>
            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-secondary"
                data-bs-dismiss="modal"
                onClick={(e) => e.currentTarget.blur()}
              >
                Close
              </button>
              <button
                type="button"
                className="btn btn-primary"
                onClick={(e) => {
                  e.currentTarget.blur()
                  addOfficer()
                }}
                data-bs-dismiss="modal"
              >
                Add
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default AddOfficer