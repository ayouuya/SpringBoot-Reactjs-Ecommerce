import { useContext } from "react";
import { Navigate } from "react-router-dom";
import AuthContext from "../Context/AuthContext";

const StoreRoute = ({ children }) => {
  const { isAdmin } = useContext(AuthContext);

  if (isAdmin) {
    return <Navigate to="/admin" replace />;
  }

  return children;
};

export default StoreRoute;
