---
name: compose-design-fix
description: >-
  Executes automated multi-pass design fixes on Jetpack Compose screens in KnowSpends,
  resolving layout, typography, token, a11y, and motion issues, followed by verification.
---

# Compose Design Fix Pipeline

Follow this 5-pass pipeline when resolving design findings in KnowSpends:

## Pipeline Phases

### Pass 1: Spacing, Grid & Layout Safety
- Fix rigid text containers: change `Modifier.height(X.dp)` to `Modifier.heightIn(min = X.dp)` or `Modifier.wrapContentHeight()`.
- Fix off-grid spacing values (e.g. 6dp -> 8dp, 14dp -> 16dp, 18dp -> 16/20dp, 130dp -> 128dp).
- Replace magic bottom spacers with dynamic `WindowInsets.navigationBars` clearance.

### Pass 2: Typography & Semantic Scale
- Migrate hardcoded `fontSize` values to standard tokens (`headlineSmall`, `labelSmall`, `headlineLarge`).
- Move ad-hoc `letterSpacing` overrides to `Type.kt`.
- Fix any `size.value.sp` misuse to use density-aware `with(LocalDensity.current) { size.toSp() }`.

### Pass 3: Colors & Shape Tokens
- Extract repeating raw hex colors (e.g., `0xFFF57C00` warning orange, `0xFFFFB300` star gold, chart palette) into `Color.kt`.
- Replace raw `CircleShape` and ad-hoc `RoundedCornerShape(...)` on buttons/chips with semantic `VyxelShapeTokens`.
- Ensure dark-theme lightness awareness on dynamic gradients.

### Pass 4: Motion & Accessibility Compliance
- Gate all `rememberInfiniteTransition` and spring animations behind `LocalMotionIntensity.current > 0f` (fallback to `snapTo(1f)` or static values).
- Add missing `contentDescription` or semantic `role = Role.Button` to clickable elements.
- Restore accessible focus indications where `indication = null` was applied.

### Pass 5: Architecture & Verification
- Hoist residual inline dialog/filter state into the respective ViewModel `UiState`.
- Run tests and lint:
  ```bash
  ./gradlew testDebugUnitTest
  ./gradlew lintDebug
  ```
