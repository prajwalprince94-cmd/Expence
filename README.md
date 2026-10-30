# KnowSpends 💰

A privacy-first, local-only native Android expense tracker built with modern Android development standards.

---

## 🚀 Key Highlights
- **100% Local-First & Zero Internet**: No external servers, no cloud accounts, and zero `INTERNET` permission in the manifest. Your financial data stays strictly on your device.
- **Precision Money Math**: Stored purely in minor units (`Long` paise) with standard Indian numbering format (`₹12,34,567.00`). Completely eliminates floating-point rounding errors.
- **Under 5-Second Logging**: Custom numeric keypad for instant 3-tap transaction entry.
- **Smart Review Inbox**: Bank SMS and UPI notification parser (HDFC, SBI, ICICI, Google Pay, PhonePe, Paytm) with 1-tap ledger approval and strict rejection of OTPs and spam.
- **Biometric Security & Anti-Snooping**: Biometric lock (fingerprint/face/device PIN), auto-lock timeouts, and `FLAG_SECURE` screen masking in the recent-apps switcher.
- **Widgets & Shortcuts**: Jetpack Glance Quick-Add Widget and launcher app shortcuts for instant logging directly from your home screen.
- **Data Sovereignty**: Versioned JSON backups and RFC 4180 CSV statements via Android Storage Access Framework (SAF), requiring zero storage permissions.
- **Target SDK**: Android 16 (API 36) with Min SDK 26 (Android 8.0 Oreo).

---

## 📁 Architecture Overview
KnowSpends is organized under clean architecture principles:
- **`app/data/`**: Room SQLite database, DAOs, entities, seed data, DataStore preferences, and repositories.
- **`app/domain/`**: Pure Kotlin models, currency formatters ([`MoneyFormatter`](file:///data/user/0/com.termux/files/home/ppp/app/src/main/java/com/knowspends/app/domain/util/MoneyFormatter.kt)), notification parsers ([`BankMessageParser`](file:///data/user/0/com.termux/files/home/ppp/app/src/main/java/com/knowspends/app/domain/util/BankMessageParser.kt)), use cases.
- **`app/ui/`**: Material 3 Compose screens ([Home](file:///data/user/0/com.termux/files/home/ppp/app/src/main/java/com/knowspends/app/ui/home/HomeScreen.kt), [Transactions](file:///data/user/0/com.termux/files/home/ppp/app/src/main/java/com/knowspends/app/ui/transactions/TransactionsScreen.kt), [Add](file:///data/user/0/com.termux/files/home/ppp/app/src/main/java/com/knowspends/app/ui/add/AddTransactionScreen.kt), [Budgets](file:///data/user/0/com.termux/files/home/ppp/app/src/main/java/com/knowspends/app/ui/budgets/BudgetsScreen.kt), [Reports](file:///data/user/0/com.termux/files/home/ppp/app/src/main/java/com/knowspends/app/ui/reports/ReportsScreen.kt), [Settings](file:///data/user/0/com.termux/files/home/ppp/app/src/main/java/com/knowspends/app/ui/settings/SettingsScreen.kt), [Review Inbox](file:///data/user/0/com.termux/files/home/ppp/app/src/main/java/com/knowspends/app/ui/inbox/ReviewInboxScreen.kt)), Navigation graph, and Glance widgets.
- **`app/di/`**: Hilt modules for Database, Repositories, and Coroutine Dispatchers.
- **`app/work/`**: WorkManager workers for scheduled and recurring transactions.

---

## 🛠️ Building & CI
The project uses Gradle with Version Catalogs (`libs.versions.toml`) and is continuously built and tested via GitHub Actions ([`.github/workflows/ci.yml`](file:///data/user/0/com.termux/files/home/ppp/.github/workflows/ci.yml)).

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run Android Lint
./gradlew lintDebug

# Build Debug APK
./gradlew assembleDebug

# Build Signed Release Bundle (.aab)
./gradlew bundleRelease
```

For complete Google Play release instructions, 12-tester verification, and Data Safety guidance, refer to the [RELEASE_CHECKLIST.md](file:///data/user/0/com.termux/files/home/ppp/RELEASE_CHECKLIST.md).
