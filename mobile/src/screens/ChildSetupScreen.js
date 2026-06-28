import { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { AppHeader } from '../components/AppHeader';
import { useFamily } from '../context/FamilyContext';
import { createChild, listChildren } from '../services/api';
import { colors, spacing, typography } from '../theme';

export function ChildSetupScreen() {
  const { activeFamily } = useFamily();
  const [children, setChildren] = useState([]);
  const [nickname, setNickname] = useState('');
  const [birthYear, setBirthYear] = useState(String(new Date().getFullYear() - 8));
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const loadChildren = useCallback(async () => {
    if (!activeFamily?.id) {
      return;
    }

    setLoading(true);
    setError('');

    try {
      setChildren(await listChildren(activeFamily.id));
    } catch (loadError) {
      setError(loadError.message ?? '아이 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [activeFamily?.id]);

  useEffect(() => {
    loadChildren();
  }, [loadChildren]);

  const handleSubmit = async () => {
    if (!activeFamily?.id || !nickname.trim()) {
      setError('아이 이름을 입력해 주세요.');
      return;
    }

    const year = Number(birthYear);
    if (!year || year < 2000 || year > 2100) {
      setError('출생 연도를 확인해 주세요.');
      return;
    }

    setSubmitting(true);
    setError('');

    try {
      await createChild(activeFamily.id, { nickname: nickname.trim(), birthYear: year });
      setNickname('');
      await loadChildren();
    } catch (submitError) {
      setError(submitError.message ?? '아이 등록에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <View style={styles.container}>
      <AppHeader subtitle="아이 등록" />

      <View style={styles.form}>
        <TextInput style={styles.input} value={nickname} onChangeText={setNickname} placeholder="아이 이름" />
        <TextInput
          style={styles.input}
          value={birthYear}
          onChangeText={setBirthYear}
          placeholder="출생 연도"
          keyboardType="number-pad"
        />
        {error ? <Text style={styles.error}>{error}</Text> : null}
        <Pressable
          style={[styles.primaryButton, submitting && styles.buttonDisabled]}
          onPress={handleSubmit}
          disabled={submitting}
        >
          {submitting ? (
            <ActivityIndicator color="#FFFFFF" />
          ) : (
            <Text style={styles.primaryButtonText}>등록</Text>
          )}
        </Pressable>
      </View>

      {loading ? <ActivityIndicator color={colors.primary} /> : null}

      <FlatList
        data={children}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <View style={styles.item}>
            <Text style={styles.itemTitle}>{item.nickname}</Text>
            <Text style={styles.itemMeta}>{item.birthYear}년생</Text>
          </View>
        )}
        ListEmptyComponent={
          !loading ? <Text style={styles.empty}>등록된 아이가 없습니다.</Text> : null
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
    padding: spacing.lg,
  },
  form: {
    gap: spacing.sm,
    marginBottom: spacing.md,
  },
  input: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 8,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  primaryButton: {
    backgroundColor: colors.primary,
    borderRadius: 8,
    paddingVertical: spacing.md,
    alignItems: 'center',
  },
  primaryButtonText: {
    ...typography.body,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  buttonDisabled: {
    opacity: 0.7,
  },
  list: {
    gap: spacing.sm,
  },
  item: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 8,
    padding: spacing.md,
    gap: 2,
  },
  itemTitle: {
    ...typography.body,
    fontWeight: '600',
    color: colors.text,
  },
  itemMeta: {
    ...typography.caption,
    color: colors.textSecondary,
  },
  empty: {
    ...typography.body,
    color: colors.textSecondary,
    textAlign: 'center',
    marginTop: spacing.lg,
  },
  error: {
    ...typography.caption,
    color: '#DC2626',
  },
});
