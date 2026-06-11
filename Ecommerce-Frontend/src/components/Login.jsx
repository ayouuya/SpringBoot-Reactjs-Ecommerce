import { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthContext from "../Context/AuthContext";
import { getApiErrorMessage } from "../axios";

const Login = () => {
  const { login } = useContext(AuthContext);
  const [form, setForm] = useState({
    email: "",
    password: "",
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
    if (!form.password) {
      setStatus({ type: "error", message: "Password is required." });
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
      setStatus({
        type: "error",
        message: getApiErrorMessage(error, "Login failed."),
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h2>Sign In</h2>
        <p>Sign in with your DIGITECH account.</p>
        {status.message && (
          <div className={`status-banner ${status.type}`}>{status.message}</div>
        )}
        <form onSubmit={handleSubmit} className="login-form">
          <input
            type="email"
            name="email"
            value={form.email}
            onChange={handleChange}
            placeholder="Email"
            required
          />
          <input
            type="password"
            name="password"
            value={form.password}
            onChange={handleChange}
            placeholder="Password"
            required
          />
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? "Signing in..." : "Login"}
          </button>
        </form>
        <small style={{ display: "block", marginTop: "1rem", opacity: 0.8 }}>
          Demo accounts: sara@digitech.ma / User@12345 or admin@digitech.ma / Admin@12345
        </small>
      </div>
    </div>
  );
};

export default Login;
