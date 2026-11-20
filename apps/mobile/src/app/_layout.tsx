import { Tabs } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View } from 'react-native';

import { AetherColors } from '../constants/aether-theme';

type TabIconProps = {
  glyph: string;
  focused: boolean;
};

function TabIcon({ glyph, focused }: TabIconProps) {
  return (
    <View
      style={[
        styles.iconContainer,
        focused && styles.iconContainerFocused,
      ]}
    >
      <Text
        style={[
          styles.iconText,
          focused ? styles.iconTextFocused : styles.iconTextInactive,
        ]}
      >
        {glyph}
      </Text>
    </View>
  );
}

export default function RootLayout() {
  return (
    <>
      <StatusBar style="dark" />

      <Tabs
        screenOptions={{
          headerShown: false,
          sceneStyle: {
            backgroundColor: AetherColors.background,
          },
          tabBarActiveTintColor: AetherColors.primaryDark,
          tabBarInactiveTintColor: AetherColors.textMuted,
          tabBarStyle: styles.tabBar,
          tabBarLabelStyle: styles.tabLabel,
          tabBarItemStyle: styles.tabItem,
        }}
      >
        <Tabs.Screen
          name="index"
          options={{
            title: 'Home',
            tabBarAccessibilityLabel: 'Home',
            tabBarIcon: ({ focused }) => (
              <TabIcon glyph="⌂" focused={focused} />
            ),
          }}
        />

        <Tabs.Screen
          name="explore"
          options={{
            title: 'Discover',
            tabBarAccessibilityLabel: 'Discover AETHER',
            tabBarIcon: ({ focused }) => (
              <TabIcon glyph="✧" focused={focused} />
            ),
          }}
        />
      </Tabs>
    </>
  );
}

const styles = StyleSheet.create({
  tabBar: {
    height: 76,
    paddingTop: 8,
    paddingBottom: 8,
    backgroundColor: AetherColors.surface,
    borderTopColor: AetherColors.border,
    borderTopWidth: StyleSheet.hairlineWidth,
    elevation: 0,
  },

  tabItem: {
    paddingVertical: 2,
  },

  tabLabel: {
    fontSize: 11,
    fontWeight: '600',
    letterSpacing: 0.2,
    marginTop: 2,
  },

  iconContainer: {
    width: 34,
    height: 28,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },

  iconContainerFocused: {
    backgroundColor: '#E9EEFF',
  },

  iconText: {
    fontSize: 23,
    lineHeight: 27,
    fontWeight: '600',
  },

  iconTextFocused: {
    color: AetherColors.primaryDark,
  },

  iconTextInactive: {
    color: AetherColors.textMuted,
  },
});