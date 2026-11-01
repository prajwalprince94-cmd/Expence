---
name: compose-design-audit
description: >-
  Audits Jetpack Compose screens and components in KnowSpends against AGENTS.md,
  Material 3 Expressive standards, 4dp grid spacing, typography scales,
  motion accessibility, and semantic shape tokens.
---

# Compose Design Audit Skill

Use this skill whenever scanning or evaluating Compose UI screens in KnowSpends.

## Audit Checklist

1. **Spacing & Layout**:
   - Verify all padding, spacing, and spacer heights align to 4dp grid (4, 8, 12, 16, 20, 24, 32, 48, 64dp).
   - Ensure text containers NEVER use hardcoded `Modifier.height(X.dp)`. Use `Modifier.heightIn(min = X.dp)` or `Modifier.wrapContentHeight()`.
   - Ensure bottom navigation clearance accounts for system navigation bars dynamically.

2. **Typography**:
   - Ensure all text styles use `MaterialTheme.typography.*` or centralized tokens.
   - Disallow ad-hoc `fontSize = X.sp` on text elements (minimum readable label size is 11sp `labelSmall`).
   - Disallow `dp` for text sizing.

3. **Color & Surfaces**:
   - Ensure colors map to `MaterialTheme.colorScheme.*` or designated semantic constants in `Color.kt` (`ExpenseColor`, `IncomeColor`, `TransferColor`, `WarningColor`).
   - Avoid inline raw hexadecimal `Color(0xFF...)` literals inside composables.

4. **Shapes & Components**:
   - Ensure buttons, cards, and chips use semantic tokens from `VyxelShapeTokens` (`Pill`, `CardDefault`, `SheetHeader`, etc.) rather than ad-hoc `RoundedCornerShape` or raw `CircleShape`.

5. **Accessibility & Motion**:
   - Ensure every `Icon`, `Image`, or clickable control specifies meaningful `contentDescription` or semantics (`Role.Button`).
   - Never suppress ripple/focus with `indication = null` without custom focus ring feedback.
   - Gate all physics springs and infinite transitions behind `LocalMotionIntensity.current > 0f`.

6. **State & Architecture**:
   - Ensure screen composables are stateless with hoisted events.
   - Screen ViewModels must expose a single immutable `StateFlow<UiState>`.
