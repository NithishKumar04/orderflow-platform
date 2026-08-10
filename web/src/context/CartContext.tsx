import {
  createContext,
  type ReactNode,
  useContext,
  useEffect,
  useMemo,
  useReducer,
  useState,
} from "react";
import type { Product } from "../types";

const CART_KEY = "orderflow.cart";

export type CartLine = {
  product: Product;
  quantity: number;
};

export type CartState = {
  lines: CartLine[];
};

export type CartAction =
  | { type: "add"; product: Product }
  | { type: "decrement"; productId: string }
  | { type: "remove"; productId: string }
  | { type: "clear" };

export function cartReducer(state: CartState, action: CartAction): CartState {
  if (action.type === "clear") {
    return { lines: [] };
  }

  if (action.type === "remove") {
    return {
      lines: state.lines.filter(
        (line) => line.product.id !== action.productId,
      ),
    };
  }

  if (action.type === "decrement") {
    return {
      lines: state.lines
        .map((line) =>
          line.product.id === action.productId
            ? { ...line, quantity: line.quantity - 1 }
            : line,
        )
        .filter((line) => line.quantity > 0),
    };
  }

  const existing = state.lines.find(
    (line) => line.product.id === action.product.id,
  );
  if (existing) {
    return {
      lines: state.lines.map((line) =>
        line.product.id === action.product.id
          ? {
              ...line,
              quantity: Math.min(
                line.quantity + 1,
                Math.min(action.product.inventory, 10),
              ),
            }
          : line,
      ),
    };
  }
  return {
    lines: [...state.lines, { product: action.product, quantity: 1 }],
  };
}

function initialState(): CartState {
  try {
    const value = localStorage.getItem(CART_KEY);
    return value ? (JSON.parse(value) as CartState) : { lines: [] };
  } catch {
    return { lines: [] };
  }
}

type CartContextValue = {
  lines: CartLine[];
  count: number;
  total: number;
  isOpen: boolean;
  setOpen: (value: boolean) => void;
  add: (product: Product) => void;
  decrement: (productId: string) => void;
  remove: (productId: string) => void;
  clear: () => void;
};

const CartContext = createContext<CartContextValue | null>(null);

export function CartProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(cartReducer, undefined, initialState);
  const [isOpen, setOpen] = useState(false);

  useEffect(() => {
    localStorage.setItem(CART_KEY, JSON.stringify(state));
  }, [state]);

  const value = useMemo<CartContextValue>(
    () => ({
      lines: state.lines,
      count: state.lines.reduce((sum, line) => sum + line.quantity, 0),
      total: state.lines.reduce(
        (sum, line) => sum + line.product.price * line.quantity,
        0,
      ),
      isOpen,
      setOpen,
      add: (product) => dispatch({ type: "add", product }),
      decrement: (productId) => dispatch({ type: "decrement", productId }),
      remove: (productId) => dispatch({ type: "remove", productId }),
      clear: () => dispatch({ type: "clear" }),
    }),
    [isOpen, state.lines],
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error("useCart must be used inside CartProvider");
  }
  return context;
}
