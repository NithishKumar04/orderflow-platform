import { describe, expect, it } from "vitest";
import type { Product } from "../types";
import { cartReducer, type CartState } from "./CartContext";

const product: Product = {
  id: "product-1",
  sku: "TEST-1",
  name: "Test Product",
  description: "Test",
  category: "Test",
  price: 25,
  imageUrl: "/test.jpg",
  inventory: 2,
  featured: false,
};

describe("cartReducer", () => {
  it("adds products and respects available inventory", () => {
    let state: CartState = { lines: [] };
    state = cartReducer(state, { type: "add", product });
    state = cartReducer(state, { type: "add", product });
    state = cartReducer(state, { type: "add", product });

    expect(state.lines).toHaveLength(1);
    expect(state.lines[0].quantity).toBe(2);
  });

  it("removes a line when its quantity reaches zero", () => {
    const state = cartReducer(
      { lines: [{ product, quantity: 1 }] },
      { type: "decrement", productId: product.id },
    );

    expect(state.lines).toEqual([]);
  });
});
