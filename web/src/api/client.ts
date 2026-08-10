import type {
  ApiProblem,
  Order,
  PaymentMethod,
  Product,
  Session,
} from "../types";

const API_BASE = import.meta.env.VITE_API_URL ?? "";

export class ApiError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

async function request<T>(
  path: string,
  options: RequestInit = {},
  token?: string,
): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body) {
    headers.set("Content-Type", "application/json");
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  if (!response.ok) {
    let problem: ApiProblem = {};
    try {
      problem = (await response.json()) as ApiProblem;
    } catch {
      problem = {};
    }
    throw new ApiError(
      response.status,
      problem.detail ?? problem.title ?? "The request could not be completed.",
    );
  }

  return (await response.json()) as T;
}

export const api = {
  login(email: string, password: string) {
    return request<Session>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
  },

  products() {
    return request<Product[]>("/api/products");
  },

  orders(token: string) {
    return request<Order[]>("/api/orders", {}, token);
  },

  checkout(
    token: string,
    items: Array<{ productId: string; quantity: number }>,
    paymentMethod: PaymentMethod,
    idempotencyKey: string,
  ) {
    return request<Order>(
      "/api/orders",
      {
        method: "POST",
        headers: { "Idempotency-Key": idempotencyKey },
        body: JSON.stringify({ items, paymentMethod }),
      },
      token,
    );
  },

  cancelOrder(token: string, orderId: string) {
    return request<Order>(
      `/api/orders/${orderId}`,
      { method: "DELETE" },
      token,
    );
  },
};
