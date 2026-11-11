import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  list: [],
  paginationInfo: {
    totalPages: 0,
    totalRecords: 0,
    numRecordsInPage: 0,
  },
  selectedInspector: null,
  loading: false,
};

const inspectorSlice = createSlice({
  name: "inspector",
  initialState,
  reducers: {
    setLoading: (state, action) => {
      state.loading = action.payload;
    },
    setInspectors: (state, action) => {
    
      if (action.payload && action.payload.list) {
        state.list = action.payload.list;
        state.paginationInfo = action.payload.paginationInfo || initialState.paginationInfo;
      } else if (Array.isArray(action.payload)) {
        state.list = action.payload;
      }
      state.loading = false;
    },
    addInspector: (state, action) => {
      state.list.unshift(action.payload);
      state.paginationInfo.totalRecords += 1;
      state.loading = false;
    },
    setSelectedInspector: (state, action) => {
      state.selectedInspector = action.payload;
    },
    updateInspector: (state, action) => {
      const idx = state.list.findIndex((i) => i.id === action.payload.id);
      if (idx !== -1) {
        state.list[idx] = action.payload;
      }
      state.selectedInspector = null;
      state.loading = false;
    },
    deleteInspector: (state, action) => {
      state.list = state.list.filter((i) => i.id !== action.payload);
      state.paginationInfo.totalRecords = Math.max(0, state.paginationInfo.totalRecords - 1);
      state.loading = false;
    },
  },
});

export const {
  setLoading,
  setInspectors,
  addInspector,
  setSelectedInspector,
  updateInspector,
  deleteInspector,
} = inspectorSlice.actions;

export default inspectorSlice.reducer;