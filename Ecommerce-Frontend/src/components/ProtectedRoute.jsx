import { useContext } from "react";
import { Navigate } from "react-router-dom";
import AuthContext from "../Context/AuthContext";

const ProtectedRoute = ({ children, requiredRole }) => {
  const { isAuthenticated, isAdmin, isUser } = useContext(AuthContext);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole === "ADMIN" && !isAdmin) {
    return <Navigate to="/" replace />;
  }

  if (requiredRole === "USER" && !isUser) {
    return <Navigate to="/admin" replace />;
  }

  return children;
};

export default ProtectedRoute;
