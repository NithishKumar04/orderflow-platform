import {
  CheckCircle2,
  ChevronRight,
  Minus,
  Plus,
  ShieldCheck,
  Trash2,
  X,
  XCircle,
} from "lucide-react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ApiError, api } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import type { PaymentMethod } from "../types";
import { formatCurrency } from "../utils/format";

export function CartDrawer() {
  const { session } = useAuth();
  const cart = useCart();
  const navigate = useNavigate();
  const [paymentMethod, setPaymentMethod] =
    useState<PaymentMethod>("DEMO_APPROVED");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  if (!cart.isOpen) {
    return null;
  }

  async function placeOrder() {
    if (!session || cart.lines.length === 0) {
      return;
    }
    setSubmitting(true);
    setError("");
    try {
      await api.checkout(
        session.token,
        cart.lines.map((line) => ({
          productId: line.product.id,
          quantity: line.quantity,
        })),
        paymentMethod,
        crypto.randomUUID(),
      );
      cart.clear();
      cart.setOpen(false);
      navigate("/orders", { state: { placed: true } });
    } catch (reason) {
      setError(
        reason instanceof ApiError
          ? reason.message
          : "Checkout could not be completed.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="drawer-layer" role="presentation">
      <button
        type="button"
        className="drawer-scrim"
        onClick={() => cart.setOpen(false)}
        aria-label="Close cart"
      />
      <aside className="cart-drawer" aria-label="Shopping cart">
        <div className="drawer-header">
          <div>
            <span className="eyebrow">Your selection</span>
            <h2>Cart ({cart.count})</h2>
          </div>
          <button
            type="button"
            className="icon-button"
            onClick={() => cart.setOpen(false)}
            aria-label="Close cart"
            title="Close cart"
          >
            <X size={20} />
          </button>
        </div>

        <div className="cart-drawer__content">
          {cart.lines.length === 0 ? (
            <div className="cart-empty">
              <CheckCircle2 size={30} />
              <h3>Your cart is clear</h3>
              <p>Add an item from the catalog to start an order.</p>
              <button
                type="button"
                className="text-button"
                onClick={() => cart.setOpen(false)}
              >
                Continue browsing <ChevronRight size={16} />
              </button>
            </div>
          ) : (
            <>
              <div className="cart-lines">
                {cart.lines.map((line) => (
                  <div className="cart-line" key={line.product.id}>
                    <img src={line.product.imageUrl} alt="" />
                    <div className="cart-line__body">
                      <div>
                        <strong>{line.product.name}</strong>
                        <span>{formatCurrency(line.product.price)}</span>
                      </div>
                      <div className="quantity-control" aria-label="Quantity">
                        <button
                          type="button"
                          onClick={() => cart.decrement(line.product.id)}
                          aria-label={`Decrease ${line.product.name} quantity`}
                        >
                          <Minus size={15} />
                        </button>
                        <span>{line.quantity}</span>
                        <button
                          type="button"
                          onClick={() => cart.add(line.product)}
                          disabled={
                            line.quantity >= Math.min(line.product.inventory, 10)
                          }
                          aria-label={`Increase ${line.product.name} quantity`}
                        >
                          <Plus size={15} />
                        </button>
                      </div>
                    </div>
                    <button
                      type="button"
                      className="remove-button"
                      onClick={() => cart.remove(line.product.id)}
                      aria-label={`Remove ${line.product.name}`}
                      title="Remove"
                    >
                      <Trash2 size={17} />
                    </button>
                  </div>
                ))}
              </div>

              <fieldset className="payment-options">
                <legend>Payment simulation</legend>
                <label
                  className={
                    paymentMethod === "DEMO_APPROVED" ? "is-selected" : ""
                  }
                >
                  <input
                    type="radio"
                    name="payment"
                    value="DEMO_APPROVED"
                    checked={paymentMethod === "DEMO_APPROVED"}
                    onChange={() => setPaymentMethod("DEMO_APPROVED")}
                  />
                  <CheckCircle2 size={19} />
                  <span>
                    <strong>Approve payment</strong>
                    <small>Completes the order workflow</small>
                  </span>
                </label>
                <label
                  className={
                    paymentMethod === "DEMO_DECLINED" ? "is-selected" : ""
                  }
                >
                  <input
                    type="radio"
                    name="payment"
                    value="DEMO_DECLINED"
                    checked={paymentMethod === "DEMO_DECLINED"}
                    onChange={() => setPaymentMethod("DEMO_DECLINED")}
                  />
                  <XCircle size={19} />
                  <span>
                    <strong>Decline payment</strong>
                    <small>Exercises inventory compensation</small>
                  </span>
                </label>
              </fieldset>
            </>
          )}
        </div>

        {cart.lines.length > 0 && (
          <div className="drawer-footer">
            <div className="order-total">
              <span>Total</span>
              <strong>{formatCurrency(cart.total)}</strong>
            </div>
            {error && <p className="form-error">{error}</p>}
            <button
              type="button"
              className="primary-button primary-button--full"
              onClick={placeOrder}
              disabled={submitting}
            >
              <ShieldCheck size={18} />
              <span>{submitting ? "Placing order..." : "Place order"}</span>
            </button>
            <small className="checkout-note">
              Demo checkout. No payment details are collected.
            </small>
          </div>
        )}
      </aside>
    </div>
  );
}
