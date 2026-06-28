import { useCallback, useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { listSchedulesForDay } from '../../services/api';
import { colors, radius, spacing, typography } from '../../theme';
import { formatTimeRange, toDateParam } from '../../utils/datetime';
import { EmptyText } from './EmptyText';

const STATUS_LABELS = {
  IN_PROGRESS: { text: '수업중', tone: 'active' },
  COMPLETED: { text: '완료', tone: 'muted' },
  UPCOMING: { text: '다음일정', tone: 'muted' },
};

function StatusBadge({ label = '수업중' }) {
  return (
    <View style={styles.statusRow}>
      <View style={styles.statusDotOuter}>
        <View style={styles.statusDotInner} />
      </View>
      <Text style={styles.statusText}>{label}</Text>
    </View>
  );
}

function TodayScheduleRow({ schedule }) {
  const status = STATUS_LABELS[schedule.status] ?? STATUS_LABELS.UPCOMING;

  return (
    <View style={styles.todayRow}>
      <View style={styles.todayRowMain}>
        <Text style={styles.todayRowTitle}>{schedule.academyName ?? schedule.title}</Text>
        <Text style={styles.todayRowTime}>{formatTimeRange(schedule.startAt, schedule.endAt)}</Text>
      </View>
      <View style={[styles.todayStatusPill, status.tone === 'active' && styles.todayStatusPillActive]}>
        <Text
          style={[styles.todayStatusText, status.tone === 'active' && styles.todayStatusTextActive]}
        >
          {status.text}
        </Text>
      </View>
    </View>
  );
}

export function ChildStatusCard({ child, familyId }) {
  const navigation = useNavigation();
  const [expanded, setExpanded] = useState(false);
  const [todaySchedules, setTodaySchedules] = useState([]);
  const [loadingToday, setLoadingToday] = useState(false);
  const [todayError, setTodayError] = useState('');

  const current = child.current;
  const next = child.next;
  const inClass = Boolean(current);
  const hasTodaySchedules = child.todayScheduleCount > 0;

  const loadTodaySchedules = useCallback(async () => {
    if (!familyId) {
      return;
    }

    setLoadingToday(true);
    setTodayError('');

    try {
      const data = await listSchedulesForDay(familyId, toDateParam(), child.childId);
      const merged = [
        ...(data.inProgress ?? []),
        ...(data.upcoming ?? []),
        ...(data.completed ?? []),
      ].sort((a, b) => new Date(a.startAt).getTime() - new Date(b.startAt).getTime());
      setTodaySchedules(merged);
    } catch (error) {
      setTodaySchedules([]);
      setTodayError(error.message ?? '오늘 일정을 불러오지 못했습니다.');
    } finally {
      setLoadingToday(false);
    }
  }, [child.childId, familyId]);

  const handleToggleToday = useCallback(async () => {
    const nextExpanded = !expanded;
    setExpanded(nextExpanded);
    if (nextExpanded && todaySchedules.length === 0 && !loadingToday) {
      await loadTodaySchedules();
    }
  }, [expanded, loadTodaySchedules, loadingToday, todaySchedules.length]);

  const handleRegisterSchedule = useCallback(() => {
    navigation.navigate('ScheduleCreate', {
      date: toDateParam(),
      childId: child.childId,
    });
  }, [child.childId, navigation]);

  return (
    <View style={styles.card}>
      <View style={styles.topRow}>
        <View style={styles.identity}>
          <View style={styles.avatar}>
            <Text style={styles.avatarText}>{child.nickname.slice(0, 1)}</Text>
          </View>
          <View style={styles.identityText}>
            <Text style={styles.childName}>{child.nickname}</Text>
            {hasTodaySchedules ? (
              inClass ? <StatusBadge /> : null
            ) : (
              <EmptyText style={styles.emptyStatus}>오늘은 일정이 없어요.</EmptyText>
            )}
          </View>
        </View>

        <View style={styles.currentBlock}>
          {!hasTodaySchedules ? (
            <Pressable style={styles.registerButton} onPress={handleRegisterSchedule} hitSlop={8}>
              <Text style={styles.registerButtonText}>일정등록</Text>
            </Pressable>
          ) : current ? (
            <>
              <Text style={styles.currentTitle}>{current.academyName ?? current.title}</Text>
              <Text style={styles.currentTime}>
                {formatTimeRange(current.startAt, current.endAt)}
              </Text>
            </>
          ) : (
            <EmptyText style={styles.currentEmpty} />
          )}
        </View>
      </View>

      {hasTodaySchedules ? (
        <>
          <View style={styles.nextBar}>
            <Text style={styles.nextLabel}>다음일정</Text>
            {next ? (
              <View style={styles.nextContent}>
                <Text style={styles.nextTitle}>{next.academyName ?? next.title}</Text>
                <Text style={styles.nextTime}>{formatTimeRange(next.startAt, next.endAt)}</Text>
              </View>
            ) : (
              <EmptyText />
            )}
          </View>

          <View style={styles.todayHeader}>
            <Text style={styles.todayLabel}>오늘일정</Text>
            <Pressable style={styles.todayToggle} onPress={handleToggleToday}>
              <Text style={styles.todayCount}>총 {child.todayScheduleCount}개</Text>
              <View style={styles.chevronButton}>
                <Ionicons
                  name={expanded ? 'chevron-up' : 'chevron-down'}
                  size={16}
                  color={colors.textTertiary}
                />
              </View>
            </Pressable>
          </View>

          {expanded ? (
            <View style={styles.todayList}>
              {loadingToday ? <ActivityIndicator color={colors.primary} /> : null}
              {todayError ? <Text style={styles.todayError}>{todayError}</Text> : null}
              {!loadingToday && !todayError && todaySchedules.length === 0 ? <EmptyText /> : null}
              {todaySchedules.map((schedule) => (
                <TodayScheduleRow key={schedule.id} schedule={schedule} />
              ))}
            </View>
          ) : null}
        </>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    padding: spacing.md,
    gap: spacing.sm,
  },
  topRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    gap: spacing.sm,
  },
  identity: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
    flex: 1,
  },
  avatar: {
    width: 42,
    height: 42,
    borderRadius: radius.full,
    backgroundColor: colors.surfaceSubtle,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    ...typography.sectionTitle,
    color: colors.textSecondary,
  },
  identityText: {
    gap: spacing.xs,
  },
  childName: {
    ...typography.sectionTitle,
    color: colors.text,
  },
  statusRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
  },
  statusDotOuter: {
    width: 16,
    height: 16,
    borderRadius: radius.full,
    backgroundColor: colors.statusActiveBg,
    alignItems: 'center',
    justifyContent: 'center',
  },
  statusDotInner: {
    width: 10,
    height: 10,
    borderRadius: radius.full,
    backgroundColor: colors.statusActive,
  },
  statusText: {
    ...typography.bodySmall,
    color: colors.statusActive,
  },
  currentBlock: {
    alignItems: 'flex-end',
    justifyContent: 'center',
    minHeight: 44,
    maxWidth: '46%',
  },
  emptyStatus: {
    marginTop: 2,
  },
  registerButton: {
    paddingVertical: spacing.xs,
    paddingHorizontal: spacing.xs,
  },
  registerButtonText: {
    ...typography.bodySmallSemibold,
    color: colors.primary,
    textAlign: 'right',
  },
  currentTitle: {
    ...typography.bodySemibold,
    color: colors.textStrong,
    textAlign: 'right',
  },
  currentTime: {
    ...typography.bodySmall,
    color: colors.textTertiary,
    textAlign: 'right',
    marginTop: 2,
  },
  currentEmpty: {
    textAlign: 'right',
  },
  nextBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: colors.background,
    borderRadius: radius.md,
    padding: spacing.sm,
    gap: spacing.sm,
  },
  nextLabel: {
    ...typography.bodySmall,
    color: colors.textTertiary,
  },
  nextContent: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
    flexShrink: 1,
    justifyContent: 'flex-end',
  },
  nextTitle: {
    ...typography.bodySmallSemibold,
    color: colors.textTertiary,
  },
  nextTime: {
    ...typography.bodySmall,
    color: colors.textTertiary,
  },
  todayHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  todayLabel: {
    ...typography.bodySmall,
    color: colors.textTertiary,
  },
  todayToggle: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
  },
  todayCount: {
    ...typography.bodySmall,
    color: colors.textStrong,
  },
  chevronButton: {
    width: 32,
    height: 32,
    borderRadius: radius.full,
    backgroundColor: colors.surfaceSubtle,
    alignItems: 'center',
    justifyContent: 'center',
  },
  todayList: {
    gap: spacing.sm,
    paddingTop: spacing.xs,
  },
  todayRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: spacing.sm,
  },
  todayRowMain: {
    flex: 1,
    gap: 2,
  },
  todayRowTitle: {
    ...typography.bodySmallSemibold,
    color: colors.text,
  },
  todayRowTime: {
    ...typography.bodySmall,
    color: colors.textTertiary,
  },
  todayStatusPill: {
    borderRadius: radius.full,
    paddingHorizontal: spacing.sm,
    paddingVertical: 2,
    backgroundColor: colors.surfaceSubtle,
  },
  todayStatusPillActive: {
    backgroundColor: colors.statusActiveBg,
  },
  todayStatusText: {
    ...typography.caption,
    color: colors.textTertiary,
  },
  todayStatusTextActive: {
    color: colors.statusActive,
    fontWeight: '600',
  },
  todayError: {
    ...typography.bodySmall,
    color: '#DC2626',
  },
});
