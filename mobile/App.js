import { Component } from 'react';
import { StatusBar } from 'expo-status-bar';
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { AuthProvider, useAuth } from './src/context/AuthContext';
import { FamilyProvider, useFamily } from './src/context/FamilyContext';
import { RootNavigator } from './src/navigation/RootNavigator';
import { FamilySetupScreen } from './src/screens/FamilySetupScreen';
import { LoginScreen } from './src/screens/LoginScreen';
import { colors, spacing, typography } from './src/theme';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { error: null };
  }

  static getDerivedStateFromError(error) {
    return { error };
  }

  render() {
    if (this.state.error) {
      return (
        <View style={styles.errorContainer}>
          <Text style={styles.errorTitle}>앱 오류</Text>
          <Text style={styles.errorMessage}>{String(this.state.error?.message ?? this.state.error)}</Text>
        </View>
      );
    }
    return this.props.children;
  }
}

function AuthenticatedApp() {
  const { isLoading, needsSetup, hasLoadError, error, refreshFamilies } = useFamily();

  if (isLoading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  if (hasLoadError) {
    return (
      <View style={styles.loadingContainer}>
        <Text style={styles.errorMessage}>{error}</Text>
        <Pressable style={styles.retryButton} onPress={refreshFamilies}>
          <Text style={styles.retryButtonText}>다시 시도</Text>
        </Pressable>
      </View>
    );
  }

  if (needsSetup) {
    return <FamilySetupScreen />;
  }

  return <RootNavigator />;
}

function AppContent() {
  const { user, isAuthenticated, isRestoring } = useAuth();

  if (!isAuthenticated) {
    return (
      <View style={styles.container}>
        <LoginScreen />
        {isRestoring ? (
          <View style={styles.restoreOverlay}>
            <ActivityIndicator color={colors.primary} />
          </View>
        ) : null}
      </View>
    );
  }

  return (
    <FamilyProvider enabled userId={user?.id}>
      <AuthenticatedApp />
    </FamilyProvider>
  );
}

export default function App() {
  return (
    <ErrorBoundary>
      <SafeAreaProvider>
        <AuthProvider>
          <View style={styles.container}>
            <AppContent />
            <StatusBar style="dark" />
          </View>
        </AuthProvider>
      </SafeAreaProvider>
    </ErrorBoundary>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  loadingContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.background,
  },
  restoreOverlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(255,255,255,0.6)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  errorContainer: {
    flex: 1,
    backgroundColor: '#FEE2E2',
    padding: spacing.lg,
    justifyContent: 'center',
    gap: spacing.sm,
  },
  errorTitle: {
    ...typography.title,
    color: '#991B1B',
  },
  errorMessage: {
    ...typography.body,
    color: '#7F1D1D',
    textAlign: 'center',
    marginBottom: spacing.sm,
  },
  retryButton: {
    backgroundColor: colors.primary,
    borderRadius: 8,
    paddingVertical: spacing.sm,
    paddingHorizontal: spacing.md,
  },
  retryButtonText: {
    ...typography.body,
    fontWeight: '600',
    color: '#FFFFFF',
  },
});
