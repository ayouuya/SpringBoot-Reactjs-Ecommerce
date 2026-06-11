import React, { useContext, useEffect, useState } from "react";
import AppContext from "../Context/Context";
import AuthContext from "../Context/AuthContext";
import CheckoutPopup from "./CheckoutPopup";
import { formatCurrency } from "../utils/formatCurrency";
import unplugged from "../assets/unplugged.png";

const Cart = () => {
  const {
    cart,
    updateCartItem,
    removeFromCart,
    clearCart,
    checkout,
    isCartLoading,
  } = useContext(AppContext);
  const { user } = useContext(AuthContext);
  const [cartItems, setCartItems] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [statusMessage, setStatusMessage] = useState({ type: "", text: "" });
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setCartItems(cart.items || []);
  }, [cart.items]);

  const handleIncreaseQuantity = async (item) => {
    if (item.quantity >= item.stockQuantity) {
      setStatusMessage({
        type: "error",
        text: "Cannot add more than available stock.",
      });
      return;
    }
    await updateCartItem(item.productId, item.quantity + 1);
  };

  const handleDecreaseQuantity = async (item) => {
    const nextQuantity = Math.max(item.quantity - 1, 1);
    await updateCartItem(item.productId, nextQuantity);
  };

  const handleRemoveFromCart = async (productId) => {
    await removeFromCart(productId);
  };

  const handleCheckoutConfirm = async (payload) => {
    setIsSubmitting(true);
    const result = await checkout(payload);
    setIsSubmitting(false);

    if (result.success) {
      setStatusMessage({
        type: "success",
        text: `Order ${result.orderNumber} confirmed.`,
      });
      setShowModal(false);
    } else {
      setStatusMessage({
        type: "error",
        text: result.message || "Payment failed.",
      });
    }

    return result;
  };

  if (isCartLoading) {
    return (
      <div className="cart-page">
        <h4>Loading cart...</h4>
      </div>
    );
  }

  return (
    <div className="cart-page">
      <div className="cart-header">
        <h2>Shopping Cart</h2>
        {cart.items.length > 0 && (
          <button className="btn btn-outline-dark" onClick={clearCart}>
            Clear Cart
          </button>
        )}
      </div>

      {statusMessage.text && (
        <div className={`status-banner ${statusMessage.type}`}>
          {statusMessage.text}
        </div>
      )}

      {cartItems.length === 0 ? (
        <div className="cart-empty">
          <h4>Your cart is empty</h4>
          <p>Browse the catalog and add your favorite products.</p>
        </div>
      ) : (
        <div className="cart-content">
          <div className="cart-items">
            {cartItems.map((item) => (
              <div key={item.productId} className="cart-item">
                <div className="cart-item-image">
                  {item.imageUrl ? (
                    <img
                      src={item.imageUrl}
                      alt={item.name}
                      onError={(event) => {
                        event.currentTarget.onerror = null;
                        event.currentTarget.src = unplugged;
                      }}
                    />
                  ) : (
                    <div className="image-placeholder">No image</div>
                  )}
                </div>
                <div className="cart-item-details">
                  <h5>{item.name}</h5>
                  <p className="cart-item-brand">{item.brand}</p>
                  <p className="cart-item-price">
                    {formatCurrency(item.unitPrice)}
                  </p>
                </div>
                <div className="cart-item-quantity">
                  <button
                    className="qty-btn"
                    onClick={() => handleDecreaseQuantity(item)}
                  >
                    -
                  </button>
                  <span>{item.quantity}</span>
                  <button
                    className="qty-btn"
                    onClick={() => handleIncreaseQuantity(item)}
                  >
                    +
                  </button>
                </div>
                <div className="cart-item-total">
                  {formatCurrency(item.lineTotal)}
                </div>
                <button
                  className="remove-btn"
                  onClick={() => handleRemoveFromCart(item.productId)}
                >
                  Remove
                </button>
              </div>
            ))}
          </div>
          <div className="cart-summary">
            <div className="summary-card">
              <h4>Order Summary</h4>
              <div className="summary-line">
                <span>Items</span>
                <span>{cart.itemCount}</span>
              </div>
              <div className="summary-line">
                <span>Subtotal</span>
                <span>{formatCurrency(cart.subtotal)}</span>
              </div>
              <div className="summary-line total">
                <span>Total</span>
                <span>{formatCurrency(cart.total)}</span>
              </div>
              <button
                className="btn btn-primary w-100"
                onClick={() => setShowModal(true)}
              >
                Proceed to Checkout
              </button>
            </div>
          </div>
        </div>
      )}

      <CheckoutPopup
        show={showModal}
        handleClose={() => setShowModal(false)}
        cartItems={cartItems}
        totalPrice={cart.total}
        currency={cart.currency}
        onConfirm={handleCheckoutConfirm}
        isSubmitting={isSubmitting}
        prefillCustomer={{
          fullName: user?.fullName || "",
          email: user?.email || "",
        }}
      />
    </div>
  );
};

export default Cart;
