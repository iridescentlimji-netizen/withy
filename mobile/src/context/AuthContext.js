import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { loginWithOAuthProvider, logout as logoutRequest, fetchCurrentUser } from '../services/auth';
import { getAccessToken } from '../services/tokenStorage';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const restoreSession = useCallback(async () => {
    setIsLoading(true);
    setError('');

    try {
      const token = await getAccessToken();
      if (!token) {
        setUser(null);
        return;
      }

      const currentUser = await fetchCurrentUser(token);
      setUser(currentUser);
    } catch (sessionError) {
      setUser(null);
      setError(sessionError.message ?? '세션 복원에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    restoreSession();
  }, [restoreSession]);

  const login = useCallback(async (provider) => {
    setError('');
    const authResponse = await loginWithOAuthProvider(provider);
    setUser(authResponse.user);
    return authResponse;
  }, []);

  const logout = useCallback(async () => {
    await logoutRequest();
    setUser(null);
    setError('');
  }, []);

  const value = useMemo(
    () => ({
      user,
      isLoading,
      error,
      isAuthenticated: Boolean(user),
      login,
      logout,
      restoreSession,
      setError,
    }),
    [user, isLoading, error, login, logout, restoreSession],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
