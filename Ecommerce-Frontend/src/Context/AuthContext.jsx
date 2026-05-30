import { createContext, useCallback, useMemo, useState } from "react";
import API from "../axios";

const AUTH_STORAGE_KEY = "digitech.auth";

const AuthContext = createContext({
  user: null,
  isAuthenticated: false,
  isAdmin: false,
  isUser: false,
  login: async () => {},
  logout: () => {},
});

const readStoredUser = () => {
  const stored = localStorage.getItem(AUTH_STORAGE_KEY);
  if (!stored) {
    return null;
  }
  try {
    return JSON.parse(stored);
  } catch (error) {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    return null;
  }
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => readStoredUser());

  const login = useCallback(async ({ fullName, email, role }) => {
    const payload = {
      fullName,
      email,
      role: role ? role.toUpperCase() : "USER",
    };
    const response = await API.post("/auth/login", payload);
    setUser(response.data);
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(response.data));
    return response.data;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    setUser(null);
  }, []);

  const value = useMemo(() => {
    const isAuthenticated = Boolean(user);
    const isAdmin = user?.role === "ADMIN";
    const isUser = user?.role === "USER";
    return {
      user,
      isAuthenticated,
      isAdmin,
      isUser,
      login,
      logout,
    };
  }, [user, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export default AuthContext;
