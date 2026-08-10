import { Check, ShoppingBag } from "lucide-react";
import { useState } from "react";
import { useCart } from "../context/CartContext";
import type { Product } from "../types";
import { formatCurrency } from "../utils/format";

export function ProductCard({ product }: { product: Product }) {
  const { add } = useCart();
  const [added, setAdded] = useState(false);
  const soldOut = product.inventory === 0;

  function addToCart() {
    add(product);
    setAdded(true);
    window.setTimeout(() => setAdded(false), 900);
  }

  return (
    <article className="product-card">
      <div className="product-card__image-wrap">
        <img
          className="product-card__image"
          src={product.imageUrl}
          alt={product.name}
          loading="lazy"
        />
        {product.featured && <span className="product-card__flag">Editor pick</span>}
      </div>
      <div className="product-card__body">
        <div className="product-card__meta">
          <span>{product.category}</span>
          <span>{soldOut ? "Sold out" : `${product.inventory} available`}</span>
        </div>
        <h2>{product.name}</h2>
        <p>{product.description}</p>
        <div className="product-card__footer">
          <strong>{formatCurrency(product.price)}</strong>
          <button
            type="button"
            className={`add-button ${added ? "add-button--added" : ""}`}
            onClick={addToCart}
            disabled={soldOut}
          >
            {added ? <Check size={17} /> : <ShoppingBag size={16} />}
            <span>{added ? "Added" : "Add"}</span>
          </button>
        </div>
      </div>
    </article>
  );
}
