# Compose Design & Quality Rules for KnowSpends

- **Grid Alignment**: All spacing, padding, margins, and spacers MUST strictly adhere to the 4dp grid (4dp, 8dp, 12dp, 16dp, 20dp, 24dp, 32dp, 48dp, 64dp).
- **Text Container Heights**: NEVER place fixed `Modifier.height(X.dp)` on containers enclosing text or labels (prevent text clipping at up to 200% dynamic font scaling). Use `Modifier.heightIn(min = X.dp)` or `Modifier.wrapContentHeight()`.
- **Typography Tokens**: Always reference `MaterialTheme.typography.*`. Never hardcode font sizes in composable functions.
- **Color Tokens**: Consume colors from `MaterialTheme.colorScheme` or semantic constants in `Color.kt`. Never write raw inline hex literals like `Color(0xFF...)` in UI screens.
- **Shape Tokens**: Use `VyxelShapeTokens` semantic tokens (`Pill`, `CardDefault`, `SheetHeader`, etc.). Do not use ad-hoc `RoundedCornerShape(X.dp)` or raw `CircleShape` for buttons/chips.
- **Accessibility**: All clickable elements must declare accessibility semantics (`role = Role.Button`, meaningful `contentDescription`). Never remove focus rings without providing accessible visual state.
- **Motion Gating**: Always gate spring physics and infinite animations behind `LocalMotionIntensity.current > 0f` to respect user reduce-motion preferences.
- **Unidirectional Data Flow**: ViewModels expose a single immutable `StateFlow<UiState>`. Screen composables must be stateless and hoist events.
