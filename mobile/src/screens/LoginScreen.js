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

export function LoginScreen() {
  const { login, error, setError } = useAuth();
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleKakaoLogin() {
    setIsSubmitting(true);
    setError('');

    try {
      await login();
    } catch (loginError) {
      setError(loginError.message ?? '카카오 로그인에 실패했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <View style={styles.container}>
      <AppHeader subtitle="카카오 계정으로 시작하세요" />

      <View style={styles.card}>
        <Text style={styles.title}>로그인</Text>
        <Text style={styles.description}>
          맞벌이 부부와 보호자가 함께 아이 스케줄을 관리합니다.
        </Text>

        <Pressable
          style={[styles.kakaoButton, isSubmitting && styles.buttonDisabled]}
          onPress={handleKakaoLogin}
          disabled={isSubmitting}
        >
          {isSubmitting ? (
            <ActivityIndicator color="#1A1A1A" />
          ) : (
            <Text style={styles.kakaoButtonText}>카카오로 시작하기</Text>
          )}
        </Pressable>

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
  buttonDisabled: {
    opacity: 0.7,
  },
  error: {
    ...typography.caption,
    color: '#DC2626',
  },
});
