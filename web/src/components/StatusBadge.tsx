import {
  Ban,
  CheckCircle2,
  CircleDashed,
  Clock3,
  CreditCard,
  PackageCheck,
  TriangleAlert,
  XCircle,
} from "lucide-react";
import type { OrderStatus } from "../types";

const labels: Record<OrderStatus, string> = {
  PENDING: "Processing",
  INVENTORY_RESERVED: "Inventory reserved",
  PAYMENT_CONFIRMED: "Payment confirmed",
  CONFIRMED: "Confirmed",
  REJECTED_OUT_OF_STOCK: "Out of stock",
  PAYMENT_FAILED: "Payment failed",
  PROCESSING_FAILED: "Needs attention",
  CANCELLED: "Cancelled",
};

const icons = {
  PENDING: Clock3,
  INVENTORY_RESERVED: PackageCheck,
  PAYMENT_CONFIRMED: CreditCard,
  CONFIRMED: CheckCircle2,
  REJECTED_OUT_OF_STOCK: TriangleAlert,
  PAYMENT_FAILED: XCircle,
  PROCESSING_FAILED: CircleDashed,
  CANCELLED: Ban,
};

export function StatusBadge({ status }: { status: OrderStatus }) {
  const Icon = icons[status];
  return (
    <span className={`status status--${status.toLowerCase()}`}>
      <Icon size={15} />
      {labels[status]}
    </span>
  );
}
