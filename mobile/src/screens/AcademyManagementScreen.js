import { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { AppHeader } from '../components/AppHeader';
import { SUBJECT_CATEGORIES, SUBJECT_CATEGORY_LABELS } from '../constants/schedule';
import { useFamily } from '../context/FamilyContext';
import { createAcademy, deleteAcademy, listAcademies, updateAcademy } from '../services/api';
import { colors, spacing, typography } from '../theme';

export function AcademyManagementScreen() {
  const { activeFamily } = useFamily();
  const [academies, setAcademies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [category, setCategory] = useState('OTHER');
  const [editingId, setEditingId] = useState(null);

  const loadAcademies = useCallback(async () => {
    if (!activeFamily?.id) {
      return;
    }

    setLoading(true);
    setError('');

    try {
      setAcademies(await listAcademies(activeFamily.id));
    } catch (loadError) {
      setError(loadError.message ?? '학원 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [activeFamily?.id]);

  useEffect(() => {
    loadAcademies();
  }, [loadAcademies]);

  const resetForm = () => {
    setName('');
    setPhone('');
    setCategory('OTHER');
    setEditingId(null);
  };

  const handleSubmit = async () => {
    if (!activeFamily?.id || !name.trim()) {
      setError('학원 이름을 입력해 주세요.');
      return;
    }

    try {
      if (editingId) {
        await updateAcademy(activeFamily.id, editingId, {
          name: name.trim(),
          phone: phone.trim() || null,
          defaultSubjectCategory: category,
          memo: null,
        });
      } else {
        await createAcademy(activeFamily.id, {
          name: name.trim(),
          phone: phone.trim() || null,
          defaultSubjectCategory: category,
          memo: null,
        });
      }
      resetForm();
      await loadAcademies();
    } catch (submitError) {
      setError(submitError.message ?? '저장에 실패했습니다.');
    }
  };

  const handleEdit = (academy) => {
    setEditingId(academy.id);
    setName(academy.name);
    setPhone(academy.phone ?? '');
    setCategory(academy.defaultSubjectCategory ?? 'OTHER');
  };

  const handleDelete = (academy) => {
    Alert.alert('삭제', `${academy.name} 학원을 삭제할까요?`, [
      { text: '취소', style: 'cancel' },
      {
        text: '삭제',
        style: 'destructive',
        onPress: async () => {
          await deleteAcademy(activeFamily.id, academy.id);
          await loadAcademies();
        },
      },
    ]);
  };

  return (
    <View style={styles.container}>
      <AppHeader subtitle="학원 관리" />

      <View style={styles.form}>
        <TextInput style={styles.input} value={name} onChangeText={setName} placeholder="학원 이름" />
        <TextInput style={styles.input} value={phone} onChangeText={setPhone} placeholder="연락처" />
        <View style={styles.chipRow}>
          {SUBJECT_CATEGORIES.map((item) => (
            <Pressable
              key={item}
              style={[styles.chip, category === item && styles.chipActive]}
              onPress={() => setCategory(item)}
            >
              <Text style={[styles.chipText, category === item && styles.chipTextActive]}>
                {SUBJECT_CATEGORY_LABELS[item]}
              </Text>
            </Pressable>
          ))}
        </View>
        <Pressable style={styles.primaryButton} onPress={handleSubmit}>
          <Text style={styles.primaryButtonText}>{editingId ? '수정' : '추가'}</Text>
        </Pressable>
        {editingId ? (
          <Pressable onPress={resetForm}>
            <Text style={styles.cancelText}>취소</Text>
          </Pressable>
        ) : null}
      </View>

      {loading ? <ActivityIndicator color={colors.primary} /> : null}
      {error ? <Text style={styles.error}>{error}</Text> : null}

      <FlatList
        data={academies}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <View style={styles.item}>
            <View style={styles.itemBody}>
              <Text style={styles.itemTitle}>{item.name}</Text>
              {item.phone ? <Text style={styles.itemMeta}>{item.phone}</Text> : null}
            </View>
            <View style={styles.itemActions}>
              <Pressable onPress={() => handleEdit(item)}>
                <Text style={styles.actionText}>수정</Text>
              </Pressable>
              <Pressable onPress={() => handleDelete(item)}>
                <Text style={[styles.actionText, styles.deleteText]}>삭제</Text>
              </Pressable>
            </View>
          </View>
        )}
        ListEmptyComponent={
          !loading ? <Text style={styles.empty}>등록된 학원이 없습니다.</Text> : null
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
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.sm,
  },
  chip: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 999,
    paddingHorizontal: spacing.sm,
    paddingVertical: 4,
  },
  chipActive: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  chipText: {
    ...typography.caption,
    color: colors.textSecondary,
  },
  chipTextActive: {
    color: '#FFFFFF',
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
  cancelText: {
    ...typography.body,
    color: colors.textSecondary,
    textAlign: 'center',
  },
  list: {
    gap: spacing.sm,
    paddingBottom: spacing.xl,
  },
  item: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 8,
    padding: spacing.md,
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: spacing.sm,
  },
  itemBody: {
    flex: 1,
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
  itemActions: {
    gap: spacing.sm,
    alignItems: 'flex-end',
  },
  actionText: {
    ...typography.caption,
    color: colors.primary,
  },
  deleteText: {
    color: '#DC2626',
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
    marginBottom: spacing.sm,
  },
});
