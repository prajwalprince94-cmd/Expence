# KnowSpends — Google Play Release Checklist & Deployment Guide

This guide details the exact steps to publish **KnowSpends** (`com.knowspends.app`) to the Google Play Store, meeting all 2026/2027 developer policies, 12-tester requirements, and Data Safety standards.

---

## 1. Google Play Developer Account & 12-Tester Requirement

Under Google Play's policy for personal developer accounts:
- You must run a **Closed Testing** track with at least **12 testers** who have been opted-in continuously for at least **14 days**.
- Testers must actively install and launch the build.

### Closed Testing Action Steps:
1. **Create Closed Track in Play Console**:
   - Navigate to **Release > Testing > Closed testing**.
   - Create a track named `Alpha - 14 Day Verification`.
2. **Setup Tester List**:
   - Create an email list or link a Google Group (e.g. `knowspends-testers@googlegroups.com`).
   - Invite at least 15–20 trusted contacts to ensure at least 12 remain opted-in every day.
3. **Upload Release App Bundle**:
   - Upload the signed `.aab` generated via `./gradlew bundleRelease`.
4. **Daily Tester Engagement**:
   - Send instructions to testers to open the app, log transactions, and create backups.
   - Collect feedback through Play Console or Google Forms.
5. **Apply for Production**:
   - After day 14, complete the questionnaire in Play Console:
     - How did you recruit testers?
     - How did testers use the app?
     - What feedback did you receive and what changes did you make?
   - Submit for Google review to unlock the Production track.

---

## 2. Play Console Data Safety Declaration

KnowSpends is built **100% Local-First** with **Zero INTERNET Permission** in `AndroidManifest.xml`. This makes Data Safety approval frictionless.

| Section | Question | Answer | Rationale |
| :--- | :--- | :--- | :--- |
| **Data Collection** | Does your app collect or share any of the required user data types? | **No** | All database tables and preferences stay exclusively on device in SQLite Room. |
| **Financial Info** | Does your app collect financial info (purchase history, bank balances)? | **No** | Stored locally only; zero bytes leave the phone. |
| **Personal Info** | Does your app collect name, email, or IDs? | **No** | No account creation, no login required. |
| **Location / Photos** | Does your app access location or device storage? | **No** | Backups use Android Storage Access Framework (SAF); no storage permissions needed. |
| **Security Practices** | Is data encrypted in transit? | **N/A** | No data is transmitted over the network (Zero network calls). |
| **Data Deletion** | Do you provide a way for users to request data deletion? | **Yes** | Users have full control: uninstalling or clearing app data wipes all local SQLite records instantly. |

---

## 3. Production App Signing & Bundle Generation

### Step 3.1: Generate a Production Keystore (if not already created)
Run the following standard `keytool` command in a secure terminal:
```bash
keytool -genkeypair -v -keystore knowspends-release.jks \
  -alias knowspends -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=KnowSpends Developer, OU=Mobile, O=KnowSpends, L=Bangalore, ST=KA, C=IN"
```
*(Store `knowspends-release.jks` and passwords securely in your password manager. Never commit keystores to version control).*

### Step 3.2: Configure Gradle Release Signing
Set environment variables or configure `app/build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file(System.getenv("KEYSTORE_PATH") ?: "../knowspends-release.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS") ?: "knowspends"
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
```

### Step 3.3: Build Signed Android App Bundle (.aab)
```bash
./gradlew bundleRelease
```
The output will be located at:
`app/build/outputs/bundle/release/app-release.aab`

---

## 4. Technical Compliance & Verification

- **Target SDK**: Android 16 (API 36) — compliant with latest Play Console target requirements.
- **Min SDK**: Android 8.0 (API 26) — supports ~95%+ of active devices.
- **Architecture**:
  - Currency: `Long` paise minor units (paise; ₹12.50 = 1250L). No IEEE-754 floating point errors.
  - Indian digit grouping: Format `₹12,34,567.00` implemented directly.
  - Storage: Room SQLite 2.6 with auto-migrations and schema export.
  - ProGuard / R8: Fully configured in `app/proguard-rules.pro` keeping Room, Serialization, Glance, and WorkManager classes.
- **Accessibility**:
  - High-contrast color palette with Material 3 dynamic color fallback.
  - Dual-mode status indicators (Colour + Icon + Text) for overspend and budget alerts.
  - Minimum 48dp touch targets on custom numeric keypad and action buttons.
  - Compatible with TalkBack and 200% system font scale.

---

## 5. Store Listing Assets Checklist

Before submitting to Google Play:
- [ ] **App Name**: KnowSpends - Smart Expense Tracker
- [ ] **Short Description** (up to 80 chars): Private, offline expense tracker & smart budget manager. Zero ads, zero tracking.
- [ ] **Full Description** (up to 4000 chars): Emphasize privacy, 100% offline local storage, instant 3-tap expense logging, smart review inbox, and zero floating-point accuracy.
- [ ] **App Icon**: 512 x 512 px 32-bit PNG with alpha.
- [ ] **Feature Graphic**: 1024 x 500 px JPG or 24-bit PNG.
- [ ] **Screenshots**: At least 4 phone screenshots (minimum 1080p, 16:9 or 9:16 aspect ratio).
- [ ] **Privacy Policy URL**: Hosted on GitHub Pages or static site stating zero telemetry and 100% local-first data storage.
