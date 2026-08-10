import http from "k6/http";
import { check, sleep } from "k6";

const baseUrl = __ENV.BASE_URL || "http://localhost:8080";

export const options = {
  scenarios: {
    checkout: {
      executor: "constant-vus",
      vus: Number(__ENV.VUS || 10),
      duration: __ENV.DURATION || "30s",
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<500"],
  },
};

export function setup() {
  const login = http.post(
    `${baseUrl}/api/auth/login`,
    JSON.stringify({
      email: "demo@orderflow.dev",
      password: "Demo123!",
    }),
    { headers: { "Content-Type": "application/json" } },
  );
  check(login, { "login succeeded": (response) => response.status === 200 });

  const catalog = http.get(`${baseUrl}/api/products`);
  check(catalog, { "catalog loaded": (response) => response.status === 200 });

  return {
    token: login.json("token"),
    productId: catalog.json("0.id"),
  };
}

export default function (data) {
  const response = http.post(
    `${baseUrl}/api/orders`,
    JSON.stringify({
      items: [{ productId: data.productId, quantity: 1 }],
      paymentMethod: "DEMO_APPROVED",
    }),
    {
      headers: {
        Authorization: `Bearer ${data.token}`,
        "Content-Type": "application/json",
        "Idempotency-Key": `k6-${__VU}-${__ITER}-${Date.now()}`,
      },
    },
  );

  check(response, { "order accepted": (result) => result.status === 201 });
  sleep(0.2);
}
