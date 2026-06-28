import { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Pressable,
  ScrollView,
  StyleSheet,
  Switch,
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
  DAY_OF_WEEK_OPTIONS,
  RECURRENCE_LABELS,
  RECURRENCE_TYPES,
  SCHEDULE_TYPES,
  SUBJECT_CATEGORIES,
  SUBJECT_CATEGORY_LABELS,
} from '../constants/schedule';
import { createAcademy, createSchedule, listChildren } from '../services/api';
import { colors, spacing, typography } from '../theme';
import { parseDateParam, toDateParam, toKstInstant, toLocalTimeParam } from '../utils/datetime';

export function ScheduleCreateScreen() {
  const navigation = useNavigation();
  const route = useRoute();
  const { user } = useAuth();
  const { activeFamily } = useFamily();

  const initialDate = route.params?.date ?? toDateParam();

  const [children, setChildren] = useState([]);
  const [childId, setChildId] = useState(null);
  const [title, setTitle] = useState('');
  const [scheduleType, setScheduleType] = useState(SCHEDULE_TYPES.ACTIVITY);
  const [recurrence, setRecurrence] = useState(RECURRENCE_TYPES.NONE);
  const [selectedDays, setSelectedDays] = useState(['MONDAY']);
  const [dayOfMonth, setDayOfMonth] = useState('1');
  const [startDate, setStartDate] = useState(parseDateParam(initialDate));
  const [endDate, setEndDate] = useState(parseDateParam(initialDate));
  const [startTime, setStartTime] = useState(new Date(2026, 0, 1, 15, 0));
  const [endTime, setEndTime] = useState(new Date(2026, 0, 1, 16, 0));
  const [effectiveUntil, setEffectiveUntil] = useState(null);
  const [hasEffectiveUntil, setHasEffectiveUntil] = useState(false);
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
    if (!activeFamily?.id) {
      return;
    }

    listChildren(activeFamily.id)
      .then((data) => {
        setChildren(data);
        const preferredChildId = route.params?.childId;
        const matchedChild = data.find((child) => child.id === preferredChildId);
        if (matchedChild) {
          setChildId(matchedChild.id);
        } else if (data[0]) {
          setChildId(data[0].id);
        }
      })
      .catch((loadError) => {
        setError(loadError.message ?? '아이 목록을 불러오지 못했습니다.');
      })
      .finally(() => setLoading(false));
  }, [activeFamily?.id, route.params?.childId]);

  const toggleDay = (day) => {
    setSelectedDays((current) =>
      current.includes(day) ? current.filter((value) => value !== day) : [...current, day],
    );
  };

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
    if (!activeFamily?.id || !childId) {
      setError('아이를 먼저 등록해 주세요.');
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

      const body =
        recurrence === RECURRENCE_TYPES.NONE
          ? {
              childId,
              title: title.trim(),
              description: null,
              scheduleType,
              academyId: scheduleType === SCHEDULE_TYPES.ACTIVITY ? academyId : null,
              subjectCategory: scheduleType === SCHEDULE_TYPES.ACTIVITY ? subjectCategory : null,
              pickupGuardianId:
                scheduleType === SCHEDULE_TYPES.PICKUP ? user?.id ?? null : null,
              recurrence,
              startAt: toKstInstant(startDate, startTime),
              endAt: toKstInstant(endDate, endTime),
            }
          : {
              childId,
              title: title.trim(),
              description: null,
              scheduleType,
              academyId: scheduleType === SCHEDULE_TYPES.ACTIVITY ? academyId : null,
              subjectCategory: scheduleType === SCHEDULE_TYPES.ACTIVITY ? subjectCategory : null,
              pickupGuardianId:
                scheduleType === SCHEDULE_TYPES.PICKUP ? user?.id ?? null : null,
              recurrence,
              startTime: toLocalTimeParam(startTime),
              endTime: toLocalTimeParam(endTime),
              effectiveFrom: toDateParam(startDate),
              effectiveUntil: hasEffectiveUntil && effectiveUntil ? toDateParam(effectiveUntil) : null,
              anchorDate: toDateParam(startDate),
              daysOfWeek:
                recurrence === RECURRENCE_TYPES.MONTHLY ? null : selectedDays,
              dayOfMonth:
                recurrence === RECURRENCE_TYPES.MONTHLY ? Number(dayOfMonth) : null,
            };

      await createSchedule(activeFamily.id, body);

      const scheduledDate =
        recurrence === RECURRENCE_TYPES.NONE ? toDateParam(startDate) : toDateParam(startDate);

      Alert.alert('완료', '일정이 등록되었습니다.', [
        {
          text: '확인',
          onPress: () => {
            navigation.navigate('MainTabs', {
              screen: 'Schedule',
              params: { selectedDate: scheduledDate },
            });
          },
        },
      ]);
    } catch (submitError) {
      setError(submitError.message ?? '일정 등록에 실패했습니다.');
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
      <AppHeader subtitle="일정 등록" />

      {children.length === 0 ? (
        <Text style={styles.warning}>아이를 먼저 등록해야 일정을 만들 수 있습니다.</Text>
      ) : null}

      <Text style={styles.label}>아이</Text>
      <View style={styles.chipRow}>
        {children.map((child) => (
          <Pressable
            key={child.id}
            style={[styles.chip, childId === child.id && styles.chipActive]}
            onPress={() => setChildId(child.id)}
          >
            <Text style={[styles.chipText, childId === child.id && styles.chipTextActive]}>
              {child.nickname}
            </Text>
          </Pressable>
        ))}
      </View>

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

      <Text style={styles.label}>반복</Text>
      <View style={styles.chipRow}>
        {Object.values(RECURRENCE_TYPES).map((type) => (
          <Pressable
            key={type}
            style={[styles.chip, recurrence === type && styles.chipActive]}
            onPress={() => setRecurrence(type)}
          >
            <Text style={[styles.chipText, recurrence === type && styles.chipTextActive]}>
              {RECURRENCE_LABELS[type]}
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

      {recurrence === RECURRENCE_TYPES.NONE ? (
        <>
          <Text style={styles.label}>시작</Text>
          <DateTimePicker value={startDate} mode="date" onChange={(_, value) => value && setStartDate(value)} />
          <DateTimePicker value={startTime} mode="time" onChange={(_, value) => value && setStartTime(value)} />
          <Text style={styles.label}>종료</Text>
          <DateTimePicker value={endDate} mode="date" onChange={(_, value) => value && setEndDate(value)} />
          <DateTimePicker value={endTime} mode="time" onChange={(_, value) => value && setEndTime(value)} />
        </>
      ) : (
        <>
          {recurrence !== RECURRENCE_TYPES.MONTHLY ? (
            <>
              <Text style={styles.label}>요일</Text>
              <View style={styles.chipRow}>
                {DAY_OF_WEEK_OPTIONS.map((day) => (
                  <Pressable
                    key={day.value}
                    style={[styles.chip, selectedDays.includes(day.value) && styles.chipActive]}
                    onPress={() => toggleDay(day.value)}
                  >
                    <Text
                      style={[
                        styles.chipText,
                        selectedDays.includes(day.value) && styles.chipTextActive,
                      ]}
                    >
                      {day.label}
                    </Text>
                  </Pressable>
                ))}
              </View>
            </>
          ) : (
            <>
              <Text style={styles.label}>매월 일자</Text>
              <TextInput
                style={styles.input}
                value={dayOfMonth}
                onChangeText={setDayOfMonth}
                keyboardType="number-pad"
              />
            </>
          )}
          <Text style={styles.label}>시작일</Text>
          <DateTimePicker
            value={startDate}
            mode="date"
            onChange={(_, value) => value && setStartDate(value)}
          />
          <Text style={styles.label}>시간</Text>
          <DateTimePicker value={startTime} mode="time" onChange={(_, value) => value && setStartTime(value)} />
          <DateTimePicker value={endTime} mode="time" onChange={(_, value) => value && setEndTime(value)} />
          <View style={styles.switchRow}>
            <Text style={styles.label}>종료일 설정</Text>
            <Switch value={hasEffectiveUntil} onValueChange={setHasEffectiveUntil} />
          </View>
          {hasEffectiveUntil ? (
            <DateTimePicker
              value={effectiveUntil ?? startDate}
              mode="date"
              onChange={(_, value) => value && setEffectiveUntil(value)}
            />
          ) : null}
        </>
      )}

      {error ? <Text style={styles.error}>{error}</Text> : null}

      <Pressable
        style={[styles.primaryButton, submitting && styles.buttonDisabled]}
        onPress={handleSubmit}
        disabled={submitting || children.length === 0}
      >
        {submitting ? (
          <ActivityIndicator color="#FFFFFF" />
        ) : (
          <Text style={styles.primaryButtonText}>등록</Text>
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
  switchRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
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
  warning: {
    ...typography.body,
    color: '#B45309',
  },
});
