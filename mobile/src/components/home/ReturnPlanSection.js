import { Pressable, StyleSheet, Text, View } from 'react-native';
import { colors, radius, spacing, typography } from '../../theme';
import { formatRelativeUntil, formatTime } from '../../utils/datetime';
import { EmptyText } from './EmptyText';

function ReturnPlanCard({ childName, plan }) {
  const hasPlan = Boolean(plan);

  return (
    <View style={styles.item}>
      <View style={styles.summaryRow}>
        <Text style={styles.childName}>{childName}</Text>
        {hasPlan ? (
          <View style={styles.timeGroup}>
            <Text style={styles.relativeTime}>{formatRelativeUntil(plan.scheduledAt)}</Text>
            <View style={styles.divider} />
            <Text style={styles.scheduledTime}>{formatTime(plan.scheduledAt)} 예정</Text>
          </View>
        ) : (
          <View style={styles.timeGroup}>
            <EmptyText>-</EmptyText>
            <View style={styles.divider} />
            <EmptyText>예정없음</EmptyText>
          </View>
        )}
      </View>

      <View style={styles.pickupRow}>
        <Text style={styles.pickupLabel}>픽업</Text>
        {hasPlan && plan.pickupGuardianNickname ? (
          <Text style={styles.pickupValue}>{plan.pickupGuardianNickname}</Text>
        ) : (
          <EmptyText>지정없음</EmptyText>
        )}
      </View>
    </View>
  );
}

export function ReturnPlanSection({ familyChildren, returnPlans, onPressEdit }) {
  const planByChildId = new Map((returnPlans ?? []).map((plan) => [plan.childId, plan]));

  return (
    <View style={styles.section}>
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>귀가 일정</Text>
        <Pressable style={styles.editButton} onPress={onPressEdit} hitSlop={8}>
          <Text style={styles.editButtonText}>편집</Text>
        </Pressable>
      </View>

      <View style={styles.card}>
        {familyChildren?.length ? (
          familyChildren.map((child) => (
            <ReturnPlanCard
              key={child.childId}
              childName={child.nickname}
              plan={planByChildId.get(child.childId)}
            />
          ))
        ) : (
          <EmptyText>등록된 아이가 없습니다.</EmptyText>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  section: {
    gap: spacing.md,
  },
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  sectionTitle: {
    ...typography.sectionTitle,
    color: colors.text,
  },
  editButton: {
    backgroundColor: colors.surface,
    borderRadius: radius.sm,
    padding: spacing.xs,
  },
  editButtonText: {
    ...typography.body,
    fontWeight: '500',
    lineHeight: 20,
    color: colors.textSecondary,
  },
  card: {
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    padding: spacing.md,
    gap: 20,
  },
  item: {
    gap: spacing.xs,
  },
  summaryRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: colors.surface,
    borderRadius: radius.md,
    padding: spacing.sm,
    gap: spacing.sm,
  },
  childName: {
    fontSize: 18,
    fontWeight: '700',
    lineHeight: 18,
    color: colors.text,
    flexShrink: 0,
  },
  timeGroup: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
    flexShrink: 1,
    justifyContent: 'flex-end',
  },
  relativeTime: {
    ...typography.bodySmall,
    fontWeight: '500',
    color: colors.textTertiary,
  },
  scheduledTime: {
    ...typography.bodySmallSemibold,
    color: colors.text,
  },
  divider: {
    width: 1,
    height: 12,
    backgroundColor: colors.divider,
  },
  pickupRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: colors.background,
    borderRadius: radius.md,
    padding: spacing.sm,
    gap: spacing.sm,
  },
  pickupLabel: {
    ...typography.bodySmall,
    fontWeight: '500',
    color: colors.textTertiary,
    flexShrink: 0,
  },
  pickupValue: {
    ...typography.bodySmall,
    fontWeight: '500',
    color: colors.text,
    textAlign: 'right',
    flexShrink: 1,
  },
});
