import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { SCHEDULE_STATUS_LABELS, SUBJECT_CATEGORY_LABELS } from '../../constants/schedule';
import { colors } from '../../theme';
import { formatTimeRange } from '../../utils/datetime';

const SUBJECT_CHIP_BG = '#EBECF9';
const SUBJECT_CHIP_TEXT = '#320096';

function StatusBadge({ status }) {
  const label = SCHEDULE_STATUS_LABELS[status] ?? SCHEDULE_STATUS_LABELS.UPCOMING;
  const isActive = status === 'IN_PROGRESS';

  return (
    <View style={[styles.statusBadge, isActive ? styles.statusBadgeActive : styles.statusBadgeMuted]}>
      <Text style={[styles.statusText, isActive ? styles.statusTextActive : styles.statusTextMuted]}>
        {label}
      </Text>
    </View>
  );
}

function SubjectBadge({ category }) {
  if (!category) {
    return null;
  }

  const label = SUBJECT_CATEGORY_LABELS[category] ?? category;

  return (
    <View style={styles.subjectBadge}>
      <Text style={styles.subjectBadgeText}>{label}</Text>
    </View>
  );
}

export function ScheduleListCard({ schedule, muted = false, onPressMenu }) {
  const displayName = schedule.academyName ?? schedule.title;

  return (
    <View style={[styles.card, muted && styles.cardMuted]}>
      <View style={styles.leftBlock}>
        <StatusBadge status={schedule.status} />
        <Text style={styles.childName}>{schedule.childNickname}</Text>
      </View>

      <View style={styles.rightBlock}>
        <View style={styles.detailColumn}>
          <View style={styles.subjectRow}>
            <SubjectBadge category={schedule.subjectCategory} />
            <Text style={styles.academyName} numberOfLines={1}>
              {displayName}
            </Text>
          </View>
          <Text style={styles.timeText}>{formatTimeRange(schedule.startAt, schedule.endAt)}</Text>
        </View>

        <Pressable style={styles.menuButton} onPress={onPressMenu} hitSlop={8} disabled={!onPressMenu}>
          {onPressMenu ? (
            <Ionicons name="ellipsis-horizontal" size={16} color={colors.textTertiary} />
          ) : null}
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: colors.surface,
    borderRadius: 16,
    paddingTop: 8,
    paddingBottom: 8,
    paddingLeft: 8,
    paddingRight: 4,
    gap: 8,
  },
  cardMuted: {
    opacity: 0.45,
  },
  leftBlock: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    flexShrink: 0,
  },
  statusBadge: {
    borderRadius: 31,
    paddingHorizontal: 4,
    paddingVertical: 2,
  },
  statusBadgeActive: {
    backgroundColor: colors.statusActive,
  },
  statusBadgeMuted: {
    backgroundColor: colors.background,
  },
  statusText: {
    fontSize: 16,
    lineHeight: 20,
    fontWeight: '500',
  },
  statusTextActive: {
    color: colors.surface,
  },
  statusTextMuted: {
    color: colors.textTertiary,
  },
  childName: {
    fontSize: 16,
    lineHeight: 20,
    fontWeight: '700',
    color: colors.textSecondary,
  },
  rightBlock: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-end',
    minWidth: 0,
  },
  detailColumn: {
    flex: 1,
    alignItems: 'flex-end',
    gap: 4,
    minWidth: 0,
  },
  subjectRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-end',
    gap: 4,
    maxWidth: '100%',
  },
  subjectBadge: {
    backgroundColor: SUBJECT_CHIP_BG,
    borderRadius: 10,
    paddingHorizontal: 4,
    paddingVertical: 2,
    flexShrink: 0,
  },
  subjectBadgeText: {
    fontSize: 12,
    lineHeight: 16,
    fontWeight: '600',
    color: SUBJECT_CHIP_TEXT,
  },
  academyName: {
    fontSize: 16,
    lineHeight: 20,
    fontWeight: '700',
    color: colors.textSecondary,
    flexShrink: 1,
    textAlign: 'right',
  },
  timeText: {
    fontSize: 16,
    lineHeight: 20,
    fontWeight: '500',
    color: colors.textTertiary,
    textAlign: 'right',
    alignSelf: 'stretch',
  },
  menuButton: {
    width: 24,
    height: 24,
    borderRadius: 37,
    backgroundColor: colors.background,
    alignItems: 'center',
    justifyContent: 'center',
    marginHorizontal: 10,
    flexShrink: 0,
  },
});
