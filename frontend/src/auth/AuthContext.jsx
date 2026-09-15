import { createContext, useContext, useMemo, useState } from 'react';
import { TOKEN_KEY, USER_KEY } from '../api/client';

const AuthContext = createContext(null);

function readUser() {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readUser);

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: !!user,
      // Called after a successful login/register (auth API response).
      signIn: (auth) => {
        localStorage.setItem(TOKEN_KEY, auth.token);
        const u = { email: auth.email, displayName: auth.displayName };
        localStorage.setItem(USER_KEY, JSON.stringify(u));
        setUser(u);
      },
      signOut: () => {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        setUser(null);
      },
    }),
    [user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
