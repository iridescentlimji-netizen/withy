import { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import DateTimePicker from '@react-native-community/datetimepicker';
import { useNavigation, useRoute } from '@react-navigation/native';
import { AcademySearchModal } from '../components/AcademySearchModal';
import { AppHeader } from '../components/AppHeader';
import { useAuth } from '../context/AuthContext';
import { useFamily } from '../context/FamilyContext';
import {
  SCHEDULE_TYPES,
  SUBJECT_CATEGORIES,
  SUBJECT_CATEGORY_LABELS,
} from '../constants/schedule';
import { createAcademy, getSchedule, updateSchedule } from '../services/api';
import { colors, spacing, typography } from '../theme';
import { toKstInstant } from '../utils/datetime';

function instantToDate(isoString) {
  const date = new Date(isoString);
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function instantToTime(isoString) {
  const date = new Date(isoString);
  return new Date(2026, 0, 1, date.getHours(), date.getMinutes());
}

export function ScheduleEditScreen() {
  const navigation = useNavigation();
  const route = useRoute();
  const { user } = useAuth();
  const { activeFamily } = useFamily();
  const scheduleId = route.params?.scheduleId;

  const [title, setTitle] = useState('');
  const [scheduleType, setScheduleType] = useState(SCHEDULE_TYPES.ACTIVITY);
  const [startDate, setStartDate] = useState(new Date());
  const [endDate, setEndDate] = useState(new Date());
  const [startTime, setStartTime] = useState(new Date(2026, 0, 1, 15, 0));
  const [endTime, setEndTime] = useState(new Date(2026, 0, 1, 16, 0));
  const [academy, setAcademy] = useState(null);
  const [subjectCategory, setSubjectCategory] = useState('OTHER');
  const [academyModalVisible, setAcademyModalVisible] = useState(false);
  const [saveNewAcademy, setSaveNewAcademy] = useState(false);
  const [newAcademyName, setNewAcademyName] = useState('');
  const [newAcademyPhone, setNewAcademyPhone] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!activeFamily?.id || !scheduleId) {
      return;
    }

    getSchedule(activeFamily.id, scheduleId)
      .then((schedule) => {
        setTitle(schedule.title ?? '');
        setScheduleType(schedule.scheduleType ?? SCHEDULE_TYPES.ACTIVITY);
        setStartDate(instantToDate(schedule.startAt));
        setEndDate(instantToDate(schedule.endAt));
        setStartTime(instantToTime(schedule.startAt));
        setEndTime(instantToTime(schedule.endAt));
        if (schedule.academyId) {
          setAcademy({
            id: schedule.academyId,
            name: schedule.academyName,
            defaultSubjectCategory: schedule.subjectCategory,
          });
        }
        if (schedule.subjectCategory) {
          setSubjectCategory(schedule.subjectCategory);
        }
      })
      .catch((loadError) => {
        setError(loadError.message ?? '일정을 불러오지 못했습니다.');
      })
      .finally(() => setLoading(false));
  }, [activeFamily?.id, scheduleId]);

  const handleSelectAcademy = (selected) => {
    setAcademy(selected);
    setTitle(selected.name);
    if (selected.defaultSubjectCategory) {
      setSubjectCategory(selected.defaultSubjectCategory);
    }
    setAcademyModalVisible(false);
  };

  const handleCreateNewAcademy = (prefillName) => {
    setAcademy(null);
    setNewAcademyName(prefillName ?? '');
    setSaveNewAcademy(true);
    setAcademyModalVisible(false);
  };

  const handleSubmit = async () => {
    if (!activeFamily?.id || !scheduleId) {
      return;
    }
    if (!title.trim()) {
      setError('제목을 입력해 주세요.');
      return;
    }

    setSubmitting(true);
    setError('');

    try {
      let academyId = academy?.id ?? null;

      if (saveNewAcademy && newAcademyName.trim()) {
        const created = await createAcademy(activeFamily.id, {
          name: newAcademyName.trim(),
          phone: newAcademyPhone.trim() || null,
          defaultSubjectCategory: subjectCategory,
          memo: null,
        });
        academyId = created.id;
      }

      await updateSchedule(activeFamily.id, scheduleId, {
        title: title.trim(),
        description: null,
        scheduleType,
        academyId: scheduleType === SCHEDULE_TYPES.ACTIVITY ? academyId : null,
        subjectCategory: scheduleType === SCHEDULE_TYPES.ACTIVITY ? subjectCategory : null,
        pickupGuardianId: scheduleType === SCHEDULE_TYPES.PICKUP ? user?.id ?? null : null,
        startAt: toKstInstant(startDate, startTime),
        endAt: toKstInstant(endDate, endTime),
      });

      Alert.alert('완료', '일정이 수정되었습니다.', [
        {
          text: '확인',
          onPress: () => navigation.goBack(),
        },
      ]);
    } catch (submitError) {
      setError(submitError.message ?? '일정 수정에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator color={colors.primary} />
      </View>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <AppHeader subtitle="일정 수정" />

      <Text style={styles.label}>유형</Text>
      <View style={styles.chipRow}>
        {[SCHEDULE_TYPES.ACTIVITY, SCHEDULE_TYPES.PICKUP].map((type) => (
          <Pressable
            key={type}
            style={[styles.chip, scheduleType === type && styles.chipActive]}
            onPress={() => setScheduleType(type)}
          >
            <Text style={[styles.chipText, scheduleType === type && styles.chipTextActive]}>
              {type === SCHEDULE_TYPES.ACTIVITY ? '학원' : '귀가'}
            </Text>
          </Pressable>
        ))}
      </View>

      {scheduleType === SCHEDULE_TYPES.ACTIVITY ? (
        <>
          <Pressable style={styles.secondaryButton} onPress={() => setAcademyModalVisible(true)}>
            <Text style={styles.secondaryButtonText}>
              {academy ? `학원: ${academy.name}` : '학원 찾기 / 선택'}
            </Text>
          </Pressable>
          {saveNewAcademy ? (
            <>
              <TextInput
                style={styles.input}
                value={newAcademyName}
                onChangeText={setNewAcademyName}
                placeholder="새 학원 이름"
              />
              <TextInput
                style={styles.input}
                value={newAcademyPhone}
                onChangeText={setNewAcademyPhone}
                placeholder="연락처 (선택)"
              />
            </>
          ) : null}
          <Text style={styles.label}>과목</Text>
          <View style={styles.chipRow}>
            {SUBJECT_CATEGORIES.map((category) => (
              <Pressable
                key={category}
                style={[styles.chip, subjectCategory === category && styles.chipActive]}
                onPress={() => setSubjectCategory(category)}
              >
                <Text
                  style={[styles.chipText, subjectCategory === category && styles.chipTextActive]}
                >
                  {SUBJECT_CATEGORY_LABELS[category]}
                </Text>
              </Pressable>
            ))}
          </View>
        </>
      ) : null}

      <Text style={styles.label}>제목</Text>
      <TextInput style={styles.input} value={title} onChangeText={setTitle} placeholder="일정 제목" />

      <Text style={styles.label}>시작</Text>
      <DateTimePicker value={startDate} mode="date" onChange={(_, value) => value && setStartDate(value)} />
      <DateTimePicker value={startTime} mode="time" onChange={(_, value) => value && setStartTime(value)} />

      <Text style={styles.label}>종료</Text>
      <DateTimePicker value={endDate} mode="date" onChange={(_, value) => value && setEndDate(value)} />
      <DateTimePicker value={endTime} mode="time" onChange={(_, value) => value && setEndTime(value)} />

      {error ? <Text style={styles.error}>{error}</Text> : null}

      <Pressable
        style={[styles.primaryButton, submitting && styles.buttonDisabled]}
        onPress={handleSubmit}
        disabled={submitting}
      >
        {submitting ? (
          <ActivityIndicator color="#FFFFFF" />
        ) : (
          <Text style={styles.primaryButtonText}>저장</Text>
        )}
      </Pressable>

      <AcademySearchModal
        visible={academyModalVisible}
        familyId={activeFamily?.id}
        onClose={() => setAcademyModalVisible(false)}
        onSelect={handleSelectAcademy}
        onCreateNew={handleCreateNewAcademy}
      />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    padding: spacing.lg,
    gap: spacing.sm,
    paddingBottom: spacing.xl,
  },
  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  label: {
    ...typography.body,
    fontWeight: '600',
    color: colors.text,
    marginTop: spacing.sm,
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
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.xs,
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
    fontWeight: '600',
  },
  secondaryButton: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 8,
    padding: spacing.md,
  },
  secondaryButtonText: {
    ...typography.body,
    color: colors.primary,
  },
  primaryButton: {
    marginTop: spacing.md,
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
  error: {
    ...typography.caption,
    color: '#DC2626',
  },
});
