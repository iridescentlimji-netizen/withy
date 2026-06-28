import { Pressable, StyleSheet, Text, View } from 'react-native';
import { SCHEDULE_TYPE_LABELS, SUBJECT_CATEGORY_LABELS } from '../constants/schedule';
import { colors, spacing, typography } from '../theme';
import { formatTime } from '../utils/datetime';

export function ScheduleCard({ schedule, muted = false, onPress }) {
  const isPickup = schedule.scheduleType === 'PICKUP';

  return (
    <Pressable
      style={[styles.card, isPickup && styles.pickupCard, muted && styles.mutedCard]}
      onPress={onPress}
      disabled={!onPress}
    >
      <View style={styles.headerRow}>
        <Text style={[styles.badge, isPickup && styles.pickupBadge]}>
          {SCHEDULE_TYPE_LABELS[schedule.scheduleType] ?? schedule.scheduleType}
        </Text>
        <Text style={styles.time}>
          {formatTime(schedule.startAt)} – {formatTime(schedule.endAt)}
        </Text>
      </View>
      <Text style={styles.title}>{schedule.title}</Text>
      <Text style={styles.meta}>{schedule.childNickname}</Text>
      {schedule.academyName ? <Text style={styles.meta}>{schedule.academyName}</Text> : null}
      {schedule.subjectCategory ? (
        <Text style={styles.meta}>
          {SUBJECT_CATEGORY_LABELS[schedule.subjectCategory] ?? schedule.subjectCategory}
        </Text>
      ) : null}
      {schedule.pickupGuardianNickname ? (
        <Text style={styles.meta}>귀가: {schedule.pickupGuardianNickname}</Text>
      ) : null}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 12,
    padding: spacing.md,
    gap: spacing.xs,
    backgroundColor: colors.background,
  },
  pickupCard: {
    borderColor: '#F59E0B',
    backgroundColor: '#FFFBEB',
  },
  mutedCard: {
    opacity: 0.4,
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  badge: {
    ...typography.caption,
    fontWeight: '600',
    color: colors.primary,
    backgroundColor: '#EFF6FF',
    paddingHorizontal: spacing.sm,
    paddingVertical: 2,
    borderRadius: 999,
  },
  pickupBadge: {
    color: '#B45309',
    backgroundColor: '#FEF3C7',
  },
  time: {
    ...typography.caption,
    color: colors.textSecondary,
  },
  title: {
    ...typography.body,
    fontWeight: '600',
    color: colors.text,
  },
  meta: {
    ...typography.caption,
    color: colors.textSecondary,
  },
});
