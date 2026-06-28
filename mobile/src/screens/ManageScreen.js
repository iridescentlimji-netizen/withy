import { Pressable, StyleSheet, Text, View } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { AppHeader } from '../components/AppHeader';
import { useFamily } from '../context/FamilyContext';
import { colors, spacing, typography } from '../theme';

export function ManageScreen() {
  const navigation = useNavigation();
  const { activeFamily } = useFamily();

  return (
    <View style={styles.container}>
      <AppHeader subtitle={`${activeFamily?.name ?? ''} · 관리`} />

      <Pressable style={styles.menuItem} onPress={() => navigation.navigate('ChildSetup')}>
        <Text style={styles.menuTitle}>아이 등록</Text>
        <Text style={styles.menuMeta}>아이 이름과 출생 연도를 등록합니다.</Text>
      </Pressable>

      <Pressable style={styles.menuItem} onPress={() => navigation.navigate('AcademyManagement')}>
        <Text style={styles.menuTitle}>학원 관리</Text>
        <Text style={styles.menuMeta}>학원 목록을 추가·수정·삭제합니다.</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
    padding: spacing.lg,
    gap: spacing.md,
  },
  menuItem: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 12,
    padding: spacing.md,
    gap: spacing.xs,
  },
  menuTitle: {
    ...typography.body,
    fontWeight: '600',
    color: colors.text,
  },
  menuMeta: {
    ...typography.caption,
    color: colors.textSecondary,
  },
});
