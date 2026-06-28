import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { colors, spacing, typography } from '../theme';
import { ScheduleCard } from './ScheduleCard';

export function CompletedSchedulesSection({ completed }) {
  const [expanded, setExpanded] = useState(false);
  const peek = completed.length > 0 ? completed[completed.length - 1] : null;

  if (!completed.length) {
    return null;
  }

  return (
    <View style={styles.section}>
      <Pressable style={styles.toggle} onPress={() => setExpanded((value) => !value)}>
        <Text style={styles.toggleText}>
          {expanded ? '이전 일정 접기' : `총 ${completed.length}개의 이전일정 보기`}
        </Text>
      </Pressable>

      {!expanded && peek ? (
        <View style={styles.peekWrap}>
          <ScheduleCard schedule={peek} muted />
        </View>
      ) : null}

      {expanded
        ? completed
            .slice()
            .reverse()
            .map((schedule) => <ScheduleCard key={schedule.id} schedule={schedule} muted />)
        : null}
    </View>
  );
}

const styles = StyleSheet.create({
  section: {
    gap: spacing.sm,
  },
  toggle: {
    paddingVertical: spacing.sm,
  },
  toggleText: {
    ...typography.body,
    color: colors.primary,
    fontWeight: '600',
  },
  peekWrap: {
    overflow: 'hidden',
    maxHeight: 96,
  },
});
