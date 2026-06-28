import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { colors } from '../../theme';
import { ScheduleListCard } from './ScheduleListCard';

export function ScheduleCompletedSection({ completed }) {
  const [expanded, setExpanded] = useState(false);
  const count = completed.length;
  const canExpand = count > 0;

  return (
    <View style={styles.wrapper}>
      <View style={styles.headerCard}>
        <Text style={styles.label}>총 {count}개의 이전일정</Text>
        {canExpand ? (
          <Pressable onPress={() => setExpanded((value) => !value)} hitSlop={8}>
            <Text style={styles.action}>{expanded ? '접기' : '보기'}</Text>
          </Pressable>
        ) : (
          <Text style={styles.label}>보기</Text>
        )}
      </View>

      {expanded && canExpand
        ? completed
            .slice()
            .reverse()
            .map((schedule) => <ScheduleListCard key={schedule.id} schedule={schedule} muted />)
        : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    gap: 8,
  },
  headerCard: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: colors.surface,
    borderRadius: 16,
    padding: 10,
  },
  label: {
    fontSize: 14,
    lineHeight: 18,
    fontWeight: '500',
    color: colors.textTertiary,
  },
  action: {
    fontSize: 14,
    lineHeight: 18,
    fontWeight: '500',
    color: colors.textTertiary,
  },
});
