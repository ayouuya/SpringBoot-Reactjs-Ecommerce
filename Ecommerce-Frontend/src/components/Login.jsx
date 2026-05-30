import { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthContext from "../Context/AuthContext";

const Login = () => {
  const { login } = useContext(AuthContext);
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    role: "USER",
  });
  const [status, setStatus] = useState({ type: "", message: "" });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!form.email) {
      setStatus({ type: "error", message: "Email is required." });
      return;
    }

    try {
      setIsSubmitting(true);
      const user = await login(form);
      setStatus({ type: "success", message: `Welcome ${user.fullName || "back"}!` });
      if (user.role === "ADMIN") {
        navigate("/admin", { replace: true });
      } else {
        navigate("/", { replace: true });
      }
    } catch (error) {
      const message = error?.response?.data || error.message;
      setStatus({ type: "error", message: message || "Login failed." });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h2>Sign In</h2>
        <p>Use your role to enter the DIGITECH platform.</p>
        {status.message && (
          <div className={`status-banner ${status.type}`}>{status.message}</div>
        )}
        <form onSubmit={handleSubmit} className="login-form">
          <input
            type="text"
            name="fullName"
            value={form.fullName}
            onChange={handleChange}
            placeholder="Full name"
          />
          <input
            type="email"
            name="email"
            value={form.email}
            onChange={handleChange}
            placeholder="Email"
            required
          />
          <select name="role" value={form.role} onChange={handleChange}>
            <option value="USER">User</option>
            <option value="ADMIN">Admin</option>
          </select>
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? "Signing in..." : "Login"}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Login;
