import API from "../axios";
import { useState, useEffect, createContext, useCallback } from "react";

const CART_KEY_STORAGE = "digitech.cartKey";
const AUTH_STORAGE_KEY = "digitech.auth";

const createCartKey = () => {
  if (typeof crypto !== "undefined" && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return `cart_${Date.now()}_${Math.random().toString(16).slice(2)}`;
};

const ensureCartKey = () => {
  const existing = localStorage.getItem(CART_KEY_STORAGE);
  if (existing) {
    return existing;
  }
  const newKey = createCartKey();
  localStorage.setItem(CART_KEY_STORAGE, newKey);
  return newKey;
};

const defaultCart = {
  cartKey: "",
  items: [],
  subtotal: 0,
  total: 0,
  itemCount: 0,
  currency: "MAD",
};

const AppContext = createContext({
  data: [],
  isError: "",
  cart: defaultCart,
  isCartLoading: false,
  addToCart: async (product, quantity) => {},
  updateCartItem: async (productId, quantity) => {},
  removeFromCart: async (productId) => {},
  clearCart: async () => {},
  refreshData: async () => {},
  refreshCart: async () => {},
  checkout: async (payload) => {},
  cartKey: "",
});

export const AppProvider = ({ children }) => {
  const [data, setData] = useState([]);
  const [isError, setIsError] = useState("");
  const [cartKey] = useState(() => ensureCartKey());
  const [cart, setCart] = useState({ ...defaultCart, cartKey });
  const [isCartLoading, setIsCartLoading] = useState(false);

  const refreshData = useCallback(async () => {
    try {
      const response = await API.get("/products");
      setData(response.data);
    } catch (error) {
      setIsError(error.message);
    }
  }, []);

  const refreshCart = useCallback(async () => {
    if (!localStorage.getItem(AUTH_STORAGE_KEY)) {
      setCart({ ...defaultCart, cartKey });
      return;
    }
    setIsCartLoading(true);
    try {
      const response = await API.get(`/cart/${cartKey}`);
      setCart(response.data);
    } catch (error) {
      setIsError(error.message);
      setCart({ ...defaultCart, cartKey });
    } finally {
      setIsCartLoading(false);
    }
  }, [cartKey]);

  const addToCart = useCallback(
    async (product, quantity = 1) => {
      if (!product || !product.id) {
        return;
      }
      try {
        await API.post(`/cart/${cartKey}/items`, {
          productId: product.id,
          quantity,
        });
        await refreshCart();
      } catch (error) {
        setIsError(error?.response?.data || error.message);
      }
    },
    [cartKey, refreshCart]
  );

  const updateCartItem = useCallback(
    async (productId, quantity) => {
      try {
        await API.put(`/cart/${cartKey}/items/${productId}`, { quantity });
        await refreshCart();
      } catch (error) {
        setIsError(error?.response?.data || error.message);
      }
    },
    [cartKey, refreshCart]
  );

  const removeFromCart = useCallback(
    async (productId) => {
      try {
        await API.delete(`/cart/${cartKey}/items/${productId}`);
        await refreshCart();
      } catch (error) {
        setIsError(error?.response?.data || error.message);
      }
    },
    [cartKey, refreshCart]
  );

  const clearCart = useCallback(async () => {
    try {
      await API.delete(`/cart/${cartKey}`);
      await refreshCart();
    } catch (error) {
      setIsError(error?.response?.data || error.message);
    }
  }, [cartKey, refreshCart]);

  const checkout = useCallback(
    async ({ customer, paymentMethod, card, paypal }) => {
      try {
        const response = await API.post("/checkout", {
          cartKey,
          customer,
          paymentMethod,
          card,
          paypal,
        });
        await refreshCart();
        return response.data;
      } catch (error) {
        const message = error?.response?.data?.message || error?.response?.data || error.message;
        return {
          success: false,
          message,
          orderId: null,
          orderNumber: null,
          orderStatus: "FAILED",
          paymentStatus: "DECLINED",
          currency: "MAD",
        };
      }
    },
    [cartKey, refreshCart]
  );

  useEffect(() => {
    refreshData();
    refreshCart();
  }, [refreshData, refreshCart]);

  return (
    <AppContext.Provider
      value={{
        data,
        isError,
        cart,
        isCartLoading,
        addToCart,
        updateCartItem,
        removeFromCart,
        clearCart,
        refreshData,
        refreshCart,
        checkout,
        cartKey,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export default AppContext;