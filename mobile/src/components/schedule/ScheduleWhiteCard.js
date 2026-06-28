import { Platform, StyleSheet, View } from 'react-native';
import { colors } from '../../theme';

export function ScheduleWhiteCard({ children, style }) {
  return <View style={[styles.card, style]}>{children}</View>;
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.surface,
    borderRadius: 32,
    padding: 16,
    ...Platform.select({
      ios: {
        shadowColor: '#5E5E5E',
        shadowOffset: { width: 0, height: 0 },
        shadowOpacity: 0.12,
        shadowRadius: 8,
      },
      android: {
        elevation: 3,
      },
    }),
  },
});
