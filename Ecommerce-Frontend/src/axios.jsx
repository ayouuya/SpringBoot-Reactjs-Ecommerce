import axios from "axios";

const API = axios.create({
  baseURL: "http://localhost:8080/api",
});

const AUTH_STORAGE_KEY = "digitech.auth";

API.interceptors.request.use((config) => {
  const stored = localStorage.getItem(AUTH_STORAGE_KEY);
  if (stored) {
    try {
      const auth = JSON.parse(stored);
      if (auth?.email) {
        config.headers["X-USER-EMAIL"] = auth.email;
      }
      if (auth?.role) {
        config.headers["X-USER-ROLE"] = auth.role;
      }
    } catch (error) {
      localStorage.removeItem(AUTH_STORAGE_KEY);
    }
  }
  return config;
});

delete API.defaults.headers.common["Authorization"];
export default API;
