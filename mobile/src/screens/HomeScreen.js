import { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useFocusEffect, useNavigation } from '@react-navigation/native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { ChildStatusCard } from '../components/home/ChildStatusCard';
import { HomeDateTimeBar } from '../components/home/HomeDateTimeBar';
import { HomeHeader } from '../components/home/HomeHeader';
import { ReturnPlanSection } from '../components/home/ReturnPlanSection';
import { formatMemberRole } from '../constants/profile';
import { useFamily } from '../context/FamilyContext';
import { getHome } from '../services/api';
import { colors, spacing, typography } from '../theme';

export function HomeScreen() {
  const navigation = useNavigation();
  const insets = useSafeAreaInsets();
  const { activeFamily } = useFamily();
  const [home, setHome] = useState(null);
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState('');
  const [refreshing, setRefreshing] = useState(false);

  const loadHome = useCallback(async () => {
    if (!activeFamily?.id) {
      return;
    }

    setError('');
    setStatus('loading');

    try {
      const data = await getHome(activeFamily.id);
      setHome(data);
      setStatus('ready');
    } catch (loadError) {
      setHome(null);
      setStatus('error');
      setError(loadError.message ?? '홈 정보를 불러오지 못했습니다.');
    }
  }, [activeFamily?.id]);

  useFocusEffect(
    useCallback(() => {
      loadHome();
    }, [loadHome]),
  );

  const handleRefresh = useCallback(async () => {
    setRefreshing(true);
    await loadHome();
    setRefreshing(false);
  }, [loadHome]);

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={[
        styles.content,
        { paddingTop: insets.top + spacing.md, paddingBottom: insets.bottom + spacing.xl },
      ]}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={handleRefresh} />}
    >
      {status === 'ready' && home ? (
        <View style={styles.topGroup}>
          <HomeHeader
            nickname={home.nickname}
            roleLabel={formatMemberRole(home.role)}
            onPressNotifications={() => {}}
          />
          <HomeDateTimeBar />
        </View>
      ) : (
        <HomeDateTimeBar />
      )}

      {status === 'loading' ? (
        <ActivityIndicator color={colors.primary} style={styles.loader} />
      ) : null}

      {status === 'error' ? (
        <View style={styles.errorCard}>
          <Text style={styles.error}>{error}</Text>
          <Pressable style={styles.retryButton} onPress={loadHome}>
            <Text style={styles.retryButtonText}>다시 시도</Text>
          </Pressable>
        </View>
      ) : null}

      {status === 'ready' && home ? (
        <>
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>아이는 지금</Text>
            {home.children?.length ? (
              <View style={styles.cardList}>
                {home.children.map((child) => (
                  <ChildStatusCard key={child.childId} child={child} familyId={activeFamily?.id} />
                ))}
              </View>
            ) : (
              <View style={styles.emptyCard}>
                <Text style={styles.emptyText}>등록된 아이가 없습니다.</Text>
                <Pressable
                  style={styles.setupButton}
                  onPress={() => navigation.navigate('ChildSetup')}
                >
                  <Text style={styles.setupButtonText}>아이 등록하기</Text>
                </Pressable>
              </View>
            )}
          </View>

          <ReturnPlanSection
            familyChildren={home.children}
            returnPlans={home.returnPlans}
            onPressEdit={() => navigation.navigate('Schedule')}
          />
        </>
      ) : null}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    paddingHorizontal: spacing.md,
    gap: spacing.lg,
  },
  topGroup: {
    gap: spacing.md,
  },
  loader: {
    marginTop: spacing.md,
  },
  section: {
    gap: spacing.md,
  },
  sectionTitle: {
    ...typography.sectionTitle,
    color: colors.text,
  },
  cardList: {
    gap: spacing.sm + spacing.xs,
  },
  emptyCard: {
    backgroundColor: colors.surface,
    borderRadius: 32,
    padding: spacing.md,
    gap: spacing.sm,
  },
  emptyText: {
    ...typography.bodySmall,
    color: colors.textTertiary,
  },
  setupButton: {
    alignSelf: 'flex-start',
    backgroundColor: colors.primary,
    borderRadius: 8,
    paddingVertical: spacing.sm,
    paddingHorizontal: spacing.md,
  },
  setupButtonText: {
    ...typography.body,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  errorCard: {
    backgroundColor: colors.surface,
    borderRadius: 32,
    padding: spacing.md,
    gap: spacing.sm,
  },
  error: {
    ...typography.body,
    color: '#DC2626',
  },
  retryButton: {
    alignSelf: 'flex-start',
    paddingVertical: spacing.sm,
    paddingHorizontal: spacing.md,
    borderRadius: 8,
    backgroundColor: colors.surfaceSubtle,
  },
  retryButtonText: {
    ...typography.body,
    color: colors.textSecondary,
  },
});
