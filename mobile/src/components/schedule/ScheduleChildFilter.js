import { useCallback, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { colors, spacing } from '../../theme';

const TAB_ACTIVE = colors.text;

export function ScheduleChildFilter({ familyChildren, selectedChildId, onSelect }) {
  const tabs = [{ id: null, nickname: '전체' }, ...familyChildren];
  const [tabWidths, setTabWidths] = useState({});

  const handleTabLayout = useCallback((key, event) => {
    const { width } = event.nativeEvent.layout;
    setTabWidths((prev) => {
      if (prev[key] === width) {
        return prev;
      }
      return { ...prev, [key]: width };
    });
  }, []);

  return (
    <View style={styles.wrapper}>
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.scrollContent}
      >
        <View style={styles.tabsBlock}>
          <View style={styles.labelRow}>
            {tabs.map((child) => {
              const key = child.id ?? 'all';
              const active = selectedChildId === child.id;

              return (
                <Pressable
                  key={key}
                  style={[styles.tab, tabWidths[key] ? { width: tabWidths[key] } : null]}
                  onPress={() => onSelect(child.id ?? null)}
                  onLayout={(event) => handleTabLayout(key, event)}
                >
                  <Text style={[styles.tabLabel, active && styles.tabLabelActive]}>{child.nickname}</Text>
                </Pressable>
              );
            })}
          </View>
        </View>
      </ScrollView>

      <View style={styles.track}>
        <View style={styles.trackInner}>
          {tabs.map((child) => {
            const key = child.id ?? 'all';
            const active = selectedChildId === child.id;
            const width = tabWidths[key];

            return (
              <View
                key={`${key}-track`}
                style={[
                  styles.trackSegment,
                  width ? { width } : null,
                  active ? styles.trackSegmentActive : null,
                ]}
              />
            );
          })}
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    marginHorizontal: -spacing.md,
    gap: 12,
  },
  scrollContent: {
    paddingHorizontal: spacing.md,
  },
  tabsBlock: {
    gap: 12,
  },
  labelRow: {
    flexDirection: 'row',
    gap: 24,
  },
  tab: {
    alignItems: 'center',
  },
  tabLabel: {
    fontSize: 18,
    lineHeight: 24,
    fontWeight: '700',
    color: colors.textTertiary,
  },
  tabLabelActive: {
    color: TAB_ACTIVE,
  },
  track: {
    height: 4,
    backgroundColor: colors.surface,
  },
  trackInner: {
    flexDirection: 'row',
    gap: 24,
    paddingHorizontal: spacing.md,
  },
  trackSegment: {
    height: 4,
    minWidth: 24,
    backgroundColor: 'transparent',
  },
  trackSegmentActive: {
    backgroundColor: TAB_ACTIVE,
  },
});
