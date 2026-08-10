import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "./components/AppShell";
import { useAuth } from "./context/AuthContext";
import { CatalogPage } from "./pages/CatalogPage";
import { LoginPage } from "./pages/LoginPage";
import { OrdersPage } from "./pages/OrdersPage";

function ProtectedShell() {
  const { session } = useAuth();
  return session ? <AppShell /> : <Navigate to="/signin" replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/signin" element={<LoginPage />} />
      <Route element={<ProtectedShell />}>
        <Route index element={<CatalogPage />} />
        <Route path="/orders" element={<OrdersPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
