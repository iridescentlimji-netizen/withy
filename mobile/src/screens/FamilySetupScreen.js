import { useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { AppHeader } from '../components/AppHeader';
import { useFamily } from '../context/FamilyContext';
import { submitJoinRequest } from '../services/api';
import { colors, spacing, typography } from '../theme';

export function FamilySetupScreen() {
  const { bootstrapFamily, refreshFamilies, error, setError } = useFamily();
  const [mode, setMode] = useState('create');
  const [name, setName] = useState('우리 가족');
  const [inviteCode, setInviteCode] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [joinSubmitted, setJoinSubmitted] = useState(false);

  const handleCreateFamily = async () => {
    const trimmed = name.trim();
    if (!trimmed) {
      setError('가족 이름을 입력해 주세요.');
      return;
    }

    setIsSubmitting(true);
    setError('');

    try {
      await bootstrapFamily(trimmed);
    } catch (submitError) {
      setError(submitError.message ?? '가족을 만들지 못했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleJoinFamily = async () => {
    const normalized = inviteCode.trim().toUpperCase();
    if (normalized.length !== 8) {
      setError('8자리 초대 코드를 입력해 주세요.');
      return;
    }

    setIsSubmitting(true);
    setError('');

    try {
      await submitJoinRequest(normalized);
      setJoinSubmitted(true);
    } catch (submitError) {
      setError(submitError.message ?? '가입 요청에 실패했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <View style={styles.container}>
      <AppHeader subtitle="처음 사용하시는군요. 가족을 만들거나 초대 코드로 참여해 주세요." />

      <View style={styles.modeRow}>
        <Pressable
          style={[styles.modeChip, mode === 'create' && styles.modeChipActive]}
          onPress={() => {
            setMode('create');
            setJoinSubmitted(false);
            setError('');
          }}
        >
          <Text style={[styles.modeChipText, mode === 'create' && styles.modeChipTextActive]}>
            새 가족 만들기
          </Text>
        </Pressable>
        <Pressable
          style={[styles.modeChip, mode === 'join' && styles.modeChipActive]}
          onPress={() => {
            setMode('join');
            setError('');
          }}
        >
          <Text style={[styles.modeChipText, mode === 'join' && styles.modeChipTextActive]}>
            초대 코드로 참여
          </Text>
        </Pressable>
      </View>

      <View style={styles.card}>
        {mode === 'create' ? (
          <>
            <Text style={styles.label}>가족 이름</Text>
            <TextInput
              style={styles.input}
              value={name}
              onChangeText={setName}
              placeholder="예: 우리 가족"
              editable={!isSubmitting}
            />
            {error ? <Text style={styles.error}>{error}</Text> : null}
            <Pressable
              style={[styles.button, isSubmitting && styles.buttonDisabled]}
              onPress={handleCreateFamily}
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <ActivityIndicator color="#FFFFFF" />
              ) : (
                <Text style={styles.buttonText}>시작하기</Text>
              )}
            </Pressable>
          </>
        ) : joinSubmitted ? (
          <>
            <Text style={styles.successTitle}>가입 요청을 보냈어요</Text>
            <Text style={styles.successMessage}>
              가족 관리자가 승인하면 일정을 볼 수 있습니다. 승인 후 아래 버튼으로 다시 확인해 주세요.
            </Text>
            {error ? <Text style={styles.error}>{error}</Text> : null}
            <Pressable
              style={[styles.button, isSubmitting && styles.buttonDisabled]}
              onPress={async () => {
                setIsSubmitting(true);
                setError('');
                try {
                  await refreshFamilies();
                } catch (refreshError) {
                  setError(refreshError.message ?? '가족 정보를 불러오지 못했습니다.');
                } finally {
                  setIsSubmitting(false);
                }
              }}
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <ActivityIndicator color="#FFFFFF" />
              ) : (
                <Text style={styles.buttonText}>승인 여부 확인</Text>
              )}
            </Pressable>
          </>
        ) : (
          <>
            <Text style={styles.label}>초대 코드</Text>
            <TextInput
              style={styles.input}
              value={inviteCode}
              onChangeText={setInviteCode}
              placeholder="8자리 코드"
              autoCapitalize="characters"
              maxLength={8}
              editable={!isSubmitting}
            />
            {error ? <Text style={styles.error}>{error}</Text> : null}
            <Pressable
              style={[styles.button, isSubmitting && styles.buttonDisabled]}
              onPress={handleJoinFamily}
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <ActivityIndicator color="#FFFFFF" />
              ) : (
                <Text style={styles.buttonText}>가입 요청 보내기</Text>
              )}
            </Pressable>
          </>
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
  modeRow: {
    flexDirection: 'row',
    gap: spacing.sm,
  },
  modeChip: {
    flex: 1,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 8,
    paddingVertical: spacing.sm,
    alignItems: 'center',
  },
  modeChipActive: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  modeChipText: {
    ...typography.bodySmall,
    color: colors.textSecondary,
    fontWeight: '600',
  },
  modeChipTextActive: {
    color: '#FFFFFF',
  },
  card: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 12,
    padding: spacing.md,
    gap: spacing.sm,
  },
  label: {
    ...typography.body,
    fontWeight: '600',
    color: colors.text,
  },
  input: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 8,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    ...typography.body,
    color: colors.text,
  },
  button: {
    marginTop: spacing.sm,
    backgroundColor: colors.primary,
    borderRadius: 8,
    paddingVertical: spacing.md,
    alignItems: 'center',
  },
  buttonDisabled: {
    opacity: 0.7,
  },
  buttonText: {
    ...typography.body,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  error: {
    ...typography.caption,
    color: '#DC2626',
  },
  successTitle: {
    ...typography.body,
    fontWeight: '700',
    color: colors.text,
  },
  successMessage: {
    ...typography.bodySmall,
    color: colors.textTertiary,
  },
});
