# AETHER Mobile

AETHER's shared React Native + TypeScript mobile application.

## Platform priority

- Android: Initial implementation and release priority.
- iOS: Planned after Android is stable and rolled out.

## Technology

- React Native 0.88.1
- Expo SDK 58
- React 19.3.0
- Expo Router
- TypeScript 6
- pnpm workspace

## Development

From the repository root:

    pnpm install

    pnpm --filter mobile exec tsc --noEmit

    pnpm --filter mobile start

To start Expo with a clean Metro cache:

    pnpm --filter mobile exec expo start --go --clear

## Current application

The initial application provides:

- AETHER Home screen.
- Discover screen.
- Bottom-tab navigation.
- Shared design tokens.

Authentication and backend API integration are planned for AETHER-003B.

## Architecture

See:

- docs/architecture/MOBILE_APPLICATION.md
- docs/PRODUCT_PRINCIPLES.md
- docs/ROADMAP.md

## Native integrations

Android-specific integrations will use Kotlin where necessary.

Future iOS-specific integrations will use Swift.

Prefer shared React Native functionality unless a platform-specific
capability requires native implementation.