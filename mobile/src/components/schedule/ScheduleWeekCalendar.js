import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, spacing } from '../../theme';
import {
  addWeeksToDateParam,
  formatMonthYearLabel,
  getWeekDateParams,
  parseDateParam,
} from '../../utils/datetime';

const WEEKDAY_LABELS = ['일', '월', '화', '수', '목', '금', '토'];
const WEEKEND_ORANGE = '#EE723F';
const WEEKDAY_MUTED = '#C1C2C5';
const SELECTED_DARK = '#2B2D35';

function getDayTextColors(index, selected) {
  const isWeekend = index === 0 || index === 6;

  if (selected) {
    return {
      weekday: isWeekend ? WEEKEND_ORANGE : colors.surface,
      date: isWeekend ? WEEKEND_ORANGE : colors.surface,
    };
  }

  if (isWeekend) {
    return { weekday: WEEKEND_ORANGE, date: WEEKEND_ORANGE };
  }

  return { weekday: WEEKDAY_MUTED, date: colors.textSecondary };
}

function getCountTextColor(count, selected) {
  if (selected) {
    return colors.surface;
  }
  if (count === 0) {
    return WEEKDAY_MUTED;
  }
  return colors.textSecondary;
}

export function ScheduleWeekCalendar({ selectedDate, countsByDate = {}, onSelectDate, onChangeWeek }) {
  const weekDates = getWeekDateParams(selectedDate);

  return (
    <View style={styles.container}>
      <View style={styles.monthRow}>
        <Pressable
          style={styles.weekNavButton}
          onPress={() => onChangeWeek?.(-1)}
          accessibilityLabel="이전 주"
        >
          <Ionicons name="chevron-back" size={20} color={colors.textSecondary} />
        </Pressable>
        <Text style={styles.monthLabel}>{formatMonthYearLabel(selectedDate)}</Text>
        <Pressable
          style={styles.weekNavButton}
          onPress={() => onChangeWeek?.(1)}
          accessibilityLabel="다음 주"
        >
          <Ionicons name="chevron-forward" size={20} color={colors.textSecondary} />
        </Pressable>
      </View>

      <View style={styles.body}>
        <View style={styles.dayRow}>
          {weekDates.map((dateParam, index) => {
            const selected = dateParam === selectedDate;
            const day = parseDateParam(dateParam).getDate();
            const textColors = getDayTextColors(index, selected);

            return (
              <Pressable
                key={dateParam}
                style={[styles.dayPill, selected && styles.dayPillSelected]}
                onPress={() => onSelectDate(dateParam)}
              >
                <Text style={[styles.weekdayInPill, { color: textColors.weekday }]}>
                  {WEEKDAY_LABELS[index]}
                </Text>
                <Text
                  style={[styles.dateInPill, { color: textColors.date }]}
                  numberOfLines={1}
                  adjustsFontSizeToFit
                  minimumFontScale={0.85}
                >
                  {day}
                </Text>
              </Pressable>
            );
          })}
        </View>

        <View style={styles.countSection}>
          <Text style={styles.countHeaderLabel}>일정</Text>
          <View style={styles.countRow}>
            {weekDates.map((dateParam) => {
              const selected = dateParam === selectedDate;
              const count = countsByDate[dateParam] ?? 0;

              return (
                <View
                  key={`${dateParam}-count`}
                  style={[styles.countPill, selected && styles.countPillSelected]}
                >
                  <Text
                    style={[
                      styles.countText,
                      { color: getCountTextColor(count, selected) },
                      selected && styles.countTextSelected,
                    ]}
                    numberOfLines={1}
                  >
                    {count}
                  </Text>
                </View>
              );
            })}
          </View>
        </View>
      </View>
    </View>
  );
}

export function shiftSelectedWeek(selectedDate, direction) {
  return addWeeksToDateParam(selectedDate, direction);
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.surface,
  },
  monthRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingTop: 10,
    paddingBottom: 10,
    paddingHorizontal: spacing.sm,
    backgroundColor: colors.surface,
  },
  weekNavButton: {
    width: 32,
    height: 32,
    alignItems: 'center',
    justifyContent: 'center',
  },
  monthLabel: {
    fontSize: 18,
    lineHeight: 24,
    fontWeight: '600',
    color: colors.textSecondary,
    textAlign: 'center',
    flex: 1,
  },
  body: {
    paddingBottom: spacing.md,
    paddingHorizontal: spacing.md,
    gap: spacing.sm,
    backgroundColor: colors.surface,
  },
  dayRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: spacing.sm,
  },
  dayPill: {
    width: 40,
    height: 70,
    borderRadius: 47,
    paddingVertical: 12,
    backgroundColor: colors.surface,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 3,
  },
  dayPillSelected: {
    backgroundColor: SELECTED_DARK,
  },
  weekdayInPill: {
    fontSize: 14,
    lineHeight: 18,
    fontWeight: '500',
  },
  dateInPill: {
    width: 40,
    fontSize: 18,
    lineHeight: 22,
    fontWeight: '500',
    textAlign: 'center',
  },
  countSection: {
    gap: spacing.sm,
  },
  countHeaderLabel: {
    fontSize: 14,
    lineHeight: 18,
    fontWeight: '500',
    color: colors.textSecondary,
  },
  countRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: spacing.sm,
  },
  countPill: {
    width: 40,
    minHeight: 48,
    borderRadius: 51,
    backgroundColor: colors.surface,
    alignItems: 'center',
    justifyContent: 'center',
  },
  countPillSelected: {
    backgroundColor: SELECTED_DARK,
  },
  countText: {
    fontSize: 18,
    lineHeight: 24,
    fontWeight: '600',
    textAlign: 'center',
  },
  countTextSelected: {
    fontWeight: '600',
  },
});
