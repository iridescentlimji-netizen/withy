import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, radius, spacing, typography } from '../../theme';

export function HomeHeader({ nickname, roleLabel, onPressNotifications }) {
  return (
    <View style={styles.container}>
      <View style={styles.nameRow}>
        <Text style={styles.name}>{nickname}님</Text>
        {roleLabel ? (
          <View style={styles.roleBadge}>
            <Text style={styles.roleBadgeText}>{roleLabel}</Text>
          </View>
        ) : null}
      </View>
      <Pressable style={styles.notificationButton} onPress={onPressNotifications} hitSlop={8}>
        <Ionicons name="notifications-outline" size={24} color={colors.textTertiary} />
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'space-between',
  },
  nameRow: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: spacing.xs,
    flex: 1,
    flexWrap: 'wrap',
  },
  name: {
    ...typography.userName,
    color: colors.text,
  },
  roleBadge: {
    backgroundColor: colors.surface,
    borderRadius: radius.sm,
    paddingHorizontal: spacing.xs,
    paddingVertical: 2,
    marginBottom: 2,
  },
  roleBadgeText: {
    ...typography.badge,
    color: colors.textSecondary,
  },
  notificationButton: {
    width: 42,
    height: 42,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
