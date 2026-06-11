import axios from "axios";

const API = axios.create({
  baseURL: "http://localhost:8080/api",
});

const AUTH_STORAGE_KEY = "digitech.auth";

export const getApiErrorMessage = (error, fallback = "An unexpected error occurred.") => {
  const responseData = error?.response?.data;

  if (typeof responseData === "string" && responseData.trim()) {
    return responseData;
  }
  if (responseData?.message) {
    return responseData.message;
  }
  if (error?.message) {
    return error.message;
  }
  return fallback;
};

API.interceptors.request.use((config) => {
  const stored = localStorage.getItem(AUTH_STORAGE_KEY);
  if (stored) {
    try {
      const auth = JSON.parse(stored);
      if (auth?.token) {
        const tokenType = auth.tokenType || "Bearer";
        config.headers.Authorization = `${tokenType} ${auth.token}`;
      }
    } catch (error) {
      localStorage.removeItem(AUTH_STORAGE_KEY);
    }
  }
  return config;
});

API.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      localStorage.removeItem(AUTH_STORAGE_KEY);
    }
    return Promise.reject(error);
  }
);

export default API;
