import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, spacing, typography } from '../../theme';

export function ScheduleScreenHeader({ onPressSearch, onPressAdd }) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>일정관리</Text>
      <View style={styles.actions}>
        <Pressable style={styles.circleButton} onPress={onPressSearch} hitSlop={8}>
          <Ionicons name="search-outline" size={20} color={colors.textTertiary} />
        </Pressable>
        <Pressable style={styles.circleButton} onPress={onPressAdd} hitSlop={8}>
          <Ionicons name="add" size={20} color={colors.primary} />
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  title: {
    ...typography.userName,
    color: colors.text,
  },
  actions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
  },
  circleButton: {
    width: 38,
    height: 38,
    borderRadius: 53,
    backgroundColor: colors.surface,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
