import React, { useEffect, useState } from "react";
import { Modal, Button } from "react-bootstrap";
import { formatCurrency } from "../utils/formatCurrency";

const CheckoutPopup = ({
  show,
  handleClose,
  cartItems,
  totalPrice,
  currency,
  onConfirm,
  isSubmitting,
  prefillCustomer,
}) => {
  const [paymentMethod, setPaymentMethod] = useState("CARD");
  const [status, setStatus] = useState({ type: "", message: "" });
  const [customer, setCustomer] = useState({
    fullName: "",
    email: "",
    phone: "",
    address: "",
  });
  const [card, setCard] = useState({
    cardHolder: "",
    cardNumber: "",
    expiryMonth: "",
    expiryYear: "",
    cvv: "",
  });
  const [paypal, setPaypal] = useState({ email: "" });

  useEffect(() => {
    if (!show) {
      setStatus({ type: "", message: "" });
    }
  }, [show]);

  useEffect(() => {
    if (show && prefillCustomer) {
      setCustomer((prev) => ({
        ...prev,
        ...prefillCustomer,
      }));
    }
  }, [show, prefillCustomer]);

  const validateEmail = (value) => {
    if (!value) {
      return false;
    }
    return /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(value);
  };

  const normalizeCardNumber = (value) => value.replace(/\D/g, "");

  const isLuhnValid = (value) => {
    const number = normalizeCardNumber(value);
    let sum = 0;
    let doubleDigit = false;

    for (let index = number.length - 1; index >= 0; index -= 1) {
      let digit = Number(number[index]);
      if (doubleDigit) {
        digit *= 2;
        if (digit > 9) {
          digit -= 9;
        }
      }
      sum += digit;
      doubleDigit = !doubleDigit;
    }

    return sum % 10 === 0;
  };

  const isValidExpiry = (monthValue, yearValue) => {
    const month = Number(monthValue);
    const year = Number(yearValue.length === 2 ? `20${yearValue}` : yearValue);
    if (!Number.isInteger(month) || month < 1 || month > 12 || !Number.isInteger(year)) {
      return false;
    }

    const now = new Date();
    const expiry = new Date(year, month, 0, 23, 59, 59);
    return expiry >= now;
  };

  const validateForm = () => {
    if (!customer.fullName || !customer.email || !customer.address) {
      return "Name, email, and address are required.";
    }
    if (!validateEmail(customer.email)) {
      return "Please enter a valid email address.";
    }

    if (paymentMethod === "CARD") {
      if (!card.cardHolder || !card.cardNumber || !card.expiryMonth || !card.expiryYear || !card.cvv) {
        return "Complete all card fields to continue.";
      }
      const cardNumber = normalizeCardNumber(card.cardNumber);
      if (cardNumber.length < 13 || cardNumber.length > 19 || !isLuhnValid(cardNumber)) {
        return "Invalid card number. For this demo, use 4242 4242 4242 4242.";
      }
      if (!isValidExpiry(card.expiryMonth, card.expiryYear)) {
        return "Enter a valid future expiry date.";
      }
      if (!/^\d{3,4}$/.test(card.cvv.trim())) {
        return "CVV must contain 3 or 4 digits.";
      }
    }

    if (paymentMethod === "PAYPAL") {
      if (!paypal.email || !validateEmail(paypal.email)) {
        return "A valid PayPal email is required.";
      }
    }

    return "";
  };

  const handleConfirm = async () => {
    const error = validateForm();
    if (error) {
      setStatus({ type: "error", message: error });
      return;
    }

    const result = await onConfirm({
      customer,
      paymentMethod,
      card,
      paypal,
    });

    if (result.success) {
      setStatus({ type: "success", message: result.message || "Payment approved." });
    } else {
      setStatus({ type: "error", message: result.message || "Payment failed." });
    }
  };

  return (
    <Modal show={show} onHide={handleClose} centered size="lg">
      <Modal.Header closeButton>
        <Modal.Title>Checkout</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <div className="checkout-modal">
          <div className="checkout-details">
            <h5>Delivery Details</h5>
            <div className="form-grid">
              <input
                type="text"
                placeholder="Full name"
                value={customer.fullName}
                onChange={(e) => setCustomer({ ...customer, fullName: e.target.value })}
              />
              <input
                type="email"
                placeholder="Email"
                value={customer.email}
                readOnly
                title="Checkout must use the email of the logged-in account."
              />
              <input
                type="text"
                placeholder="Phone"
                value={customer.phone}
                onChange={(e) => setCustomer({ ...customer, phone: e.target.value })}
              />
              <input
                type="text"
                placeholder="Shipping address"
                value={customer.address}
                onChange={(e) => setCustomer({ ...customer, address: e.target.value })}
              />
            </div>

            <h5>Payment Method</h5>
            <div className="payment-methods">
              <label>
                <input
                  type="radio"
                  name="paymentMethod"
                  value="CARD"
                  checked={paymentMethod === "CARD"}
                  onChange={() => setPaymentMethod("CARD")}
                />
                Card
              </label>
              <label>
                <input
                  type="radio"
                  name="paymentMethod"
                  value="PAYPAL"
                  checked={paymentMethod === "PAYPAL"}
                  onChange={() => setPaymentMethod("PAYPAL")}
                />
                PayPal
              </label>
            </div>

            {paymentMethod === "CARD" && (
              <div className="form-grid">
                <input
                  type="text"
                  placeholder="Card holder"
                  value={card.cardHolder}
                  onChange={(e) => setCard({ ...card, cardHolder: e.target.value })}
                />
                <input
                  type="text"
                  placeholder="Card number"
                  value={card.cardNumber}
                  inputMode="numeric"
                  maxLength={23}
                  onChange={(e) => setCard({ ...card, cardNumber: e.target.value })}
                />
                <input
                  type="text"
                  placeholder="MM"
                  value={card.expiryMonth}
                  inputMode="numeric"
                  maxLength={2}
                  onChange={(e) => setCard({ ...card, expiryMonth: e.target.value })}
                />
                <input
                  type="text"
                  placeholder="YYYY"
                  value={card.expiryYear}
                  inputMode="numeric"
                  maxLength={4}
                  onChange={(e) => setCard({ ...card, expiryYear: e.target.value })}
                />
                <input
                  type="password"
                  placeholder="CVV"
                  value={card.cvv}
                  inputMode="numeric"
                  maxLength={4}
                  onChange={(e) => setCard({ ...card, cvv: e.target.value })}
                />
                <small>
                  Demo card: 4242 4242 4242 4242, any future expiry date, and a
                  3-digit CVV.
                </small>
              </div>
            )}

            {paymentMethod === "PAYPAL" && (
              <div className="form-grid">
                <input
                  type="email"
                  placeholder="PayPal email"
                  value={paypal.email}
                  onChange={(e) => setPaypal({ ...paypal, email: e.target.value })}
                />
              </div>
            )}
          </div>

          <div className="checkout-summary">
            <h5>Order Summary</h5>
            <div className="summary-list">
              {cartItems.map((item) => (
                <div key={item.productId} className="summary-item">
                  <span>{item.name}</span>
                  <span>{formatCurrency(item.lineTotal)}</span>
                </div>
              ))}
            </div>
            <div className="summary-total">
              <span>Total</span>
              <span>{formatCurrency(totalPrice)}</span>
            </div>
            {currency && <small>Currency: {currency}</small>}
          </div>
        </div>
        {status.message && (
          <div className={`status-banner ${status.type}`}>{status.message}</div>
        )}
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={handleClose} disabled={isSubmitting}>
          Close
        </Button>
        <Button variant="primary" onClick={handleConfirm} disabled={isSubmitting}>
          {isSubmitting ? "Processing..." : "Confirm Purchase"}
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default CheckoutPopup;
