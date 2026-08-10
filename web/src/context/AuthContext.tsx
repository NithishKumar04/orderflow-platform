import {
  createContext,
  type ReactNode,
  useContext,
  useMemo,
  useState,
} from "react";
import { api } from "../api/client";
import type { Session } from "../types";

const SESSION_KEY = "orderflow.session";

type AuthContextValue = {
  session: Session | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

function readSession(): Session | null {
  try {
    const value = localStorage.getItem(SESSION_KEY);
    return value ? (JSON.parse(value) as Session) : null;
  } catch {
    localStorage.removeItem(SESSION_KEY);
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(readSession);

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      login: async (email, password) => {
        const nextSession = await api.login(email, password);
        localStorage.setItem(SESSION_KEY, JSON.stringify(nextSession));
        setSession(nextSession);
      },
      logout: () => {
        localStorage.removeItem(SESSION_KEY);
        setSession(null);
      },
    }),
    [session],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return context;
}
