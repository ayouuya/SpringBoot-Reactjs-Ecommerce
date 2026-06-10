import "./App.css";
import React, { useState } from "react";
import Home from "./components/Home";
import Navbar from "./components/Navbar";
import Cart from "./components/Cart";
import Product from "./components/Product";
import Login from "./components/Login";
import OrderHistory from "./components/OrderHistory";
import ProtectedRoute from "./components/ProtectedRoute";
import StoreRoute from "./components/StoreRoute";
import AdminLayout from "./components/admin/AdminLayout";
import AdminProducts from "./components/admin/AdminProducts";
import AdminAddProduct from "./components/admin/AdminAddProduct";
import AdminEditProduct from "./components/admin/AdminEditProduct";
import { BrowserRouter, Routes, Route, useLocation } from "react-router-dom";
import ChatBot from "./components/ChatBot";

import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";


const AppRoutes = ({ selectedCategory, onSelectCategory }) => {
  const location = useLocation();
  const isAdminRoute = location.pathname.startsWith("/admin");

  return (
    <>
      {!isAdminRoute && <Navbar onSelectCategory={onSelectCategory} />}
      <Routes>
        <Route
          path="/"
          element={
            <StoreRoute>
              <Home selectedCategory={selectedCategory} />
            </StoreRoute>
          }
        />
        <Route
          path="/product/:id"
          element={
            <StoreRoute>
              <Product />
            </StoreRoute>
          }
        />
        <Route
          path="/cart"
          element={
            <ProtectedRoute requiredRole="USER">
              <Cart />
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders"
          element={
            <ProtectedRoute requiredRole="USER">
              <OrderHistory />
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<Login />} />

        <Route
          path="/admin"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <AdminLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<AdminProducts />} />
          <Route path="products/new" element={<AdminAddProduct />} />
          <Route path="products/:id" element={<AdminEditProduct />} />
        </Route>
      </Routes>
      <ChatBot />
    </>

  );
};

function App() {
  const [selectedCategory, setSelectedCategory] = useState("");

  const handleCategorySelect = (category) => {
    setSelectedCategory(category);
  };

  return (
    <BrowserRouter>
      <AppRoutes
        selectedCategory={selectedCategory}
        onSelectCategory={handleCategorySelect}
      />
    </BrowserRouter>
  );
}

export default App;
