import { useCallback, useEffect, useState } from 'react';
import {
  ActionSheetIOS,
  ActivityIndicator,
  Alert,
  Platform,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useFocusEffect, useNavigation, useRoute } from '@react-navigation/native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { ScheduleChildFilter } from '../components/schedule/ScheduleChildFilter';
import { ScheduleCompletedSection } from '../components/schedule/ScheduleCompletedSection';
import { ScheduleEmptyState } from '../components/schedule/ScheduleEmptyState';
import { ScheduleListCard } from '../components/schedule/ScheduleListCard';
import { ScheduleScreenHeader } from '../components/schedule/ScheduleScreenHeader';
import { ScheduleWeekCalendar, shiftSelectedWeek } from '../components/schedule/ScheduleWeekCalendar';
import { ScheduleWhiteCard } from '../components/schedule/ScheduleWhiteCard';
import { useFamily } from '../context/FamilyContext';
import { cancelSchedule, getScheduleCalendar, listChildren, listSchedulesForDay } from '../services/api';
import { colors, spacing, typography } from '../theme';
import { getWeekDateParams, toDateParam, toMonthParam } from '../utils/datetime';

function getMonthsInWeek(dateParam) {
  const months = new Set(getWeekDateParams(dateParam).map((day) => toMonthParam(day)));
  return [...months];
}

async function loadCalendarCounts(familyId, dateParam, childId) {
  const months = getMonthsInWeek(dateParam);
  const counts = {};

  for (const month of months) {
    const response = await getScheduleCalendar(familyId, month, childId);
    Object.assign(counts, response.countsByDate ?? {});
  }

  return counts;
}

function findFirstDateWithCount(dateParam, countsByDate) {
  return getWeekDateParams(dateParam).find((day) => (countsByDate[day] ?? 0) > 0) ?? null;
}

function dayHasSchedules(schedules) {
  if (!schedules) {
    return false;
  }
  return (
    (schedules.inProgress?.length ?? 0) > 0 ||
    (schedules.upcoming?.length ?? 0) > 0 ||
    (schedules.completed?.length ?? 0) > 0
  );
}

export function ScheduleScreen() {
  const navigation = useNavigation();
  const route = useRoute();
  const insets = useSafeAreaInsets();
  const { activeFamily } = useFamily();
  const [children, setChildren] = useState([]);
  const [selectedChildId, setSelectedChildId] = useState(null);
  const [selectedDate, setSelectedDate] = useState(toDateParam());
  const [calendarCounts, setCalendarCounts] = useState({});
  const [data, setData] = useState(null);
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState('');
  const [refreshing, setRefreshing] = useState(false);

  const loadChildren = useCallback(async () => {
    if (!activeFamily?.id) {
      return;
    }

    try {
      setChildren(await listChildren(activeFamily.id));
    } catch {
      setChildren([]);
    }
  }, [activeFamily?.id]);

  const loadSchedules = useCallback(
    async (dateOverride, options = {}) => {
      if (!activeFamily?.id) {
        return;
      }

      const date = dateOverride ?? selectedDate;
      const { autoSelectWeekDate = false } = options;

      setError('');
      setStatus('loading');

      try {
        const [counts, schedules] = await Promise.all([
          loadCalendarCounts(activeFamily.id, date, selectedChildId ?? undefined),
          listSchedulesForDay(activeFamily.id, date, selectedChildId ?? undefined),
        ]);
        setCalendarCounts(counts);

        if (autoSelectWeekDate && !dayHasSchedules(schedules)) {
          const firstScheduledDate = findFirstDateWithCount(date, counts);
          if (firstScheduledDate && firstScheduledDate !== date) {
            setSelectedDate(firstScheduledDate);
            return;
          }
        }

        setData(schedules);
        setStatus('ready');
      } catch (loadError) {
        setCalendarCounts({});
        setData(null);
        setStatus('error');
        setError(loadError.message ?? '일정을 불러오지 못했습니다.');
      }
    },
    [activeFamily?.id, selectedChildId, selectedDate],
  );

  useFocusEffect(
    useCallback(() => {
      loadChildren();

      const dateFromParams = route.params?.selectedDate;
      if (dateFromParams) {
        setSelectedDate(dateFromParams);
        navigation.setParams({ selectedDate: undefined });
      }

      loadSchedules(dateFromParams, { autoSelectWeekDate: !dateFromParams });
    }, [loadChildren, loadSchedules, navigation, route.params?.selectedDate]),
  );

  useEffect(() => {
    if (!activeFamily?.id) {
      return;
    }

    loadSchedules();
  }, [activeFamily?.id, selectedDate, selectedChildId, loadSchedules]);

  const handleChangeWeek = useCallback((direction) => {
    setSelectedDate((current) => shiftSelectedWeek(current, direction));
  }, []);

  const handleRefresh = useCallback(async () => {
    setRefreshing(true);
    await Promise.all([loadChildren(), loadSchedules()]);
    setRefreshing(false);
  }, [loadChildren, loadSchedules]);

  const handleRegister = useCallback(() => {
    navigation.navigate('ScheduleCreate', {
      date: selectedDate,
      childId: selectedChildId ?? undefined,
    });
  }, [navigation, selectedChildId, selectedDate]);

  const handleDeleteSchedule = useCallback(
    async (schedule, scope) => {
      if (!activeFamily?.id) {
        return;
      }

      try {
        await cancelSchedule(activeFamily.id, schedule.id, scope);
        await loadSchedules();
      } catch (deleteError) {
        Alert.alert('삭제 실패', deleteError.message ?? '일정을 삭제하지 못했습니다.');
      }
    },
    [activeFamily?.id, loadSchedules],
  );

  const confirmDeleteSchedule = useCallback(
    (schedule) => {
      const isRecurring = Boolean(schedule.seriesId);

      if (isRecurring) {
        Alert.alert('일정 삭제', '어떤 일정을 삭제할까요?', [
          { text: '취소', style: 'cancel' },
          {
            text: '이 일정만',
            onPress: () => handleDeleteSchedule(schedule, 'OCCURRENCE'),
          },
          {
            text: '이후 반복 일정 모두',
            style: 'destructive',
            onPress: () => handleDeleteSchedule(schedule, 'FUTURE'),
          },
        ]);
        return;
      }

      Alert.alert('일정 삭제', '이 일정을 삭제할까요?', [
        { text: '취소', style: 'cancel' },
        {
          text: '삭제',
          style: 'destructive',
          onPress: () => handleDeleteSchedule(schedule, 'OCCURRENCE'),
        },
      ]);
    },
    [handleDeleteSchedule],
  );

  const handlePressScheduleMenu = useCallback(
    (schedule) => {
      const openEdit = () => {
        navigation.navigate('ScheduleEdit', { scheduleId: schedule.id });
      };
      const openDelete = () => confirmDeleteSchedule(schedule);

      if (Platform.OS === 'ios') {
        ActionSheetIOS.showActionSheetWithOptions(
          {
            options: ['취소', '수정', '삭제'],
            cancelButtonIndex: 0,
            destructiveButtonIndex: 2,
          },
          (buttonIndex) => {
            if (buttonIndex === 1) {
              openEdit();
            } else if (buttonIndex === 2) {
              openDelete();
            }
          },
        );
        return;
      }

      Alert.alert('일정', undefined, [
        { text: '취소', style: 'cancel' },
        { text: '수정', onPress: openEdit },
        { text: '삭제', style: 'destructive', onPress: openDelete },
      ]);
    },
    [confirmDeleteSchedule, navigation],
  );

  const canEdit = activeFamily?.canEdit !== false;

  const activeSchedules = data
    ? [...(data.inProgress ?? []), ...(data.upcoming ?? [])].sort(
        (a, b) => new Date(a.startAt).getTime() - new Date(b.startAt).getTime(),
      )
    : [];
  const completedSchedules = data?.completed ?? [];
  const hasAnySchedules = activeSchedules.length > 0 || completedSchedules.length > 0;

  return (
    <View style={styles.container}>
      <ScrollView
        contentContainerStyle={[
          styles.content,
          { paddingTop: insets.top + spacing.md, paddingBottom: insets.bottom + spacing.xl },
        ]}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={handleRefresh} />}
      >
        <ScheduleScreenHeader onPressSearch={() => {}} onPressAdd={handleRegister} />

        <ScheduleChildFilter
          familyChildren={children}
          selectedChildId={selectedChildId}
          onSelect={setSelectedChildId}
        />

        <View style={styles.calendarBleed}>
          <ScheduleWeekCalendar
            selectedDate={selectedDate}
            countsByDate={calendarCounts}
            onSelectDate={setSelectedDate}
            onChangeWeek={handleChangeWeek}
          />
        </View>

        {status === 'loading' ? <ActivityIndicator color={colors.primary} /> : null}
        {status === 'error' ? <Text style={styles.error}>{error}</Text> : null}

        {status === 'ready' && data ? (
          hasAnySchedules ? (
            <View style={styles.listSection}>
              <View style={styles.listHeaderRow}>
                <Text style={styles.listTitle}>일정목록</Text>
                <Text style={styles.listCaption}>시작시간 기준으로 정렬됩니다.</Text>
              </View>

              <View style={styles.listItems}>
                <ScheduleCompletedSection completed={completedSchedules} />
                {activeSchedules.map((schedule) => (
                  <ScheduleListCard
                    key={schedule.id}
                    schedule={schedule}
                    onPressMenu={canEdit ? () => handlePressScheduleMenu(schedule) : undefined}
                  />
                ))}
              </View>
            </View>
          ) : (
            <>
              <View style={styles.listHeaderRow}>
                <Text style={styles.listTitle}>일정목록</Text>
                <Text style={styles.listCaption}>시작시간 기준으로 정렬됩니다.</Text>
              </View>
              <ScheduleWhiteCard>
                <ScheduleEmptyState onPressRegister={handleRegister} />
              </ScheduleWhiteCard>
            </>
          )
        ) : null}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    paddingHorizontal: spacing.md,
    gap: spacing.md,
  },
  calendarBleed: {
    marginHorizontal: -spacing.md,
  },
  listSection: {
    gap: spacing.md,
  },
  listHeaderRow: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'space-between',
    gap: spacing.sm,
  },
  listTitle: {
    ...typography.sectionTitle,
    color: colors.text,
    flexShrink: 0,
  },
  listCaption: {
    ...typography.caption,
    color: colors.textTertiary,
    flexShrink: 1,
    textAlign: 'right',
  },
  listItems: {
    gap: spacing.sm,
  },
  error: {
    ...typography.body,
    color: '#DC2626',
  },
});
