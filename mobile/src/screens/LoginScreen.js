import { useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { AppHeader } from '../components/AppHeader';
import { useAuth } from '../context/AuthContext';
import { colors, spacing, typography } from '../theme';

const LOGIN_OPTIONS = [
  { provider: 'kakao', label: '카카오로 시작하기', buttonStyle: styles.kakaoButton, textStyle: styles.kakaoButtonText },
  { provider: 'naver', label: '네이버로 시작하기', buttonStyle: styles.naverButton, textStyle: styles.naverButtonText },
  { provider: 'google', label: 'Google로 시작하기', buttonStyle: styles.googleButton, textStyle: styles.googleButtonText },
];

export function LoginScreen() {
  const { login, error, setError } = useAuth();
  const [activeProvider, setActiveProvider] = useState(null);

  async function handleLogin(provider) {
    setActiveProvider(provider);
    setError('');

    try {
      await login(provider);
    } catch (loginError) {
      setError(loginError.message ?? '로그인에 실패했습니다.');
    } finally {
      setActiveProvider(null);
    }
  }

  return (
    <View style={styles.container}>
      <AppHeader subtitle="SSO 계정으로 시작하세요" />

      <View style={styles.card}>
        <Text style={styles.title}>로그인</Text>
        <Text style={styles.description}>
          맞벌이 부부와 보호자가 함께 아이 스케줄을 관리합니다.
        </Text>

        {LOGIN_OPTIONS.map((option) => (
          <Pressable
            key={option.provider}
            style={[option.buttonStyle, activeProvider && styles.buttonDisabled]}
            onPress={() => handleLogin(option.provider)}
            disabled={Boolean(activeProvider)}
          >
            {activeProvider === option.provider ? (
              <ActivityIndicator color={option.provider === 'kakao' ? '#1A1A1A' : '#FFFFFF'} />
            ) : (
              <Text style={option.textStyle}>{option.label}</Text>
            )}
          </Pressable>
        ))}

        {error ? <Text style={styles.error}>{error}</Text> : null}
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
  },
  card: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 12,
    padding: spacing.lg,
    gap: spacing.md,
  },
  title: {
    ...typography.title,
    color: colors.text,
  },
  description: {
    ...typography.body,
    color: colors.textSecondary,
  },
  kakaoButton: {
    backgroundColor: '#FEE500',
    borderRadius: 10,
    paddingVertical: spacing.md,
    alignItems: 'center',
  },
  kakaoButtonText: {
    ...typography.body,
    fontWeight: '700',
    color: '#1A1A1A',
  },
  naverButton: {
    backgroundColor: '#03C75A',
    borderRadius: 10,
    paddingVertical: spacing.md,
    alignItems: 'center',
  },
  naverButtonText: {
    ...typography.body,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  googleButton: {
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 10,
    paddingVertical: spacing.md,
    alignItems: 'center',
  },
  googleButtonText: {
    ...typography.body,
    fontWeight: '700',
    color: colors.text,
  },
  buttonDisabled: {
    opacity: 0.7,
  },
  error: {
    ...typography.caption,
    color: '#DC2626',
  },
});
