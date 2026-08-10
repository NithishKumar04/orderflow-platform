import { ArrowRight, KeyRound, LockKeyhole } from "lucide-react";
import { type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ApiError } from "../api/client";
import { Brand } from "../components/Brand";
import { useAuth } from "../context/AuthContext";

export function LoginPage() {
  const { session, login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("demo@orderflow.dev");
  const [password, setPassword] = useState("Demo123!");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (session) {
      navigate("/", { replace: true });
    }
  }, [navigate, session]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await login(email, password);
      navigate("/", { replace: true });
    } catch (reason) {
      setError(
        reason instanceof ApiError ? reason.message : "Sign in failed.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="signin">
      <section className="signin__visual" aria-label="OrderFlow collection">
        <Brand light />
        <div className="signin__visual-copy">
          <span className="eyebrow eyebrow--light">System status · Operational</span>
          <h1>Every order has a story.</h1>
          <p>Follow it from intent to inventory, payment, and fulfillment.</p>
        </div>
      </section>

      <section className="signin__form-wrap">
        <div className="signin__form">
          <span className="signin__mobile-brand">
            <Brand />
          </span>
          <div className="signin__heading">
            <span className="eyebrow">Demo workspace</span>
            <h2>Sign in to OrderFlow</h2>
            <p>Use the prepared customer account to explore the storefront.</p>
          </div>

          <div className="demo-credentials">
            <KeyRound size={18} />
            <div>
              <strong>Demo account</strong>
              <span>demo@orderflow.dev · Demo123!</span>
            </div>
          </div>

          <form onSubmit={submit}>
            <label>
              Email
              <input
                type="email"
                autoComplete="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                required
              />
            </label>
            <label>
              Password
              <input
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                required
              />
            </label>
            {error && <p className="form-error">{error}</p>}
            <button
              type="submit"
              className="primary-button primary-button--full"
              disabled={submitting}
            >
              <LockKeyhole size={18} />
              <span>{submitting ? "Signing in..." : "Sign in"}</span>
              <ArrowRight size={18} />
            </button>
          </form>
        </div>
      </section>
    </main>
  );
}
