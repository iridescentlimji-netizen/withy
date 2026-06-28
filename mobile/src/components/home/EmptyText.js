import { StyleSheet, Text } from 'react-native';
import { colors, typography } from '../../theme';

export function EmptyText({ children = '없음', style }) {
  return <Text style={[styles.text, style]}>{children}</Text>;
}

const styles = StyleSheet.create({
  text: {
    ...typography.bodySmall,
    color: colors.textTertiary,
  },
});
