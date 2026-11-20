import { router } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import {
  Pressable,
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

const principles = [
  {
    number: '01',
    title: 'Remember what matters',
    description:
      'Personal memory that you can inspect, correct and control.',
  },
  {
    number: '02',
    title: 'Meet you in the moment',
    description:
      'Voice-first interaction, supported by a thoughtful visual experience.',
  },
  {
    number: '03',
    title: 'Keep you in control',
    description:
      'Understand and approve meaningful actions before they happen.',
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
    </View>
  );
}

function HeroCard() {
  return (
    <View style={styles.heroCard}>
      <View
        pointerEvents="none"
        style={styles.heroArtwork}
        accessibilityElementsHidden
        importantForAccessibility="no-hide-descendants"
      >
        <View style={styles.orbOuter} />
        <View style={styles.orbMiddle} />

        <View style={styles.orbCore}>
          <Text style={styles.orbLetter}>A</Text>
        </View>
      </View>

      <View style={styles.heroContent}>
        <Text style={styles.heroEyebrow}>THE AETHER EXPERIENCE</Text>

        <Text style={styles.heroTitle}>
          Intelligence{'\n'}
          that grows{'\n'}
          with you.
        </Text>

        <Text style={styles.heroDescription}>
          Thoughtful by design.
          {'\n'}
          Always in your control.
        </Text>
      </View>

      <View style={styles.heroFooter}>
        <View style={styles.heroFooterLine} />
        <Text style={styles.heroFooterText}>BUILT AROUND YOU</Text>
      </View>
    </View>
  );
}

export default function HomeScreen() {
  return (
    <SafeAreaView style={styles.safeArea} edges={['top']}>
      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.content}
      >
        <BrandHeader />

        <View style={styles.introduction}>
          <Text style={styles.eyebrow}>
            YOUR PERSONAL COMPANION
          </Text>

          <Text style={styles.heading}>
            A little more{'\n'}
            clarity. A lot{'\n'}
            more you.
          </Text>

          <Text style={styles.introDescription}>
            AETHER is being built to understand your world, remember
            what matters, and help you move through the day — with you
            in control.
          </Text>
        </View>

        <HeroCard />

        <View style={styles.sectionHeader}>
          <Text style={styles.eyebrow}>THE FOUNDATION</Text>
          <Text style={styles.sectionTitle}>
            What guides AETHER
          </Text>
          <Text style={styles.sectionDescription}>
            The principles behind every product decision.
          </Text>
        </View>

        <View style={styles.principlesContainer}>
          {principles.map((principle, index) => (
            <View
              key={principle.number}
              style={[
                styles.principleCard,
                index === principles.length - 1 &&
                  styles.lastPrincipleCard,
              ]}
            >
              <View style={styles.principleNumber}>
                <Text style={styles.principleNumberText}>
                  {principle.number}
                </Text>
              </View>

              <View style={styles.principleContent}>
                <Text style={styles.principleTitle}>
                  {principle.title}
                </Text>

                <Text style={styles.principleDescription}>
                  {principle.description}
                </Text>
              </View>
            </View>
          ))}
        </View>

        <Pressable
          accessibilityRole="button"
          accessibilityLabel="Explore the AETHER vision"
          onPress={() => router.navigate('/explore')}
          style={({ pressed }) => [
            styles.exploreButton,
            pressed && styles.buttonPressed,
          ]}
        >
          <Text style={styles.exploreButtonText}>
            Explore the AETHER vision
          </Text>

          <Text style={styles.exploreButtonArrow}>→</Text>
        </Pressable>

        <Text style={styles.footerText}>
          AETHER · A more personal kind of intelligence.
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
    width: 44,
    height: 44,
    borderRadius: 14,
    backgroundColor: AetherColors.navy,
    alignItems: 'center',
    justifyContent: 'center',
  },

  brandMarkText: {
    color: AetherColors.surface,
    fontSize: 27,
    fontWeight: '700',
    lineHeight: 32,
  },

  brandName: {
    color: AetherColors.text,
    fontSize: 17,
    fontWeight: '800',
    letterSpacing: 2.1,
  },

  brandDescriptor: {
    color: AetherColors.textMuted,
    fontSize: 8,
    fontWeight: '700',
    letterSpacing: 1.2,
    marginTop: 3,
  },

  introduction: {
    marginTop: 42,
    marginBottom: 27,
  },

  eyebrow: {
    color: AetherColors.primaryDark,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1.6,
  },

  heading: {
    color: AetherColors.text,
    fontSize: 40,
    lineHeight: 46,
    fontWeight: '700',
    letterSpacing: -1.6,
    marginTop: 13,
  },

  introDescription: {
    color: AetherColors.textSecondary,
    fontSize: 15,
    lineHeight: 24,
    marginTop: 16,
    maxWidth: 340,
  },

  heroCard: {
    minHeight: 278,
    borderRadius: AetherRadius.extraLarge,
    backgroundColor: AetherColors.navy,
    padding: 22,
    paddingBottom: 20,
    overflow: 'hidden',
    justifyContent: 'space-between',
  },

  heroArtwork: {
    position: 'absolute',
    top: 0,
    right: 0,
    bottom: 0,
    left: 0,
  },

  orbOuter: {
    position: 'absolute',
    width: 160,
    height: 160,
    borderRadius: 80,
    borderWidth: 1,
    borderColor: 'rgba(160, 184, 255, 0.25)',
    right: -38,
    top: 43,
  },

  orbMiddle: {
    position: 'absolute',
    width: 128,
    height: 128,
    borderRadius: 64,
    backgroundColor: AetherColors.navyLight,
    borderWidth: 1,
    borderColor: 'rgba(160, 184, 255, 0.3)',
    right: -22,
    top: 59,
  },

  orbCore: {
    position: 'absolute',
    width: 86,
    height: 86,
    borderRadius: 43,
    backgroundColor: AetherColors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    right: -1,
    top: 80,
    elevation: 8,
  },

  orbLetter: {
    color: AetherColors.surface,
    fontSize: 49,
    fontWeight: '500',
    lineHeight: 58,
  },

  heroContent: {
    width: '58%',
    zIndex: 2,
  },

  heroEyebrow: {
    color: AetherColors.accent,
    fontSize: 9,
    fontWeight: '800',
    letterSpacing: 1.35,
    marginBottom: 18,
  },

  heroTitle: {
    color: AetherColors.surface,
    fontSize: 27,
    lineHeight: 32,
    fontWeight: '700',
    letterSpacing: -0.8,
  },

  heroDescription: {
    color: '#C4CFE3',
    fontSize: 11,
    lineHeight: 18,
    marginTop: 13,
    maxWidth: 175,
  },

  heroFooter: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 9,
    marginTop: 20,
    zIndex: 2,
  },

  heroFooterLine: {
    width: 22,
    height: 2,
    borderRadius: 1,
    backgroundColor: AetherColors.accent,
  },

  heroFooterText: {
    color: '#AAB8D2',
    fontSize: 8,
    fontWeight: '800',
    letterSpacing: 1.5,
  },

  sectionHeader: {
    marginTop: 36,
    marginBottom: 18,
  },

  sectionTitle: {
    color: AetherColors.text,
    fontSize: 23,
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

  principlesContainer: {
    backgroundColor: AetherColors.surface,
    borderRadius: AetherRadius.large,
    paddingHorizontal: 17,
    paddingVertical: 4,
    borderWidth: 1,
    borderColor: AetherColors.border,
  },

  principleCard: {
    flexDirection: 'row',
    paddingVertical: 18,
    borderBottomWidth: 1,
    borderBottomColor: AetherColors.border,
    gap: 14,
  },

  lastPrincipleCard: {
    borderBottomWidth: 0,
  },

  principleNumber: {
    width: 36,
    height: 36,
    borderRadius: 12,
    backgroundColor: '#EEF1FF',
    alignItems: 'center',
    justifyContent: 'center',
  },

  principleNumberText: {
    color: AetherColors.primaryDark,
    fontSize: 11,
    fontWeight: '800',
  },

  principleContent: {
    flex: 1,
  },

  principleTitle: {
    color: AetherColors.text,
    fontSize: 14,
    fontWeight: '700',
  },

  principleDescription: {
    color: AetherColors.textSecondary,
    fontSize: 12,
    lineHeight: 19,
    marginTop: 5,
  },

  exploreButton: {
    minHeight: 56,
    marginTop: 24,
    borderRadius: AetherRadius.medium,
    backgroundColor: AetherColors.primary,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 12,
    paddingHorizontal: 18,
  },

  exploreButtonText: {
    color: AetherColors.surface,
    fontSize: 14,
    fontWeight: '700',
  },

  exploreButtonArrow: {
    color: AetherColors.surface,
    fontSize: 20,
    lineHeight: 23,
  },

  buttonPressed: {
    opacity: 0.82,
    transform: [{ scale: 0.99 }],
  },

  footerText: {
    color: AetherColors.textMuted,
    fontSize: 10,
    textAlign: 'center',
    marginTop: 22,
    letterSpacing: 0.3,
  },
});