import { useEffect, useState } from "react";
import { useParams } from "react-router";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import Swal from "sweetalert2";
import "../../styles/ProfileSection.css";

function ProfileSection() {
  const params = useParams();
  const token = params.token || localStorage.getItem("token");

  const profileUrl = API_SERVER_URL_DEV + "/api/customer/profile/v2";

  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [saving, setSaving] = useState(false);

  // Aligned with CustomerResponseDto
  const [profileData, setProfileData] = useState({
    id: null,
    name: "",
    username: "",
    phoneNo: "",
    address: "",
    dob: "",
  });

  // Aligned with CustomerUpdateDto
  const [formData, setFormData] = useState({
    name: "",
    phoneNo: "",
    address: "",
    dob: "",
  });

  const fetchProfile = async () => {
    setLoading(true);
    try {
      const resp = await axios.get(profileUrl, {
        headers: { Authorization: "Bearer " + token },
      });

      const data = {
        id: resp.data.id ?? null,
        name: resp.data.name || "",
        username: resp.data.username || "",
        phoneNo: resp.data.phoneNo || "",
        address: resp.data.address || "",
        dob: resp.data.dob || "",
      };

      setProfileData(data);
      setFormData({
        name: data.name,
        phoneNo: data.phoneNo,
        address: data.address,
        dob: data.dob,
      });
    } catch (err) {
      Swal.fire({
        title: "Error",
        text: err?.response?.data?.message || "Failed to load customer profile.",
        icon: "error",
        confirmButtonColor: "#0f172a",
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, [token]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleCancel = () => {
    setFormData({
      name: profileData.name,
      phoneNo: profileData.phoneNo,
      address: profileData.address,
      dob: profileData.dob,
    });
    setIsEditing(false);
  };

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setSaving(true);

    // Payload mapped strictly to CustomerUpdateDto
    const updateDto = {
      name: formData.name.trim(),
      phoneNo: formData.phoneNo.trim(),
      address: formData.address.trim(),
      dob: formData.dob || null,
    };

    try {
      await axios.put(profileUrl, updateDto, {
        headers: { Authorization: "Bearer " + token },
      });

      setProfileData((prev) => ({
        ...prev,
        ...updateDto,
      }));

      setIsEditing(false);

      Swal.fire({
        title: "Profile Updated",
        text: "Your profile details have been successfully saved.",
        icon: "success",
        confirmButtonColor: "#0f172a",
        confirmButtonText: "OK",
        timer: 2000,
        timerProgressBar: true,
      });
    } catch (err) {
      Swal.fire({
        title: "Update Failed",
        text: err?.response?.data?.message || "Could not update profile details.",
        icon: "error",
        confirmButtonColor: "#dc2626",
      });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="profile-card p-5 text-center text-muted">
        <div className="spinner-border spinner-border-sm text-primary me-2" role="status"></div>
        Loading profile details...
      </div>
    );
  }

  return (
    <div className="profile-card">
      <div className="profile-card-header d-flex justify-content-between align-items-center">
        <div>
          <h2 className="h5 fw-bold text-dark mb-1">Customer Profile</h2>
          <p className="text-muted small mb-0">View and update your personal contact details</p>
        </div>
        {!isEditing && (
          <button className="btn btn-primary btn-sm fw-semibold" onClick={() => setIsEditing(true)}>
            Edit Profile
          </button>
        )}
      </div>

      <div className="p-4">
        <div className="row g-4">
          {/* Left Column: Avatar & Identifier */}
          <div className="col-12 col-md-4 text-center text-md-start">
            <div className="profile-summary-box d-flex flex-column align-items-center text-center">
              <div className="profile-avatar-circle mb-3">
                {profileData.name ? profileData.name.charAt(0).toUpperCase() : "U"}
              </div>
              <h3 className="h6 fw-bold text-dark mb-1">{profileData.name || "Customer"}</h3>
              <p className="text-muted small mb-2">@{profileData.username || "username"}</p>
              {profileData.id && (
                <span className="text-muted small mb-2">Customer ID: #{profileData.id}</span>
              )}
              <span className="badge bg-primary-subtle text-primary border border-primary-subtle px-2 py-1 small">
                VERIFIED CUSTOMER
              </span>
            </div>
          </div>

          {/* Right Column: Details View or Edit Form */}
          <div className="col-12 col-md-8">
            {!isEditing ? (
              /* Read-Only Details View (CustomerResponseDto) */
              <div className="row g-3">
                <div className="col-12 col-sm-6">
                  <div className="profile-info-group">
                    <div className="profile-info-label">Full Name</div>
                    <div className="profile-info-value">{profileData.name || "--"}</div>
                  </div>
                </div>

                <div className="col-12 col-sm-6">
                  <div className="profile-info-group">
                    <div className="profile-info-label">Username</div>
                    <div className="profile-info-value">{profileData.username || "--"}</div>
                  </div>
                </div>

                <div className="col-12 col-sm-6">
                  <div className="profile-info-group">
                    <div className="profile-info-label">Contact / Phone</div>
                    <div className="profile-info-value">{profileData.phoneNo || "--"}</div>
                  </div>
                </div>

                <div className="col-12 col-sm-6">
                  <div className="profile-info-group">
                    <div className="profile-info-label">Date of Birth</div>
                    <div className="profile-info-value">{profileData.dob || "--"}</div>
                  </div>
                </div>

                <div className="col-12">
                  <div className="profile-info-group">
                    <div className="profile-info-label">Address</div>
                    <div className="profile-info-value">{profileData.address || "--"}</div>
                  </div>
                </div>
              </div>
            ) : (
              /* Inline Editable Form (CustomerUpdateDto) */
              <form onSubmit={handleSaveProfile}>
                <div className="row g-3">
                  <div className="col-12 col-sm-6">
                    <label className="profile-form-label">Full Name</label>
                    <input
                      type="text"
                      name="name"
                      className="form-control profile-form-control"
                      value={formData.name}
                      onChange={handleInputChange}
                      required
                    />
                  </div>

                  <div className="col-12 col-sm-6">
                    <label className="profile-form-label">Username (Read-Only)</label>
                    <input
                      type="text"
                      className="form-control profile-form-control bg-light"
                      value={profileData.username}
                      disabled
                    />
                  </div>

                  <div className="col-12 col-sm-6">
                    <label className="profile-form-label">Contact Number (10 Digits)</label>
                    <input
                      type="tel"
                      name="phoneNo"
                      pattern="[0-9]{10}"
                      maxLength="10"
                      className="form-control profile-form-control"
                      value={formData.phoneNo}
                      onChange={handleInputChange}
                      required
                    />
                  </div>

                  <div className="col-12 col-sm-6">
                    <label className="profile-form-label">Date of Birth</label>
                    <input
                      type="date"
                      name="dob"
                      max={new Date().toISOString().split("T")[0]}
                      className="form-control profile-form-control"
                      value={formData.dob}
                      onChange={handleInputChange}
                      required
                    />
                  </div>

                  <div className="col-12">
                    <label className="profile-form-label">Address</label>
                    <textarea
                      name="address"
                      rows="3"
                      className="form-control profile-form-control"
                      value={formData.address}
                      onChange={handleInputChange}
                      required
                    />
                  </div>
                </div>

                <div className="d-flex justify-content-end gap-2 mt-4 pt-2 border-top">
                  <button
                    type="button"
                    className="btn btn-outline-secondary btn-sm px-3"
                    onClick={handleCancel}
                    disabled={saving}
                  >
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary btn-sm px-3 fw-semibold" disabled={saving}>
                    {saving ? "Saving..." : "Save Changes"}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProfileSection;