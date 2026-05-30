import React, { useContext, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import API from "../axios";
import AppContext from "../Context/Context";
import AuthContext from "../Context/AuthContext";
// import { json } from "react-router-dom";
// import { BiSunFill, BiMoon } from "react-icons/bi";

const Navbar = ({ onSelectCategory }) => {
  const getInitialTheme = () => {
    const storedTheme = localStorage.getItem("theme");
    return storedTheme ? storedTheme : "light-theme";
  };
  const { cart } = useContext(AppContext);
  const { user, isAdmin, isUser, isAuthenticated, logout } = useContext(AuthContext);
  const [selectedCategory, setSelectedCategory] = useState("");
  const [theme, setTheme] = useState(getInitialTheme());
  const [input, setInput] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [noResults, setNoResults] = useState(false);
  const [showSearchResults,setShowSearchResults] = useState(false)
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAdmin) {
      fetchData();
    }
  }, [isAdmin]);

  const fetchData = async (value) => {
    try {
      const response = await API.get("/products");
      setSearchResults(response.data);
      console.log(response.data);
    } catch (error) {
      console.error("Error fetching data:", error);
    }
  };

  const handleChange = async (value) => {
    setInput(value);
    if (value.length >= 1) {
      setShowSearchResults(true)
    try {
      const response = await API.get(`/products/search?keyword=${value}`);
      setSearchResults(response.data);
      setNoResults(response.data.length === 0);
      console.log(response.data);
    } catch (error) {
      console.error("Error searching:", error);
    }
    } else {
      setShowSearchResults(false);
      setSearchResults([]);
      setNoResults(false);
    }
  };

  
  // const handleChange = async (value) => {
  //   setInput(value);
  //   if (value.length >= 1) {
  //     setShowSearchResults(true);
  //     try {
  //       let response;
  //       if (!isNaN(value)) {
  //         // Input is a number, search by ID
  //         response = await axios.get(`http://localhost:8080/api/products/search?id=${value}`);
  //       } else {
  //         // Input is not a number, search by keyword
  //         response = await axios.get(`http://localhost:8080/api/products/search?keyword=${value}`);
  //       }

  //       const results = response.data;
  //       setSearchResults(results);
  //       setNoResults(results.length === 0);
  //       console.log(results);
  //     } catch (error) {
  //       console.error("Error searching:", error.response ? error.response.data : error.message);
  //     }
  //   } else {
  //     setShowSearchResults(false);
  //     setSearchResults([]);
  //     setNoResults(false);
  //   }
  // };

  const handleCategorySelect = (category) => {
    setSelectedCategory(category);
    onSelectCategory(category);
  };
  const toggleTheme = () => {
    const newTheme = theme === "dark-theme" ? "light-theme" : "dark-theme";
    setTheme(newTheme);
    localStorage.setItem("theme", newTheme);
  };

  useEffect(() => {
    document.body.className = theme;
  }, [theme]);

  const categories = ["Laptop", "Headphone", "Mobile", "Electronics", "Toys", "Fashion"];

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };
  return (
    <>
      <header>
        <nav className="navbar navbar-expand-lg fixed-top">
          <div className="container-fluid">
            <a className="navbar-brand" href="/">
              DIGITECH
            </a>
            <button
              className="navbar-toggler"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#navbarSupportedContent"
              aria-controls="navbarSupportedContent"
              aria-expanded="false"
              aria-label="Toggle navigation"
            >
              <span className="navbar-toggler-icon"></span>
            </button>
            <div
              className="collapse navbar-collapse"
              id="navbarSupportedContent"
            >
              <ul className="navbar-nav me-auto mb-2 mb-lg-0">
                {!isAdmin && (
                  <li className="nav-item">
                    <a className="nav-link active" aria-current="page" href="/">
                      Home
                    </a>
                  </li>
                )}
                {isAdmin && (
                  <li className="nav-item">
                    <a className="nav-link active" aria-current="page" href="/admin">
                      Dashboard
                    </a>
                  </li>
                )}
                {isUser && (
                  <li className="nav-item">
                    <a className="nav-link" href="/orders">
                      Order History
                    </a>
                  </li>
                )}
                {!isAdmin && (
                  <li className="nav-item dropdown">
                    <a
                      className="nav-link dropdown-toggle"
                      href="/"
                      role="button"
                      data-bs-toggle="dropdown"
                      aria-expanded="false"
                    >
                      Categories
                    </a>

                    <ul className="dropdown-menu">
                      {categories.map((category) => (
                        <li key={category}>
                          <button
                            className="dropdown-item"
                            onClick={() => handleCategorySelect(category)}
                          >
                            {category}
                          </button>
                        </li>
                      ))}
                    </ul>
                  </li>
                )}
                <li className="nav-item"></li>
              </ul>
              <button className="theme-btn" onClick={() => toggleTheme()}>
                {theme === "dark-theme" ? (
                  <i className="bi bi-moon-fill"></i>
                ) : (
                  <i className="bi bi-sun-fill"></i>
                )}
              </button>
              <div className="d-flex align-items-center cart">
                {!isAdmin && isUser && (
                  <>
                    <a href="/cart" className="nav-link text-dark">
                      <i
                        className="bi bi-cart me-2"
                        style={{ display: "flex", alignItems: "center" }}
                      >
                        Cart
                      </i>
                    </a>
                    <span className="cart-count">{cart.itemCount || 0}</span>
                  </>
                )}
                {!isAdmin && (
                  <div className="nav-search">
                    <input
                      className="form-control me-2"
                      type="search"
                      placeholder="Search"
                      aria-label="Search"
                      value={input}
                      onChange={(e) => handleChange(e.target.value)}
                    />
                    {showSearchResults && (
                      <ul className="list-group">
                        {searchResults.length > 0 ? (
                          searchResults.map((result) => (
                            <li key={result.id} className="list-group-item">
                              <a href={`/product/${result.id}`} className="search-result-link">
                                <span>{result.name}</span>
                              </a>
                            </li>
                          ))
                        ) : (
                          noResults && (
                            <p className="no-results-message">
                              No product with such name
                            </p>
                          )
                        )}
                      </ul>
                    )}
                  </div>
                )}
                <div className="auth-actions">
                  {isAuthenticated ? (
                    <>
                      <span className="user-chip">
                        {user?.fullName || user?.email}
                      </span>
                      <button className="btn btn-outline-dark" onClick={handleLogout}>
                        Logout
                      </button>
                    </>
                  ) : (
                    <button
                      className="btn btn-primary"
                      onClick={() => navigate("/login")}
                    >
                      Login
                    </button>
                  )}
                </div>
                <div />
              </div>
            </div>
          </div>
        </nav>
      </header>
    </>
  );
};

export default Navbar;
