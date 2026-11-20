import { SafeAreaView } from 'react-native-safe-area-context';
import {
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import {
  AetherColors,
  AetherRadius,
  AetherSpacing,
} from '../constants/aether-theme';

const capabilities = [
  {
    number: '01',
    label: 'VOICE FIRST',
    title: 'Talk naturally.',
    description:
      'Speak when it is easier. Read, type and interact visually whenever that works better.',
    accent: '#E9EEFF',
    accentText: '#304FCB',
  },
  {
    number: '02',
    label: 'PERSONAL MEMORY',
    title: 'Remember what matters.',
    description:
      'Build useful continuity over time, with the ability to inspect, correct and remove memories.',
    accent: '#E4F4F0',
    accentText: '#25856C',
  },
  {
    number: '03',
    label: 'PROACTIVE INTELLIGENCE',
    title: 'Help at the right moment.',
    description:
      'Recognize relevant events and opportunities without overwhelming you with interruptions.',
    accent: '#F3EAFE',
    accentText: '#8051B8',
  },
  {
    number: '04',
    label: 'USER CONTROL',
    title: 'You stay in charge.',
    description:
      'Understand proposed actions and approve meaningful changes before they are carried out.',
    accent: '#FFF1E5',
    accentText: '#B66A2A',
  },
];

function BrandHeader() {
  return (
    <View style={styles.header}>
      <View style={styles.brandLockup}>
        <View style={styles.brandMark}>
          <Text style={styles.brandMarkText}>A</Text>
        </View>

        <View>
          <Text style={styles.brandName}>AETHER</Text>
          <Text style={styles.brandDescriptor}>
            PERSONAL INTELLIGENCE
          </Text>
        </View>
      </View>

      <Text style={styles.headerLabel}>DISCOVER</Text>
    </View>
  );
}

function CapabilityCard({
  number,
  label,
  title,
  description,
  accent,
  accentText,
}: (typeof capabilities)[number]) {
  return (
    <View style={styles.capabilityCard}>
      <View style={styles.capabilityTopRow}>
        <View
          style={[
            styles.capabilityNumber,
            { backgroundColor: accent },
          ]}
        >
          <Text
            style={[
              styles.capabilityNumberText,
              { color: accentText },
            ]}
          >
            {number}
          </Text>
        </View>

        <Text style={styles.capabilityLabel}>{label}</Text>
      </View>

      <Text style={styles.capabilityTitle}>{title}</Text>

      <Text style={styles.capabilityDescription}>
        {description}
      </Text>

      <View style={styles.plannedRow}>
        <View style={styles.plannedDot} />
        <Text style={styles.plannedText}>PLANNED CAPABILITY</Text>
      </View>
    </View>
  );
}

export default function ExploreScreen() {
  return (
    <SafeAreaView style={styles.safeArea} edges={['top']}>
      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.content}
      >
        <BrandHeader />

        <View style={styles.introduction}>
          <Text style={styles.eyebrow}>
            A DIFFERENT KIND OF COMPANION
          </Text>

          <Text style={styles.heading}>
            Intelligence that{'\n'}
            understands you.
          </Text>

          <Text style={styles.description}>
            AETHER brings conversation, context, personal memory and
            assistance together into one coherent experience.
          </Text>
        </View>

        <View style={styles.directionNotice}>
          <View style={styles.noticeMark}>
            <Text style={styles.noticeMarkText}>i</Text>
          </View>

          <View style={styles.noticeContent}>
            <Text style={styles.noticeTitle}>
              The direction we're building towards
            </Text>

            <Text style={styles.noticeDescription}>
              These are planned product capabilities. They'll be
              introduced in tested stages, not presented as features
              that are already available.
            </Text>
          </View>
        </View>

        <View style={styles.capabilitiesHeader}>
          <Text style={styles.eyebrow}>FOUR GUIDING IDEAS</Text>

          <Text style={styles.sectionTitle}>
            Designed around you
          </Text>

          <Text style={styles.sectionDescription}>
            Each capability is guided by privacy, usefulness and trust.
          </Text>
        </View>

        <View style={styles.capabilitiesList}>
          {capabilities.map((capability) => (
            <CapabilityCard
              key={capability.number}
              {...capability}
            />
          ))}
        </View>

        <View style={styles.closingCard}>
          <View style={styles.closingAccent} />

          <Text style={styles.closingEyebrow}>
            THE AETHER PROMISE
          </Text>

          <Text style={styles.closingTitle}>
            More capable.{'\n'}
            Never less yours.
          </Text>

          <Text style={styles.closingDescription}>
            Technology should make your life feel more yours, not less.
            That principle guides the decisions behind AETHER.
          </Text>
        </View>

        <Text style={styles.footer}>
          AETHER · PERSONAL INTELLIGENCE
        </Text>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: AetherColors.background,
  },

  content: {
    paddingHorizontal: 22,
    paddingTop: AetherSpacing.lg,
    paddingBottom: 30,
  },

  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },

  brandLockup: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 11,
  },

  brandMark: {
    width: 42,
    height: 42,
    borderRadius: 13,
    backgroundColor: AetherColors.navy,
    alignItems: 'center',
    justifyContent: 'center',
  },

  brandMarkText: {
    color: AetherColors.surface,
    fontSize: 25,
    fontWeight: '700',
    lineHeight: 30,
  },

  brandName: {
    color: AetherColors.text,
    fontSize: 16,
    fontWeight: '800',
    letterSpacing: 1.9,
  },

  brandDescriptor: {
    color: AetherColors.textMuted,
    fontSize: 8,
    fontWeight: '700',
    letterSpacing: 1,
    marginTop: 3,
  },

  headerLabel: {
    color: AetherColors.textMuted,
    fontSize: 9,
    fontWeight: '800',
    letterSpacing: 1.5,
  },

  introduction: {
    marginTop: 43,
    marginBottom: 25,
  },

  eyebrow: {
    color: AetherColors.primaryDark,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1.6,
  },

  heading: {
    color: AetherColors.text,
    fontSize: 37,
    lineHeight: 44,
    fontWeight: '700',
    letterSpacing: -1.4,
    marginTop: 13,
  },

  description: {
    color: AetherColors.textSecondary,
    fontSize: 14,
    lineHeight: 23,
    marginTop: 16,
  },

  directionNotice: {
    flexDirection: 'row',
    gap: 12,
    padding: 16,
    borderRadius: AetherRadius.medium,
    backgroundColor: '#EAF0FF',
    borderWidth: 1,
    borderColor: '#DCE5FF',
  },

  noticeMark: {
    width: 26,
    height: 26,
    borderRadius: 13,
    backgroundColor: '#D8E2FF',
    alignItems: 'center',
    justifyContent: 'center',
  },

  noticeMarkText: {
    color: AetherColors.primaryDark,
    fontSize: 14,
    fontWeight: '800',
  },

  noticeContent: {
    flex: 1,
  },

  noticeTitle: {
    color: AetherColors.text,
    fontSize: 12,
    fontWeight: '700',
  },

  noticeDescription: {
    color: AetherColors.textSecondary,
    fontSize: 11,
    lineHeight: 17,
    marginTop: 5,
  },

  capabilitiesHeader: {
    marginTop: 35,
    marginBottom: 18,
  },

  sectionTitle: {
    color: AetherColors.text,
    fontSize: 23,
    lineHeight: 29,
    fontWeight: '700',
    letterSpacing: -0.6,
    marginTop: 8,
  },

  sectionDescription: {
    color: AetherColors.textSecondary,
    fontSize: 12,
    lineHeight: 18,
    marginTop: 5,
  },

  capabilitiesList: {
    gap: 13,
  },

  capabilityCard: {
    backgroundColor: AetherColors.surface,
    borderRadius: AetherRadius.large,
    borderWidth: 1,
    borderColor: AetherColors.border,
    padding: 19,
  },

  capabilityTopRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },

  capabilityNumber: {
    width: 36,
    height: 36,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },

  capabilityNumberText: {
    fontSize: 11,
    fontWeight: '800',
  },

  capabilityLabel: {
    color: AetherColors.textMuted,
    fontSize: 9,
    fontWeight: '800',
    letterSpacing: 1.05,
  },

  capabilityTitle: {
    color: AetherColors.text,
    fontSize: 19,
    fontWeight: '700',
    letterSpacing: -0.4,
    marginTop: 17,
  },

  capabilityDescription: {
    color: AetherColors.textSecondary,
    fontSize: 13,
    lineHeight: 20,
    marginTop: 7,
  },

  plannedRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 7,
    marginTop: 17,
  },

  plannedDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: AetherColors.primary,
  },

  plannedText: {
    color: AetherColors.textMuted,
    fontSize: 8,
    fontWeight: '800',
    letterSpacing: 1.1,
  },

  closingCard: {
    backgroundColor: AetherColors.navy,
    padding: 22,
    borderRadius: AetherRadius.extraLarge,
    marginTop: 26,
    overflow: 'hidden',
  },

  closingAccent: {
    width: 34,
    height: 3,
    borderRadius: 2,
    backgroundColor: AetherColors.accent,
    marginBottom: 20,
  },

  closingEyebrow: {
    color: AetherColors.accent,
    fontSize: 9,
    fontWeight: '800',
    letterSpacing: 1.5,
  },

  closingTitle: {
    color: AetherColors.surface,
    fontSize: 28,
    lineHeight: 34,
    fontWeight: '700',
    letterSpacing: -0.8,
    marginTop: 16,
  },

  closingDescription: {
    color: '#C4CFE3',
    fontSize: 13,
    lineHeight: 21,
    marginTop: 12,
  },

  footer: {
    color: AetherColors.textMuted,
    fontSize: 9,
    textAlign: 'center',
    marginTop: 24,
    letterSpacing: 1.2,
    fontWeight: '700',
  },
});