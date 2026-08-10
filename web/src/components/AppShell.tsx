import {
  LogOut,
  PackageCheck,
  ShoppingBag,
  Store,
  UserRound,
} from "lucide-react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { Brand } from "./Brand";
import { CartDrawer } from "./CartDrawer";

export function AppShell() {
  const { session, logout } = useAuth();
  const { count, setOpen } = useCart();
  const navigate = useNavigate();

  function signOut() {
    logout();
    navigate("/signin");
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="topbar__inner">
          <NavLink to="/" className="brand-link" aria-label="OrderFlow home">
            <Brand />
          </NavLink>

          <nav className="main-nav" aria-label="Main navigation">
            <NavLink to="/" end>
              <Store size={17} />
              <span>Catalog</span>
            </NavLink>
            <NavLink to="/orders">
              <PackageCheck size={17} />
              <span>Orders</span>
            </NavLink>
          </nav>

          <div className="topbar__actions">
            <div className="account">
              <span className="account__avatar">
                <UserRound size={16} />
              </span>
              <span className="account__text">
                <strong>{session?.displayName}</strong>
                <small>{session?.email}</small>
              </span>
            </div>
            <button
              type="button"
              className="icon-button"
              onClick={signOut}
              aria-label="Sign out"
              title="Sign out"
            >
              <LogOut size={18} />
            </button>
            <button
              type="button"
              className="cart-button"
              onClick={() => setOpen(true)}
              aria-label={`Open cart with ${count} items`}
            >
              <ShoppingBag size={18} />
              <span>Cart</span>
              <strong>{count}</strong>
            </button>
          </div>
        </div>
      </header>

      <main>
        <Outlet />
      </main>
      <CartDrawer />
    </div>
  );
}
