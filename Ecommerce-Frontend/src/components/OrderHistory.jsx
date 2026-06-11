import { useEffect, useState } from "react";
import API, { getApiErrorMessage } from "../axios";
import { formatCurrency } from "../utils/formatCurrency";

const OrderHistory = () => {
  const [orders, setOrders] = useState([]);
  const [status, setStatus] = useState({ type: "", message: "" });

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const response = await API.get("/orders/my-orders");
        setOrders(response.data || []);
      } catch (error) {
        setStatus({
          type: "error",
          message: getApiErrorMessage(error, "Unable to load orders."),
        });
      }
    };

    fetchOrders();
  }, []);

  return (
    <div className="order-history">
      <div className="order-history-header">
        <h2>Order History</h2>
        <p>Track every purchase in MAD.</p>
      </div>
      {status.message && (
        <div className={`status-banner ${status.type}`}>{status.message}</div>
      )}
      {orders.length === 0 ? (
        <div className="order-empty">
          <h4>No orders yet</h4>
          <p>Complete a purchase to see it listed here.</p>
        </div>
      ) : (
        <div className="order-list">
          {orders.map((order) => (
            <div key={order.id} className="order-card">
              <div className="order-card-header">
                <div>
                  <h5>Order {order.orderNumber}</h5>
                  <span>
                    {order.createdAt
                      ? new Date(order.createdAt).toLocaleDateString()
                      : ""}
                  </span>
                </div>
                <span className={`order-status ${order.status?.toLowerCase()}`}>
                  {order.status}
                </span>
              </div>
              <div className="order-items">
                {order.items.map((item) => (
                  <div key={item.productId} className="order-item">
                    <span>{item.name}</span>
                    <span>
                      {item.quantity} x {formatCurrency(item.unitPrice)}
                    </span>
                    <span>{formatCurrency(item.lineTotal)}</span>
                  </div>
                ))}
              </div>
              <div className="order-total">
                <span>Total</span>
                <span>{formatCurrency(order.total)}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default OrderHistory;
