import { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import API from "../../axios";
import AppContext from "../../Context/Context";
import { formatCurrency } from "../../utils/formatCurrency";

const AdminProducts = () => {
  const { data, refreshData } = useContext(AppContext);
  const [status, setStatus] = useState({ type: "", message: "" });
  const [stockInputs, setStockInputs] = useState({});

  useEffect(() => {
    refreshData();
  }, [refreshData]);

  const handleDelete = async (productId) => {
    try {
      await API.delete(`/product/${productId}`);
      setStatus({ type: "success", message: "Product deleted." });
      refreshData();
    } catch (error) {
      const message = error?.response?.data || error.message;
      setStatus({ type: "error", message: message || "Unable to delete product." });
    }
  };

  const handleStockChange = (productId, value) => {
    setStockInputs((prev) => ({ ...prev, [productId]: value }));
  };

  const handleStockUpdate = async (productId) => {
    const stockQuantity = Number(stockInputs[productId]);
    if (Number.isNaN(stockQuantity)) {
      setStatus({ type: "error", message: "Stock quantity must be a number." });
      return;
    }

    try {
      await API.put(`/product/${productId}/stock`, { stockQuantity });
      setStatus({ type: "success", message: "Stock updated." });
      refreshData();
    } catch (error) {
      const message = error?.response?.data || error.message;
      setStatus({ type: "error", message: message || "Unable to update stock." });
    }
  };

  return (
    <div className="admin-products">
      <div className="admin-products-header">
        <div>
          <h4>Product Inventory</h4>
          <p>Manage products and stock levels.</p>
        </div>
        <Link className="btn btn-primary" to="/admin/products/new">
          Add Product
        </Link>
      </div>
      {status.message && (
        <div className={`status-banner ${status.type}`}>{status.message}</div>
      )}
      <div className="admin-table">
        <div className="admin-table-row admin-table-head">
          <span>Product</span>
          <span>Category</span>
          <span>Price</span>
          <span>Stock</span>
          <span>Status</span>
          <span>Actions</span>
        </div>
        {data.map((product) => (
          <div key={product.id} className="admin-table-row">
            <span>{product.name}</span>
            <span>{product.category}</span>
            <span>{formatCurrency(product.price)}</span>
            <div className="stock-cell">
              <input
                type="number"
                value={
                  stockInputs[product.id] !== undefined
                    ? stockInputs[product.id]
                    : product.stockQuantity
                }
                onChange={(event) => handleStockChange(product.id, event.target.value)}
              />
              <button
                className="btn btn-outline-dark"
                onClick={() => handleStockUpdate(product.id)}
              >
                Update
              </button>
            </div>
            <span>{product.productAvailable ? "Active" : "Out"}</span>
            <div className="admin-actions">
              <Link className="btn btn-outline-dark" to={`/admin/products/${product.id}`}>
                Edit
              </Link>
              <button
                className="btn btn-outline-dark"
                onClick={() => handleDelete(product.id)}
              >
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default AdminProducts;
