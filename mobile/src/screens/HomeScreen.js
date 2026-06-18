import { useEffect, useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native';
import { AppHeader } from '../components/AppHeader';
import { API_BASE_URL } from '../config/env';
import { useAuth } from '../context/AuthContext';
import { checkApiHealth } from '../services/api';
import { colors, spacing, typography } from '../theme';

export function HomeScreen() {
  const { user, logout } = useAuth();
  const [apiStatus, setApiStatus] = useState('checking');
  const [apiMessage, setApiMessage] = useState('');

  useEffect(() => {
    checkApiHealth()
      .then((data) => {
        setApiStatus('connected');
        setApiMessage(data.status ?? 'UP');
      })
      .catch((error) => {
        setApiStatus('offline');
        setApiMessage(
          `백엔드 서버에 연결할 수 없습니다.\n(${API_BASE_URL})\n${error.message ?? ''}`.trim(),
        );
      });
  }, []);

  return (
    <View style={styles.container}>
      <AppHeader subtitle="맞벌이 부부를 위한 아이 스케줄 관리" />

      <View style={styles.card}>
        <Text style={styles.cardTitle}>로그인 사용자</Text>
        <Text style={styles.userName}>{user?.nickname ?? '사용자'}</Text>
        <Text style={styles.userMeta}>{user?.accountType ?? 'ADULT'}</Text>
        <Pressable style={styles.logoutButton} onPress={logout}>
          <Text style={styles.logoutButtonText}>로그아웃</Text>
        </Pressable>
      </View>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>서버 연결 상태</Text>
        {apiStatus === 'checking' ? (
          <ActivityIndicator color={colors.primary} />
        ) : (
          <Text
            style={[
              styles.status,
              apiStatus === 'connected' ? styles.statusOk : styles.statusError,
            ]}
          >
            {apiStatus === 'connected' ? `API ${apiMessage}` : apiMessage}
          </Text>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.xl,
    gap: spacing.md,
  },
  card: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 12,
    padding: spacing.md,
    gap: spacing.sm,
  },
  cardTitle: {
    ...typography.body,
    fontWeight: '600',
    color: colors.text,
  },
  userName: {
    ...typography.title,
    color: colors.text,
  },
  userMeta: {
    ...typography.caption,
    color: colors.textSecondary,
  },
  logoutButton: {
    alignSelf: 'flex-start',
    marginTop: spacing.sm,
    paddingVertical: spacing.sm,
    paddingHorizontal: spacing.md,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.border,
  },
  logoutButtonText: {
    ...typography.body,
    color: colors.textSecondary,
  },
  status: {
    ...typography.body,
  },
  statusOk: {
    color: '#059669',
  },
  statusError: {
    color: '#DC2626',
  },
});
