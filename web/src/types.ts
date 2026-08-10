export type Product = {
  id: string;
  sku: string;
  name: string;
  description: string;
  category: string;
  price: number;
  imageUrl: string;
  inventory: number;
  featured: boolean;
};

export type PaymentMethod = "DEMO_APPROVED" | "DEMO_DECLINED";

export type OrderStatus =
  | "PENDING"
  | "INVENTORY_RESERVED"
  | "PAYMENT_CONFIRMED"
  | "CONFIRMED"
  | "REJECTED_OUT_OF_STOCK"
  | "PAYMENT_FAILED"
  | "PROCESSING_FAILED"
  | "CANCELLED";

export type Order = {
  id: string;
  orderNumber: string;
  status: OrderStatus;
  totalAmount: number;
  paymentMethod: PaymentMethod;
  createdAt: string;
  updatedAt: string;
  items: OrderItem[];
  timeline: TimelineEntry[];
};

export type OrderItem = {
  productId: string;
  sku: string;
  productName: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
};

export type TimelineEntry = {
  status: OrderStatus;
  title: string;
  description: string;
  occurredAt: string;
};

export type Session = {
  token: string;
  email: string;
  displayName: string;
};

export type ApiProblem = {
  status?: number;
  title?: string;
  detail?: string;
};
