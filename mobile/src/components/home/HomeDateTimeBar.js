import { useEffect, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { colors, radius, spacing, typography } from '../../theme';
import { formatClock, formatHomeDate } from '../../utils/datetime';

export function HomeDateTimeBar() {
  const [now, setNow] = useState(() => new Date());

  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  return (
    <View style={styles.container}>
      <View style={styles.left}>
        <Text style={styles.weather}>🌤️</Text>
        <Text style={styles.date}>{formatHomeDate(now)}</Text>
      </View>
      <Text style={styles.clock}>{formatClock(now)}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: colors.surface,
    borderRadius: radius.pill,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  left: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  weather: {
    fontSize: 24,
    lineHeight: 28,
  },
  date: {
    ...typography.body,
    color: colors.textTertiary,
  },
  clock: {
    ...typography.bodySemibold,
    color: colors.textSecondary,
  },
});
