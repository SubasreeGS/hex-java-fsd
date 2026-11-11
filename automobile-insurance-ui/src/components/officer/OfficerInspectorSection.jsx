import { useEffect, useState, useCallback } from "react";
import { useParams } from "react-router";
import { useDispatch, useSelector } from "react-redux";
import { API_SERVER_URL_DEV } from "../../../config";
import axios from "axios";
import { setInspectors, setLoading } from "../../store/inspectorSlice";
import AddInspector from "./AddInspector";
import InspectorDirectorySection from "./InspectorDirectorySection";
import "../../styles/OfficerInspectorSection.css";

function OfficerInspectorSection() {
  const params = useParams();
  const token = params.token || localStorage.getItem("token");
  const dispatch = useDispatch();

  // Pagination State
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(5);

  const paginationInfo = useSelector((state) => state.inspector.paginationInfo);

  const loadInspectors = useCallback(async () => {
    dispatch(setLoading(true));
    try {
      const resp = await axios.get(
        `${API_SERVER_URL_DEV}/api/inspector/paginated?page=${page}&size=${pageSize}`,
        {
          headers: { Authorization: "Bearer " + token },
        }
      );
      dispatch(setInspectors(resp.data));
    } catch (err) {
      console.error("Failed to fetch paginated inspectors:", err);
      dispatch(
        setInspectors({
          list: [],
          paginationInfo: { totalPages: 0, totalRecords: 0, numRecordsInPage: 0 },
        })
      );
    }
  }, [page, pageSize, token, dispatch]);

  useEffect(() => {
    loadInspectors();
  }, [loadInspectors]);

  const handleAfterAction = () => {
    // Return to page 0 to show the newest added record at the top
    setPage(0);
    loadInspectors();
  };

  return (
    <div className="officer-inspector-panel">
      {/* Header Bar */}
      <div className="inspector-panel-header d-flex flex-wrap justify-content-between align-items-center gap-2 mb-4">
        <div>
          <h2 className="h5 fw-bold text-dark mb-1">Field Inspector Management</h2>
          <p className="text-muted small mb-0">
            Onboard certified field inspectors and maintain technical survey personnel.
          </p>
        </div>
        <button className="btn btn-outline-secondary btn-sm" onClick={handleAfterAction}>
          Refresh Personnel
        </button>
      </div>

      {/* Onboarding / Profile Form */}
      <div className="mb-4">
        <AddInspector onInspectorAdded={handleAfterAction} />
      </div>

      <hr className="my-4 text-secondary" style={{ opacity: 0.15 }} />

      {/* Directory Cards Section */}
      <div className="row">
        <div className="col-12">
          <InspectorDirectorySection
            page={page}
            pageSize={pageSize}
            setPage={setPage}
            setPageSize={setPageSize}
            totalPages={paginationInfo?.totalPages || 0}
            totalRecords={paginationInfo?.totalRecords || 0}
            onRefresh={loadInspectors}
          />
        </div>
      </div>
    </div>
  );
}

export default OfficerInspectorSection;