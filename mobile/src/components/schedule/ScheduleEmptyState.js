import { Pressable, StyleSheet, Text, View } from 'react-native';
import { colors, spacing, typography } from '../../theme';

export function ScheduleEmptyState({ onPressRegister }) {
  return (
    <View style={styles.container}>
      <View style={styles.messageBlock}>
        <Text style={styles.emoji}>😴</Text>
        <Text style={styles.message}>오늘은 일정이 없어요.{'\n'}일정을 등록할까요?</Text>
      </View>
      <Pressable style={styles.button} onPress={onPressRegister}>
        <Text style={styles.buttonText}>일정 등록</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    gap: 32,
    width: '100%',
  },
  messageBlock: {
    alignItems: 'center',
    gap: spacing.sm,
    paddingTop: 10,
    width: '100%',
  },
  emoji: {
    fontSize: 24,
    lineHeight: 28,
    fontWeight: '600',
    textAlign: 'center',
    color: colors.textSecondary,
  },
  message: {
    fontSize: 16,
    lineHeight: 20,
    fontWeight: '600',
    color: colors.textTertiary,
    textAlign: 'center',
  },
  button: {
    alignSelf: 'stretch',
    backgroundColor: colors.primary,
    borderRadius: 16,
    padding: 12,
    alignItems: 'center',
  },
  buttonText: {
    ...typography.bodySemibold,
    color: colors.surface,
    lineHeight: 24,
  },
});
