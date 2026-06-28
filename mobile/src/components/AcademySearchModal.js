import { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  Modal,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { SUBJECT_CATEGORY_LABELS } from '../constants/schedule';
import { listAcademies } from '../services/api';
import { colors, spacing, typography } from '../theme';

export function AcademySearchModal({ visible, familyId, onClose, onSelect, onCreateNew }) {
  const [query, setQuery] = useState('');
  const [academies, setAcademies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!visible || !familyId) {
      return;
    }

    let cancelled = false;
    setLoading(true);
    setError('');

    listAcademies(familyId, query.trim() || undefined)
      .then((data) => {
        if (!cancelled) {
          setAcademies(data);
        }
      })
      .catch((loadError) => {
        if (!cancelled) {
          setError(loadError.message ?? '학원 목록을 불러오지 못했습니다.');
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [visible, familyId, query]);

  return (
    <Modal visible={visible} animationType="slide" onRequestClose={onClose}>
      <View style={styles.container}>
        <View style={styles.header}>
          <Text style={styles.title}>학원 찾기</Text>
          <Pressable onPress={onClose}>
            <Text style={styles.close}>닫기</Text>
          </Pressable>
        </View>

        <TextInput
          style={styles.input}
          value={query}
          onChangeText={setQuery}
          placeholder="학원 이름 검색"
        />

        <Pressable style={styles.manualButton} onPress={() => onCreateNew(null)}>
          <Text style={styles.manualButtonText}>학원 없이 직접 입력</Text>
        </Pressable>

        {loading ? <ActivityIndicator color={colors.primary} /> : null}
        {error ? <Text style={styles.error}>{error}</Text> : null}

        <FlatList
          data={academies}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.list}
          renderItem={({ item }) => (
            <Pressable style={styles.item} onPress={() => onSelect(item)}>
              <Text style={styles.itemTitle}>{item.name}</Text>
              {item.phone ? <Text style={styles.itemMeta}>{item.phone}</Text> : null}
              {item.defaultSubjectCategory ? (
                <Text style={styles.itemMeta}>
                  {SUBJECT_CATEGORY_LABELS[item.defaultSubjectCategory] ??
                    item.defaultSubjectCategory}
                </Text>
              ) : null}
            </Pressable>
          )}
          ListEmptyComponent={
            !loading ? <Text style={styles.empty}>검색 결과가 없습니다.</Text> : null
          }
        />

        <Pressable style={styles.createButton} onPress={() => onCreateNew(query.trim())}>
          <Text style={styles.createButtonText}>새 학원 등록</Text>
        </Pressable>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
    padding: spacing.lg,
    paddingTop: spacing.xl,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.md,
  },
  title: {
    ...typography.title,
    color: colors.text,
  },
  close: {
    ...typography.body,
    color: colors.primary,
  },
  input: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 8,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    marginBottom: spacing.sm,
  },
  manualButton: {
    marginBottom: spacing.md,
  },
  manualButtonText: {
    ...typography.body,
    color: colors.textSecondary,
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
  createButton: {
    backgroundColor: colors.primary,
    borderRadius: 8,
    paddingVertical: spacing.md,
    alignItems: 'center',
  },
  createButtonText: {
    ...typography.body,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  error: {
    ...typography.caption,
    color: '#DC2626',
    marginBottom: spacing.sm,
  },
});
