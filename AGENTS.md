# AGENTS.md — KnowSpends Engineering & Architecture Guidelines

## 1. Project Identity & Architecture
- **App**: KnowSpends (`com.knowspends.app`)
- **Philosophy**: Zero-network local-first personal ledger, strict financial privacy, high tactile utility.
- **Architecture Pattern**: Google Official Architecture (Now in Android pattern).
  - **Data Layer**: Room DB (`AppDatabase`) as Single Source of Truth (SSOT) + DataStore for user preferences. DAOs are internal; the UI layer only consumes Repositories.
  - **Domain Layer**: Clean, single-responsibility UseCases using coroutines (`flowOn(Dispatchers.IO)`).
  - **UI Layer**: ViewModels exposing a single, immutable `StateFlow<UiState>` using `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InitialState)`. Stateless Composable screens with hoisted callbacks.
  - **Dependency Injection**: Hilt (`@HiltViewModel`, `@AndroidEntryPoint`, `@InstallIn`).

## 2. Technology Stack & Verified Versions
- **Language**: Kotlin `2.0.21` (integrated Compose compiler plugin).
- **Build System**: Android Gradle Plugin `8.7.2`, Gradle `8.9+`, JDK `17`.
- **UI Framework**: Jetpack Compose BOM `2024.10.01` + Material 3 (`androidx.compose.material3`).
- **Shapes & Morphing**: `androidx.graphics.shapes` for algorithmic geometric morphing.
- **Image Loading**: Coil (`io.coil-kt:coil-compose:2.7.0`).
- **Local Database**: Room `2.6.1` with KSP.
- **Testing**: JUnit4, MockK, Turbine, Kotlinx Coroutines Test.
- **Environment**: Android on Linux / Termux.

## 3. Responsive Layout & Device Constraints (Samsung Galaxy S24 FE / One UI)
Target Device: **Samsung Galaxy S24 FE** (6.7" Dynamic AMOLED 2X, 1080 x 2340 px, ~385 ppi, Compact width class).
1. **Dynamic Typography (Samsung Max Font Size & Screen Zoom)**:
   - Always use `sp` for text dimensions and line heights.
   - Design and test for **up to 200% non-linear font scaling** (Android 14+ / One UI max font size).
   - **CRITICAL**: NEVER use rigid `Modifier.height(X.dp)` on containers enclosing text. Always use `Modifier.heightIn(min = X.dp)` or `Modifier.wrapContentHeight()` with appropriate vertical padding to prevent text truncation or ellipsis clipping.
2. **Adaptive Window Size Classes (`WindowWidthSizeClass`)**:
   - **Compact (<600dp)**: S24 FE portrait. Floating bottom pill navigation bar.
   - **Medium (600–840dp)**: S24 FE landscape / foldables unfolded. Navigation rail.
   - **Expanded (>840dp)**: Large tablets / desktop. Dual-pane List-Detail layout (`SupportingPaneScaffold` / `ListDetailPaneScaffold`).
3. **Semantic Shapes vs Hardcoded Values**:
   - Do NOT sprinkle ad-hoc `RoundedCornerShape(16.dp)` in screen composables.
   - Always consume semantic shape tokens from `MaterialTheme.shapes.*` (`extraSmall`, `small`, `medium`, `large`, `extraLarge`) or defined shape theme tokens to support consistent app-wide shape morphing.

## 4. Expressive UI & Motion (Vyxel + M3E Integration)
1. **Physics Springs First**:
   - Favor physics springs over static duration tweens for interactive elements:
     - `snappy`: damping 0.78f, stiffness 600f (chips, toggles, micro-interactions).
     - `expressive`: damping 0.72f, stiffness 480f (cards, bottom sheets, FAB morphs).
     - `bouncy`: damping 0.62f, stiffness 380f (dialog pops, milestone badges).
     - `smooth`: damping 0.85f, stiffness 260f (page transitions, parallax).
   - Reserve `tween()` strictly for non-interactive alpha fades.
2. **Tactile Spring Feedback**:
   - Interactive buttons and keypad keys must apply `Modifier.pressScale(0.955f)` on touch.
3. **Motion Accessibility**:
   - Honor `LocalMotionIntensity.current == 0f` and system `ANIMATOR_DURATION_SCALE = 0` to immediately disable spring physics for users with vestibular sensitivities.
4. **Atmospheric Surfaces**:
   - Use hardware `RenderEffect` backdrop blur (`Modifier.glassSurface`) on Android 12+ (API 31+).
   - Gracefully fall back to high-contrast opaque/translucent surface containers (`surfaceContainerHigh` with 88% alpha) on pre-Android 12 devices.
5. **Expressive Budget Feedback**:
   - Use `WavyLinearProgressIndicator` for budget health tracking (amplitude and frequency increase as spending approaches 100%).

## 5. Coding & Workflow Standards
- **Zero Network**: KnowSpends is 100% offline. Never introduce network libraries, telemetry, or remote analytics.
- **Preserve Documentation**: Retain all existing KDoc and inline architectural comments.
- **Verification Commands**:
  - Run Unit Tests: `./gradlew testDebugUnitTest`
  - Build Debug APK: `./gradlew assembleDebug`
  - Run Lint: `./gradlew lintDebug`
