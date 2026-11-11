import { configureStore } from "@reduxjs/toolkit";
import inspectorReducer from "./inspectorSlice";

export const store = configureStore({
  reducer: {
    inspector: inspectorReducer,
  },
});

export default store;