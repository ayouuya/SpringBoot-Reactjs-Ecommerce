import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useContext } from "react";
import AuthContext from "../../Context/AuthContext";

const AdminLayout = () => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <div className="admin-layout">
      <aside className="admin-sidebar">
        <div className="admin-brand">
          <span>DIGITECH</span>
          <small>Admin Panel</small>
        </div>
        <nav className="admin-nav">
          <NavLink to="/admin" end className={({ isActive }) => (isActive ? "active" : "") }>
            Products
          </NavLink>
          <NavLink
            to="/admin/products/new"
            className={({ isActive }) => (isActive ? "active" : "") }
          >
            Add Product
          </NavLink>
        </nav>
        <div className="admin-footer">
          <div className="admin-user">
            <span>{user?.fullName || "Admin"}</span>
            <small>{user?.email}</small>
          </div>
          <button className="btn btn-outline-dark" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </aside>
      <main className="admin-main">
        <div className="admin-topbar">
          <h3>Dashboard</h3>
        </div>
        <div className="admin-content">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default AdminLayout;
