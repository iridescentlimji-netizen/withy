import { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { useFocusEffect, useNavigation } from '@react-navigation/native';
import { AppHeader } from '../components/AppHeader';
import { useAuth } from '../context/AuthContext';
import { useFamily } from '../context/FamilyContext';
import { formatMemberRole, isPlaceholderNickname } from '../constants/profile';
import {
  approveJoinRequest,
  createInviteCode,
  getHome,
  listPendingJoinRequests,
  rejectJoinRequest,
} from '../services/api';
import { fetchOAuthLinks, linkOAuthProvider } from '../services/auth';
import { colors, radius, spacing, typography } from '../theme';

const OAUTH_PROVIDERS = [
  { id: 'kakao', label: '카카오' },
  { id: 'naver', label: '네이버' },
  { id: 'google', label: 'Google' },
];

export function MyScreen() {
  const navigation = useNavigation();
  const { user, logout, updateNickname, error, setError } = useAuth();
  const { activeFamily } = useFamily();
  const [roleLabel, setRoleLabel] = useState('');
  const [nicknameInput, setNicknameInput] = useState(user?.nickname ?? '');
  const [isSaving, setIsSaving] = useState(false);
  const [linkedProviders, setLinkedProviders] = useState([]);
  const [isLoadingLinks, setIsLoadingLinks] = useState(true);
  const [linkingProvider, setLinkingProvider] = useState(null);
  const [memberRole, setMemberRole] = useState('');
  const [inviteCode, setInviteCode] = useState('');
  const [inviteExpiresAt, setInviteExpiresAt] = useState('');
  const [isCreatingInvite, setIsCreatingInvite] = useState(false);
  const [joinRequests, setJoinRequests] = useState([]);
  const [isLoadingJoinRequests, setIsLoadingJoinRequests] = useState(false);
  const [reviewingRequestId, setReviewingRequestId] = useState(null);

  const loadOAuthLinks = useCallback(async () => {
    setIsLoadingLinks(true);
    try {
      const data = await fetchOAuthLinks();
      setLinkedProviders(data.links?.map((link) => link.provider.toLowerCase()) ?? []);
    } catch {
      setLinkedProviders([]);
    } finally {
      setIsLoadingLinks(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      loadOAuthLinks();
    }, [loadOAuthLinks]),
  );

  useEffect(() => {
    setNicknameInput(user?.nickname ?? '');
  }, [user?.nickname]);

  const loadFamilyHome = useCallback(async () => {
    if (!activeFamily?.id) {
      return;
    }

    setIsLoadingJoinRequests(true);

    try {
      const home = await getHome(activeFamily.id);
      setRoleLabel(formatMemberRole(home.role));
      setMemberRole(home.role ?? '');

      if (home.role === 'MASTER') {
        setJoinRequests(await listPendingJoinRequests(activeFamily.id));
      } else {
        setJoinRequests([]);
      }
    } catch {
      setRoleLabel('');
      setMemberRole('');
      setJoinRequests([]);
    } finally {
      setIsLoadingJoinRequests(false);
    }
  }, [activeFamily?.id]);

  useEffect(() => {
    loadFamilyHome();
  }, [loadFamilyHome]);

  useFocusEffect(
    useCallback(() => {
      loadFamilyHome();
    }, [loadFamilyHome]),
  );

  const handleSaveNickname = useCallback(async () => {
    const trimmed = nicknameInput.trim();
    if (!trimmed) {
      setError('이름을 입력해 주세요.');
      return;
    }

    setIsSaving(true);
    setError('');

    try {
      await updateNickname(trimmed);
    } catch (saveError) {
      setError(saveError.message ?? '이름을 저장하지 못했습니다.');
    } finally {
      setIsSaving(false);
    }
  }, [nicknameInput, setError, updateNickname]);

  const handleLinkProvider = useCallback(
    async (providerId) => {
      setLinkingProvider(providerId);
      setError('');

      try {
        const data = await linkOAuthProvider(providerId);
        setLinkedProviders(data.links?.map((link) => link.provider.toLowerCase()) ?? []);
      } catch (linkError) {
        setError(linkError.message ?? '로그인 연결에 실패했습니다.');
      } finally {
        setLinkingProvider(null);
      }
    },
    [setError],
  );

  const handleCreateInvite = useCallback(async () => {
    if (!activeFamily?.id) {
      return;
    }

    setIsCreatingInvite(true);
    setError('');

    try {
      const response = await createInviteCode(activeFamily.id, {
        role: 'FAMILY',
        canEdit: true,
      });
      setInviteCode(response.code);
      setInviteExpiresAt(response.expiresAt ?? '');
    } catch (inviteError) {
      setError(inviteError.message ?? '초대 코드를 만들지 못했습니다.');
    } finally {
      setIsCreatingInvite(false);
    }
  }, [activeFamily?.id, setError]);

  const handleReviewJoinRequest = useCallback(
    async (requestId, action) => {
      if (!activeFamily?.id) {
        return;
      }

      setReviewingRequestId(requestId);
      setError('');

      try {
        if (action === 'approve') {
          await approveJoinRequest(activeFamily.id, requestId);
        } else {
          await rejectJoinRequest(activeFamily.id, requestId);
        }
        await loadFamilyHome();
      } catch (reviewError) {
        setError(reviewError.message ?? '가입 요청 처리에 실패했습니다.');
      } finally {
        setReviewingRequestId(null);
      }
    },
    [activeFamily?.id, loadFamilyHome, setError],
  );

  const showPlaceholderHint = isPlaceholderNickname(user?.nickname);
  const displayName = showPlaceholderHint ? '이름을 설정해 주세요' : `${user?.nickname ?? ''}님`;

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <AppHeader subtitle={activeFamily?.name ?? ''} />

      <View style={styles.profileCard}>
        <View style={styles.profileHeader}>
          <Text style={styles.profileName}>{displayName}</Text>
          {roleLabel ? (
            <View style={styles.roleBadge}>
              <Text style={styles.roleBadgeText}>{roleLabel}</Text>
            </View>
          ) : null}
        </View>
        {showPlaceholderHint ? (
          <Text style={styles.profileHint}>
            로그인 계정에서 이름을 받지 못했어요. 아래에서 표시할 이름을 입력해 주세요.
          </Text>
        ) : null}
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionLabel}>표시 이름</Text>
        <TextInput
          style={styles.input}
          value={nicknameInput}
          onChangeText={setNicknameInput}
          placeholder="예: 임지현"
          editable={!isSaving}
        />
        <Pressable
          style={[styles.saveButton, isSaving && styles.saveButtonDisabled]}
          onPress={handleSaveNickname}
          disabled={isSaving}
        >
          {isSaving ? (
            <ActivityIndicator color="#FFFFFF" />
          ) : (
            <Text style={styles.saveButtonText}>이름 저장</Text>
          )}
        </Pressable>
        {error ? <Text style={styles.error}>{error}</Text> : null}
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionLabel}>연결된 로그인</Text>
        <Text style={styles.sectionHint}>카카오·네이버·Google 중 연결하면 어떤 방법으로 로그인해도 같은 가족 데이터를 볼 수 있어요.</Text>
        {isLoadingLinks ? (
          <ActivityIndicator color={colors.primary} />
        ) : (
          OAUTH_PROVIDERS.map((provider) => {
            const isLinked = linkedProviders.includes(provider.id);
            const isLinking = linkingProvider === provider.id;

            return (
              <View key={provider.id} style={styles.linkRow}>
                <Text style={styles.linkLabel}>{provider.label}</Text>
                {isLinked ? (
                  <Text style={styles.linkStatus}>연결됨</Text>
                ) : (
                  <Pressable
                    style={[styles.linkButton, isLinking && styles.linkButtonDisabled]}
                    onPress={() => handleLinkProvider(provider.id)}
                    disabled={Boolean(linkingProvider)}
                  >
                    {isLinking ? (
                      <ActivityIndicator color={colors.primary} size="small" />
                    ) : (
                      <Text style={styles.linkButtonText}>연결하기</Text>
                    )}
                  </Pressable>
                )}
              </View>
            );
          })
        )}
      </View>

      {memberRole === 'MASTER' ? (
        <View style={styles.section}>
          <Text style={styles.sectionLabel}>가족 초대</Text>
          <Text style={styles.sectionHint}>
            초대 코드를 만들어 다른 보호자에게 공유하세요. 코드는 24시간 동안 유효합니다.
          </Text>
          <Pressable
            style={[styles.saveButton, isCreatingInvite && styles.saveButtonDisabled]}
            onPress={handleCreateInvite}
            disabled={isCreatingInvite}
          >
            {isCreatingInvite ? (
              <ActivityIndicator color="#FFFFFF" />
            ) : (
              <Text style={styles.saveButtonText}>초대 코드 만들기</Text>
            )}
          </Pressable>
          {inviteCode ? (
            <View style={styles.inviteCodeBox}>
              <Text style={styles.inviteCodeLabel}>초대 코드</Text>
              <Text style={styles.inviteCodeValue}>{inviteCode}</Text>
              {inviteExpiresAt ? (
                <Text style={styles.sectionHint}>
                  만료: {new Date(inviteExpiresAt).toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}
                </Text>
              ) : null}
            </View>
          ) : null}

          <Text style={[styles.sectionLabel, styles.joinRequestTitle]}>가입 승인 대기</Text>
          {isLoadingJoinRequests ? (
            <ActivityIndicator color={colors.primary} />
          ) : joinRequests.length === 0 ? (
            <Text style={styles.sectionHint}>대기 중인 가입 요청이 없습니다.</Text>
          ) : (
            joinRequests.map((request) => (
              <View key={request.id} style={styles.joinRequestRow}>
                <View style={styles.joinRequestInfo}>
                  <Text style={styles.linkLabel}>{request.userNickname}</Text>
                  <Text style={styles.sectionHint}>
                    {formatMemberRole(request.role)}
                    {request.canEdit ? ' · 일정 수정 가능' : ' · 일정 조회만'}
                  </Text>
                </View>
                <View style={styles.joinRequestActions}>
                  <Pressable
                    style={styles.approveButton}
                    onPress={() => handleReviewJoinRequest(request.id, 'approve')}
                    disabled={reviewingRequestId === request.id}
                  >
                    <Text style={styles.approveButtonText}>승인</Text>
                  </Pressable>
                  <Pressable
                    style={styles.rejectButton}
                    onPress={() => handleReviewJoinRequest(request.id, 'reject')}
                    disabled={reviewingRequestId === request.id}
                  >
                    <Text style={styles.rejectButtonText}>거절</Text>
                  </Pressable>
                </View>
              </View>
            ))
          )}
        </View>
      ) : null}

      <Pressable style={styles.menuItem} onPress={() => navigation.navigate('ChildSetup')}>
        <Text style={styles.menuTitle}>아이 등록</Text>
        <Text style={styles.menuMeta}>아이 이름과 출생 연도를 등록합니다.</Text>
      </Pressable>

      <Pressable style={styles.logoutButton} onPress={logout}>
        <Text style={styles.logoutButtonText}>로그아웃</Text>
      </Pressable>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    padding: spacing.md,
    gap: spacing.md,
  },
  profileCard: {
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    padding: spacing.md,
    gap: spacing.sm,
  },
  profileHeader: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: spacing.xs,
    flexWrap: 'wrap',
  },
  profileName: {
    ...typography.userName,
    color: colors.text,
  },
  roleBadge: {
    backgroundColor: colors.surface,
    borderRadius: radius.sm,
    paddingHorizontal: spacing.xs,
    paddingVertical: 2,
  },
  roleBadgeText: {
    ...typography.badge,
    color: colors.textSecondary,
  },
  profileHint: {
    ...typography.bodySmall,
    color: colors.textTertiary,
  },
  section: {
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    padding: spacing.md,
    gap: spacing.sm,
  },
  sectionLabel: {
    ...typography.body,
    fontWeight: '600',
    color: colors.text,
  },
  sectionHint: {
    ...typography.bodySmall,
    color: colors.textTertiary,
  },
  linkRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: spacing.xs,
  },
  linkLabel: {
    ...typography.body,
    color: colors.text,
  },
  linkStatus: {
    ...typography.bodySmall,
    color: colors.textSecondary,
  },
  linkButton: {
    borderWidth: 1,
    borderColor: colors.primary,
    borderRadius: radius.sm,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    minWidth: 72,
    alignItems: 'center',
  },
  linkButtonDisabled: {
    opacity: 0.7,
  },
  linkButtonText: {
    ...typography.bodySmall,
    fontWeight: '600',
    color: colors.primary,
  },
  input: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: radius.sm,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    ...typography.body,
    color: colors.text,
    backgroundColor: colors.background,
  },
  saveButton: {
    backgroundColor: colors.primary,
    borderRadius: radius.sm,
    paddingVertical: spacing.sm,
    alignItems: 'center',
  },
  saveButtonDisabled: {
    opacity: 0.7,
  },
  saveButtonText: {
    ...typography.body,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  error: {
    ...typography.bodySmall,
    color: '#DC2626',
  },
  menuItem: {
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    padding: spacing.md,
    gap: spacing.xs,
  },
  menuTitle: {
    ...typography.body,
    fontWeight: '600',
    color: colors.text,
  },
  menuMeta: {
    ...typography.bodySmall,
    color: colors.textTertiary,
  },
  logoutButton: {
    alignSelf: 'flex-start',
    backgroundColor: colors.surface,
    borderRadius: radius.sm,
    paddingVertical: spacing.sm,
    paddingHorizontal: spacing.md,
  },
  logoutButtonText: {
    ...typography.body,
    color: colors.textSecondary,
  },
  inviteCodeBox: {
    backgroundColor: colors.background,
    borderRadius: radius.sm,
    padding: spacing.sm,
    gap: spacing.xs,
  },
  inviteCodeLabel: {
    ...typography.bodySmall,
    color: colors.textTertiary,
  },
  inviteCodeValue: {
    ...typography.title,
    letterSpacing: 4,
    color: colors.text,
  },
  joinRequestTitle: {
    marginTop: spacing.sm,
  },
  joinRequestRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: spacing.sm,
    paddingVertical: spacing.xs,
  },
  joinRequestInfo: {
    flex: 1,
    gap: 2,
  },
  joinRequestActions: {
    flexDirection: 'row',
    gap: spacing.xs,
  },
  approveButton: {
    backgroundColor: colors.primary,
    borderRadius: radius.sm,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
  },
  approveButtonText: {
    ...typography.bodySmall,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  rejectButton: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: radius.sm,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
  },
  rejectButtonText: {
    ...typography.bodySmall,
    color: colors.textSecondary,
  },
});
