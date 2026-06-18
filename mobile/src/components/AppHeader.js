import { StyleSheet, Text, View } from 'react-native';
import { APP_NAME } from '../config/env';
import { colors, spacing, typography } from '../theme';

export function AppHeader({ subtitle }) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>{APP_NAME}</Text>
      {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: spacing.lg,
  },
  title: {
    ...typography.title,
    color: colors.text,
  },
  subtitle: {
    ...typography.body,
    color: colors.textSecondary,
    marginTop: spacing.xs,
  },
});
