import { Platform, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { AcademyManagementScreen } from '../screens/AcademyManagementScreen';
import { HomeScreen } from '../screens/HomeScreen';
import { MyScreen } from '../screens/MyScreen';
import { ScheduleScreen } from '../screens/ScheduleScreen';
import { colors, spacing, typography } from '../theme';

const Tab = createBottomTabNavigator();

const TAB_ICONS = {
  Home: { active: 'home', inactive: 'home-outline' },
  Schedule: { active: 'calendar', inactive: 'calendar-outline' },
  Academy: { active: 'list', inactive: 'list-outline' },
  My: { active: 'person', inactive: 'person-outline' },
};

export function MainTabs() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.tabInactive,
        tabBarStyle: styles.tabBar,
        tabBarLabelStyle: styles.tabBarLabel,
        tabBarItemStyle: styles.tabBarItem,
        tabBarIcon: ({ color, size, focused }) => {
          const icons = TAB_ICONS[route.name];
          return (
            <Ionicons
              name={focused ? icons.active : icons.inactive}
              size={size ?? 24}
              color={color}
            />
          );
        },
      })}
    >
      <Tab.Screen name="Home" component={HomeScreen} options={{ title: '홈' }} />
      <Tab.Screen name="Schedule" component={ScheduleScreen} options={{ title: '일정' }} />
      <Tab.Screen
        name="Academy"
        component={AcademyManagementScreen}
        options={{ title: '학원관리' }}
      />
      <Tab.Screen name="My" component={MyScreen} options={{ title: '마이' }} />
    </Tab.Navigator>
  );
}

const styles = StyleSheet.create({
  tabBar: {
    backgroundColor: colors.surface,
    borderTopWidth: 0,
    height: Platform.OS === 'ios' ? 88 : 72,
    paddingTop: spacing.sm,
    paddingBottom: Platform.OS === 'ios' ? spacing.lg : spacing.sm,
    paddingHorizontal: spacing.md,
    ...Platform.select({
      ios: {
        shadowColor: '#5E5E5E',
        shadowOffset: { width: 0, height: 0 },
        shadowOpacity: 0.25,
        shadowRadius: 10,
      },
      android: {
        elevation: 12,
      },
    }),
  },
  tabBarLabel: {
    ...typography.caption,
    marginTop: spacing.xs,
  },
  tabBarItem: {
    borderRadius: 55,
    paddingVertical: spacing.xs,
  },
});
