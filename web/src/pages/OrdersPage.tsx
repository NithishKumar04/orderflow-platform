import {
  ArrowRight,
  Check,
  PackageOpen,
  RefreshCw,
  RotateCcw,
} from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { ApiError, api } from "../api/client";
import { StatusBadge } from "../components/StatusBadge";
import { useAuth } from "../context/AuthContext";
import type { Order } from "../types";
import { formatCurrency, formatDate, formatTime } from "../utils/format";

const ACTIVE_STATUSES = new Set([
  "PENDING",
  "INVENTORY_RESERVED",
  "PAYMENT_CONFIRMED",
]);

export function OrdersPage() {
  const { session } = useAuth();
  const location = useLocation();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");
  const [cancelling, setCancelling] = useState("");
  const [showPlaced, setShowPlaced] = useState(
    Boolean((location.state as { placed?: boolean } | null)?.placed),
  );

  const loadOrders = useCallback(
    async (quiet = false) => {
      if (!session) return;
      if (quiet) setRefreshing(true);
      try {
        setOrders(await api.orders(session.token));
        setError("");
      } catch (reason) {
        setError(
          reason instanceof ApiError
            ? reason.message
            : "Orders could not be loaded.",
        );
      } finally {
        setLoading(false);
        setRefreshing(false);
      }
    },
    [session],
  );

  useEffect(() => {
    void loadOrders();
  }, [loadOrders]);

  useEffect(() => {
    if (!orders.some((order) => ACTIVE_STATUSES.has(order.status))) {
      return;
    }
    const timer = window.setInterval(() => void loadOrders(true), 1_500);
    return () => window.clearInterval(timer);
  }, [loadOrders, orders]);

  useEffect(() => {
    if (!showPlaced) return;
    const timer = window.setTimeout(() => setShowPlaced(false), 4_000);
    return () => window.clearTimeout(timer);
  }, [showPlaced]);

  async function cancel(orderId: string) {
    if (!session) return;
    setCancelling(orderId);
    setError("");
    try {
      const updated = await api.cancelOrder(session.token, orderId);
      setOrders((current) =>
        current.map((order) => (order.id === orderId ? updated : order)),
      );
    } catch (reason) {
      setError(
        reason instanceof ApiError ? reason.message : "Cancellation failed.",
      );
    } finally {
      setCancelling("");
    }
  }

  return (
    <section className="orders-page">
      {showPlaced && (
        <div className="toast" role="status">
          <Check size={18} />
          Order accepted. Processing has started.
        </div>
      )}

      <div className="orders-heading">
        <div>
          <span className="eyebrow">Order history</span>
          <h1>Your orders</h1>
          <p>Track each state transition from checkout to confirmation.</p>
        </div>
        <button
          type="button"
          className="secondary-button"
          onClick={() => void loadOrders(true)}
          disabled={refreshing}
        >
          <RefreshCw size={17} className={refreshing ? "is-spinning" : ""} />
          Refresh
        </button>
      </div>

      {error && <div className="page-message page-message--error">{error}</div>}

      {loading ? (
        <div className="orders-loading">
          <span />
          <span />
        </div>
      ) : orders.length === 0 ? (
        <div className="orders-empty">
          <PackageOpen size={36} />
          <h2>No orders yet</h2>
          <p>Your completed checkouts will appear here.</p>
          <Link to="/" className="text-button">
            Browse catalog <ArrowRight size={16} />
          </Link>
        </div>
      ) : (
        <div className="orders-list">
          {orders.map((order) => (
            <article className="order-card" key={order.id}>
              <header className="order-card__header">
                <div>
                  <span>Order</span>
                  <h2>{order.orderNumber}</h2>
                  <small>{formatDate(order.createdAt)}</small>
                </div>
                <div className="order-card__summary">
                  <StatusBadge status={order.status} />
                  <strong>{formatCurrency(order.totalAmount)}</strong>
                </div>
              </header>

              <div className="order-card__content">
                <div className="order-items">
                  <h3>Items</h3>
                  {order.items.map((item) => (
                    <div className="order-item" key={item.productId}>
                      <div>
                        <strong>{item.productName}</strong>
                        <span>
                          {item.quantity} × {formatCurrency(item.unitPrice)}
                        </span>
                      </div>
                      <span>{formatCurrency(item.lineTotal)}</span>
                    </div>
                  ))}
                  <div className="order-payment">
                    Payment simulation
                    <strong>
                      {order.paymentMethod === "DEMO_APPROVED"
                        ? "Approved path"
                        : "Declined path"}
                    </strong>
                  </div>
                </div>

                <div className="order-timeline">
                  <h3>Event timeline</h3>
                  <ol>
                    {order.timeline.map((entry, index) => (
                      <li key={`${entry.status}-${entry.occurredAt}`}>
                        <span className="timeline-dot">
                          {index === order.timeline.length - 1 && <span />}
                        </span>
                        <div>
                          <strong>{entry.title}</strong>
                          <p>{entry.description}</p>
                          <time dateTime={entry.occurredAt}>
                            {formatTime(entry.occurredAt)}
                          </time>
                        </div>
                      </li>
                    ))}
                  </ol>
                </div>
              </div>

              {(order.status === "PENDING" ||
                order.status === "CONFIRMED") && (
                <footer className="order-card__footer">
                  <button
                    type="button"
                    className="danger-text-button"
                    onClick={() => void cancel(order.id)}
                    disabled={cancelling === order.id}
                  >
                    <RotateCcw size={16} />
                    {cancelling === order.id
                      ? "Cancelling..."
                      : "Cancel order"}
                  </button>
                </footer>
              )}
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
