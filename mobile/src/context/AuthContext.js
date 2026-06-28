import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { loginWithOAuthProvider, logout as logoutRequest, fetchCurrentUser, updateMyNickname } from '../services/auth';
import { getAccessToken } from '../services/tokenStorage';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isRestoring, setIsRestoring] = useState(true);
  const [error, setError] = useState('');

  const restoreSession = useCallback(async () => {
    setIsRestoring(true);
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
      setIsRestoring(false);
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

  const updateNickname = useCallback(async (nickname) => {
    setError('');
    const updated = await updateMyNickname(nickname);
    setUser(updated);
    return updated;
  }, []);

  const value = useMemo(
    () => ({
      user,
      isRestoring,
      error,
      isAuthenticated: Boolean(user),
      login,
      logout,
      updateNickname,
      restoreSession,
      setError,
    }),
    [user, isRestoring, error, login, logout, updateNickname, restoreSession],
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
