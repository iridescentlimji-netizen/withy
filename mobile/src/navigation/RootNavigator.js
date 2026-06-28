import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { AcademyManagementScreen } from '../screens/AcademyManagementScreen';
import { ChildSetupScreen } from '../screens/ChildSetupScreen';
import { ScheduleCreateScreen } from '../screens/ScheduleCreateScreen';
import { ScheduleEditScreen } from '../screens/ScheduleEditScreen';
import { MainTabs } from './MainTabs';

const Stack = createNativeStackNavigator();

export function RootNavigator() {
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen name="MainTabs" component={MainTabs} options={{ headerShown: false }} />
        <Stack.Screen
          name="ScheduleCreate"
          component={ScheduleCreateScreen}
          options={{ title: '일정 등록', presentation: 'modal' }}
        />
        <Stack.Screen
          name="ScheduleEdit"
          component={ScheduleEditScreen}
          options={{ title: '일정 수정', presentation: 'modal' }}
        />
        <Stack.Screen name="ChildSetup" component={ChildSetupScreen} options={{ title: '아이 등록' }} />
        <Stack.Screen
          name="AcademyManagement"
          component={AcademyManagementScreen}
          options={{ title: '학원 관리' }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}
