// src/utils/errorHandler.js

/**
 * Extracts a user-friendly error message from Axios error responses
 * matching the Spring Boot GlobalExceptionHandler format.
 *
 * @param {any} err - The error object caught in the catch block
 * @param {string} fallbackMsg - Default fallback if response is empty
 * @returns {string} - Clean error message string
 */
export const parseBackendError = (err, fallbackMsg = "Operation failed. Please try again.") => {
  // 1. Network failure / Server completely unreachable
  if (!err?.response) {
    return err?.message || "Unable to connect to server. Please check your network connection.";
  }

  const { data } = err.response;

  // 2. Fallback if response body is empty or plain string
  if (!data) return fallbackMsg;
  if (typeof data === "string") return data;

  // 3. Scenario A: MethodArgumentNotValidException Map<String, String>
  // Shape: { "panNo": "invalid PAN format", "phoneNo": "must be 10 digits" }
  if (typeof data === "object" && !data.message) {
    const fieldErrors = Object.entries(data);
    if (fieldErrors.length > 0) {
      // Formats as: "panNo: invalid PAN format, phoneNo: must be 10 digits"
      return fieldErrors
        .map(([field, msg]) => `${field}: ${msg}`)
        .join(" | ");
    }
  }

  // 4. Scenario B: ErrorDto
  // Shape: { message: "...", comments: "...", timestamp: "..." }
  if (data.message) {
    return data.message;
  }

  return fallbackMsg;
};