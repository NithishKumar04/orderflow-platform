import { ArrowDown, Search, SlidersHorizontal } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { ApiError, api } from "../api/client";
import { ProductCard } from "../components/ProductCard";
import type { Product } from "../types";

export function CatalogPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("All");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .products()
      .then(setProducts)
      .catch((reason) =>
        setError(
          reason instanceof ApiError
            ? reason.message
            : "The catalog is unavailable.",
        ),
      )
      .finally(() => setLoading(false));
  }, []);

  const categories = useMemo(
    () => ["All", ...new Set(products.map((product) => product.category))],
    [products],
  );

  const filteredProducts = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    return products.filter(
      (product) =>
        (category === "All" || product.category === category) &&
        (!normalized ||
          product.name.toLowerCase().includes(normalized) ||
          product.description.toLowerCase().includes(normalized)),
    );
  }, [category, products, query]);

  function scrollToCollection() {
    document
      .getElementById("collection")
      ?.scrollIntoView({ behavior: "smooth", block: "start" });
  }

  return (
    <>
      <section className="catalog-hero">
        <div className="catalog-hero__overlay" />
        <div className="catalog-hero__content">
          <span className="eyebrow eyebrow--light">The workspace edit</span>
          <h1>Tools with purpose.</h1>
          <p>Six considered essentials for the desk and the day.</p>
          <button type="button" onClick={scrollToCollection}>
            Shop the collection <ArrowDown size={17} />
          </button>
        </div>
      </section>

      <section className="collection" id="collection">
        <div className="collection__heading">
          <div>
            <span className="eyebrow">Curated catalog</span>
            <h2>Made for focused work</h2>
          </div>
          <p>{filteredProducts.length} products</p>
        </div>

        <div className="catalog-tools">
          <label className="search-control">
            <Search size={18} />
            <span className="sr-only">Search products</span>
            <input
              type="search"
              placeholder="Search the collection"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
          </label>
          <div className="category-filter" aria-label="Filter by category">
            <SlidersHorizontal size={17} />
            <div>
              {categories.map((item) => (
                <button
                  type="button"
                  className={item === category ? "is-active" : ""}
                  onClick={() => setCategory(item)}
                  key={item}
                >
                  {item}
                </button>
              ))}
            </div>
          </div>
        </div>

        {error && <div className="page-message page-message--error">{error}</div>}
        {loading ? (
          <div className="product-grid" aria-label="Loading catalog">
            {Array.from({ length: 6 }, (_, index) => (
              <div className="product-skeleton" key={index} />
            ))}
          </div>
        ) : (
          <div className="product-grid">
            {filteredProducts.map((product) => (
              <ProductCard product={product} key={product.id} />
            ))}
          </div>
        )}

        {!loading && !error && filteredProducts.length === 0 && (
          <div className="page-message">
            No products match the current filters.
          </div>
        )}
      </section>
    </>
  );
}
