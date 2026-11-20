# AETHER — Mobile Application Architecture

Version: 1.0
Status: Initial Android application foundation

## 1. Overview

AETHER uses a shared React Native + TypeScript mobile application.

Android is the initial implementation and release priority.
iOS implementation follows a stable Android rollout.

The mobile application communicates with the AETHER Spring Boot API.
It is responsible for presentation, user interaction and device-specific
capabilities. Backend business rules, identity and authorization remain
owned by the API.

## 2. Technology stack

- React Native 0.88.1
- React 19.3.0
- Expo SDK 58
- Expo Router
- TypeScript 6
- pnpm workspace
- Kotlin for future Android-native integrations
- Swift for future iOS-native integrations

The application currently uses Expo Go for development and emulator
validation.

A standalone Android development build and production build are
separate validation activities.

## 3. Repository structure

apps/mobile/
app.json
package.json
tsconfig.json
css.d.ts
src/
app/
_layout.tsx
index.tsx
explore.tsx
constants/
aether-theme.ts
components/
hooks/

The structure will evolve as complete application capabilities
are introduced.

## 4. Navigation

Expo Router provides file-based navigation.

Initial routes:

| Route | Screen | Purpose |
|---|---|---|
| / | Home | AETHER introduction and product principles |
| /explore | Discover | Product vision and planned capabilities |

The application uses a bottom-tab navigation structure.

Further navigation groups will be introduced when authentication
and the authenticated application experience are implemented.

## 5. Design system

Shared visual tokens are defined in:

src/constants/aether-theme.ts

The initial design system includes:

- Primary and secondary colors.
- Background and surface colors.
- Text colors.
- Border colors.
- Spacing scale.
- Border-radius scale.

Current visual direction:

- Deep navy as the primary brand foundation.
- Restrained blue as the interactive accent.
- Soft neutral backgrounds.
- Clear typography and readable content hierarchy.
- Rounded cards and controlled decorative elements.

Screens should consume shared theme tokens rather than introduce
unrelated colors and spacing values.

## 6. Current implementation

AETHER-003A establishes:

- Expo mobile application configuration.
- Android-first application identity.
- React Native and TypeScript foundation.
- pnpm workspace integration.
- Expo Router navigation.
- Home screen.
- Discover screen.
- Shared design tokens.
- Initial visual application identity.

The Home screen presents the AETHER product vision and guiding
principles.

The Discover screen presents planned capabilities:

- Voice-first interaction.
- Personal memory.
- Proactive intelligence.
- User control.

Planned capabilities are explicitly identified as planned.
The UI does not imply that these capabilities are already operational.

## 7. API integration boundary

The mobile application will communicate with the backend using
versioned REST APIs.

The backend remains the authority for:

- User identity.
- Account lifecycle.
- Authentication.
- Session validation.
- Authorization.
- Personal data access.

The mobile application must not independently implement backend
security rules.

API communication will be centralized behind a shared API client.

Base URLs must be environment-specific.

Android emulator networking must account for the difference between
the emulator's localhost and the host development machine.

## 8. Authentication and secure storage

Not yet implemented in AETHER-003A.

The upcoming authentication slice will introduce:

- Registration.
- Email verification.
- Login.
- Current-user retrieval.
- Access-token handling.
- Refresh-token rotation.
- Logout.
- Expired-session handling.

Sensitive credentials must use platform-backed secure storage.

Ordinary application preferences or AsyncStorage must not be used
for raw access or refresh credentials.

Refresh operations must be serialized because the backend uses
single-use rotating refresh tokens.

Android will use secure platform storage.
iOS will use the corresponding Apple secure storage mechanism
when iOS implementation begins.

## 9. Native integration strategy

Use React Native and Expo capabilities for shared functionality.

Introduce Kotlin modules only when Android-specific behavior
requires native implementation.

Future Android integration areas may include:

- Secure storage.
- Audio capture and playback.
- Notifications.
- Background processing.
- Device capabilities.

Avoid introducing native modules speculatively.

## 10. Privacy and security requirements

- Do not log credentials or authentication tokens.
- Do not expose personal content through diagnostics.
- Do not store secrets in source-controlled configuration.
- Keep API authorization authoritative on the backend.
- Respect account and session lifecycle.
- Keep important actions approval-based.
- Provide user control over personal memory when implemented.

## 11. Validation status

AETHER-003A has been visually exercised using an Android Pixel 8
emulator with Expo Go.

The Home and Discover screens have been rendered and inspected.

Final TypeScript and Git whitespace validation must pass before
the milestone is committed.

No production Android build or Play Store release has been completed.

No authentication or backend API integration is included yet.

## 12. Next implementation slice

AETHER-003B — User Registration and Authentication Experience.

Expected capabilities:

1. Registration screen.
2. Email verification experience.
3. Login screen.
4. API client foundation.
5. Backend integration.
6. Secure token storage.
7. Session restoration.
8. Refresh-token handling.
9. Logout.
10. Authenticated application shell.