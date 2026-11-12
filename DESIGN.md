# DESIGN.md — KnowSpends UI & Design System Contract

> **This file is mandatory reading for every AI coding session on this project.**
> It is a living contract between the product direction and the agent.
> Every composable, screen, color, and animation decision must be justified against it.
>
> Sources synthesised into this document (all adapted for Jetpack Compose / Material 3):
> - [Leonxlnx/taste-skill](https://github.com/Leonxlnx/taste-skill) · [LeoStehlik/no-slop-ui](https://github.com/LeoStehlik/no-slop-ui)
> - [funboy322/avoid-ai-design](https://github.com/funboy322/avoid-ai-design) · [miqdadbadjuber/anti-slop](https://github.com/miqdadbadjuber/anti-slop)
> - [Netxeo/skill-file-security](https://github.com/Netxeo/skill-file-security) · [GrubbyLee/design-guide](https://github.com/GrubbyLee/design-guide)
> - [lotfb86/web-design-skills](https://github.com/lotfb86/web-design-skills) · [alchaincyf/huashu-design](https://github.com/alchaincyf/huashu-design)

---

## 1. Product Identity

**KnowSpends** is a privacy-first, local-only Android expense tracker. It is a *precision tool*, not a consumer lifestyle app.

The design reads as: **dense, utilitarian clarity**. Like a good pocket notebook or a physical ledger — paper-weight, monochromatic anchors, one accent. The UI should feel like something a thoughtful engineer designed for themselves. It should not look like a fintech startup's investor demo.

### Design Read (lock this before writing any composable)
- **Palette archetype**: Near-black surface with one warm ink accent. Financial green only for positive deltas. Financial red only for negative deltas. No gradients.
- **Type character**: Monospaced or tabular-figures body for numbers. Clean geometric sans for labels. No decorative serifs.
- **Motion character**: Instantaneous or sub-150ms transitions for data operations. Purposeful reveal only on first-load states. Zero bounce easing.
- **Spatial character**: Tight, information-dense. Breathing room comes from white space *between groups*, not padding within cards. Cards are not the primary layout unit — lists and rows are.

---

## 2. Color System

```kotlin
// KnowSpends Design Tokens — MaterialTheme.colorScheme overrides
object KsColor {
    // Surface
    val Background   = Color(0xFF0E0E10)  // near-black, not pure black
    val Surface      = Color(0xFF161618)
    val SurfaceEl    = Color(0xFF1E1E21)  // elevated card surfaces
    val Outline      = Color(0xFF2C2C30)  // dividers, borders

    // Content
    val OnBackground = Color(0xFFF0EFE9)  // warm off-white — primary text
    val OnSurface    = Color(0xFFCCCBC5)  // secondary text
    val OnMuted      = Color(0xFF6B6B70)  // tertiary / disabled

    // Accent — ONE accent, used sparingly
    val Accent       = Color(0xFFD4A853)  // warm amber-gold (ink, not neon)
    val AccentMuted  = Color(0xFF3A2E18)  // accent container / tinted bg

    // Semantic — financial only, never decorative
    val Positive     = Color(0xFF4CAF72)  // income / positive delta
    val Negative     = Color(0xFFE05252)  // expense / negative delta
    val Neutral      = Color(0xFF8A8A8F)  // zero / unchanged

    // Never use
    // - gradient fills
    // - purple / indigo / sky-blue tints
    // - glassmorphism (blur behind translucent surfaces)
    // - Material You dynamic color (disable completely — it breaks the contract)
}
```

**Rules:**
- Dynamic color (`dynamicColor = true`) is **disabled**. KnowSpends has a hand-specified palette.
- Never use `Color.White` or `Color.Black` directly — use the token aliases above.
- The accent (`Accent`) appears in at most **one place per screen**. Usually: the primary CTA, the active nav item, or one data highlight.
- Background and surface are near-identical — the difference is felt in depth, not seen as stark contrast.

---

## 3. Typography

```kotlin
object KsType {
    // Display: used for the big balance figure only
    val BalanceDisplay = TextStyle(
        fontFamily = FontFamily.Monospace,  // tabular figures, no kerning drift
        fontWeight = FontWeight.Medium,
        fontSize = 36.sp,
        letterSpacing = (-0.5).sp,
    )

    // Headlines: screen titles, section labels
    val HeadlineLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp)
    val HeadlineMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp)

    // Body: transaction rows, descriptions
    val BodyLarge  = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.15.sp)
    val BodyMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.1.sp)

    // Amounts: always monospace, tabular figures
    val AmountLarge  = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    val AmountMedium = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Normal)

    // Labels: category chips, timestamps, metadata
    val Label = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.4.sp)
}
```

**Rules:**
- ALL currency/amount text uses `FontFamily.Monospace`. This ensures tabular digit alignment in lists.
- Never use `sp` sizes below 11.sp for any visible text.
- Never use `letterSpacing > 1.sp` — it reads as decorative AI output.
- Use `fontFeatureSettings = "tnum"` on amount Text composables for tabular figures.
- Section headers are sentence case, not ALL CAPS.
- Never put decorative 01 / 02 / 03 numbering on section headers.

---

## 4. Spacing & Layout

**Primary rhythm unit: 4dp**. All spacing values must be multiples of 4.

```
Micro  :  4dp  — icon-to-label gap, tight list row padding
Small  :  8dp  — between related elements within a row
Medium : 12dp  — between unrelated items in a list
Base   : 16dp  — screen horizontal margin, card internal padding
Large  : 24dp  — section separator
XLarge : 32dp  — top of screen hero area, major section breaks
```

**Rules:**
- Horizontal screen margin: **16dp** (never less, never more unless in landscape).
- List rows: `paddingVertical(12dp)`, `paddingHorizontal(16dp)`.
- Do NOT use card components (`Card {}`) for plain list rows — use `Row` with a `Divider`. Cards are only for grouped summary blocks (e.g., balance card, budget overview).
- Never use `Modifier.fillMaxWidth().padding(X).padding(Y)` chained padding from multiple layers — flatten into one.
- **Tap targets**: Minimum `48dp × 48dp` for all interactive elements (Material 3 requirement). Use `Modifier.minimumInteractiveComponentSize()`.

---

## 5. Shape System

```kotlin
object KsShape {
    val Card       = RoundedCornerShape(12.dp)
    val Chip       = RoundedCornerShape(6.dp)
    val Button     = RoundedCornerShape(10.dp)
    val Sheet      = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val Input      = RoundedCornerShape(8.dp)
    val Icon       = RoundedCornerShape(8.dp)   // icon containers / avatars
}
```

**Rules:**
- Never use `CircleShape` for icon containers — it is the most recognizable AI-slop pattern on Android.
- Never use `RoundedCornerShape(50)` (which renders as a pill) on containers larger than 40dp.
- Do NOT apply the same corner radius to everything. Each shape token above is intentional.
- **No glassmorphism**: never use `Modifier.blur()` behind a semi-transparent surface.

---

## 6. Motion & Animation

**Principle: Motion must have a job. If it has no job, it has no place.**

| Use case | Allowed animation | Duration |
|---|---|---|
| List item reveal | `fadeIn + slideInVertically` | 120ms |
| Screen transition | Shared element or `fadeIn/Out` | 200–300ms |
| Number change (balance) | `animateFloatAsState` on value | 250ms |
| FAB appearance | `scaleIn` from 0.8f | 150ms |
| Sheet expand | Spring, `dampingRatio=0.9` | 300ms |
| Deletion / swipe | `slideOutHorizontally` | 180ms |

**Banned:**
- Bounce easing (`Spring.DampingRatioHighBouncy`) on data operations — only for playful interactions.
- Fade-up (`slideInVertically`) on **every** list item on every screen load — it becomes wallpaper.
- Looping animations when the user is not interacting with that element.
- Motion that ignores `LocalInspectionMode` or doesn't respect `Settings.Global.ANIMATOR_DURATION_SCALE`.
- Count-up number animations on stat cards (this is the most visible AI-slop motion pattern).

Always check `LocalContext.current.getSystemAnimatorDurationScale()` and multiply durations.

---

## 7. Anti-Slop Rules — Jetpack Compose Edition

These are compiled from all 8 source repos, adapted from web to Android/Compose. They are **hard gates** — do not ship a composable that violates them.

### HARD GATE: Never Do

| ❌ Pattern | Why it's slop | ✅ Replace with |
|---|---|---|
| `Brush.linearGradient(...)` on card backgrounds | Gradient on every card is the #1 AI-generated Android tell | Flat `SurfaceEl` color with `elevation` |
| `CircleShape` icon containers everywhere | Copied from generic Material demos | `KsShape.Icon` (8dp corner) |
| `shimmer` loading on every screen | Lazy pattern, no thought about empty states | Purpose-designed empty state composables |
| Generic `MaterialTheme.colorScheme.primary = Color(0xFF6650A4)` (default purple) | Unconfigured Material3 default | Custom KsColor palette above |
| `animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)` on data | Playful pattern on a finance app | `tween(200)` or gentle spring |
| `Icon(imageVector = Icons.Rounded.Star, ...)` for financial categories | Stock icon set = instant AI recognition | Custom `KsIcons` set, specific metaphors |
| `Text("Seamlessly track your finances")` or similar hype copy | AI-generated marketing copy | Specific, honest, imperative: "Add a transaction" |
| Fake numbers in previews: `"₹1,234.56"` hardcoded everywhere | Lazy placeholder data | Use the `KnowSpendsPreviewParameterProvider` |
| Three icon cards in a row for feature highlights | The most recognizable AI landing pattern | Does not apply — this is a utility app, no marketing UI |

### SHOULD GATE: Almost Never Do

| ⚠️ Pattern | Acceptable only when... |
|---|---|
| `LazyColumn` with same `fadeIn` on every item | Never. If staggered, max 5 items, 20ms stagger |
| Cards with `shadow-lg` equivalent (`elevation = 8.dp`) | Only for modal/overlay surfaces |
| `Spacer(Modifier.height(X))` between items | Use `Arrangement.spacedBy()` on `Column/Row` instead |
| Hard-coded `Modifier.height(56.dp)` on a text container | Use `heightIn(min = 56.dp)` with `wrapContentHeight()` |
| `LinearProgressIndicator` for budget progress | Use custom `KsProgressBar` with `Positive/Negative` color logic |

### DESIGN CHECK: Before shipping any composable, verify

1. **Purpose**: Does every element earn its place? Remove anything decorative that isn't load-bearing.
2. **Color**: Only from `KsColor`. No inline `Color(0xFF...)` in composable files — use tokens.
3. **Typography**: Only from `KsType`. Amounts are always monospace.
4. **Motion**: Does this animation have a job? If you can't state the job in one sentence, remove it.
5. **Tap targets**: Every clickable element ≥ 48dp × 48dp.
6. **Accessibility**: `contentDescription` on every Icon, `semantics { }` on custom interactive elements.
7. **Font scaling**: Does it hold at 200% font scale without clipping? (Test in preview with `fontScale = 2f`)
8. **Dynamic color**: Is it still disabled in `KnowSpendsTheme`?

---

## 8. Component Vocabulary

### Transaction Row
```
[Category Icon (40dp, KsShape.Icon)] [Title + Category] [Amount (right-aligned, monospace)] [Date/time label]
```
- Amount: color `Positive` for income, `Negative` for expense.
- No card wrapper. Plain `Row` with bottom `Divider(color = KsColor.Outline)`.
- Swipe-to-delete via `SwipeToDismiss` with red background reveal.

### Balance Display (Home screen)
```
[Label: "Total Balance"] 
[Amount: 36sp monospace, OnBackground]
[Delta indicator: +/- with Positive/Negative color, 13sp]
```
- One composable, no card, sits directly on `Background` color surface.
- No gradient backdrop. No decorative ring/circle behind it.

### Bottom Nav (Compact width)
- Floating pill design (already specified in AGENTS.md).
- Active item: `Accent` color icon + label.
- Inactive: `OnMuted` icon only (no label for compact — icon speaks).
- 5 items max.

### FAB (Quick-Add)
- Single FAB, bottom-right, `KsShape.Button` (10dp), `Accent` background.
- Label: "Add" — not "New Transaction", not a sparkle emoji.
- Hides on scroll down, reveals on scroll up.

### Category Chip
- `KsShape.Chip` (6dp), `SurfaceEl` background, `OnSurface` text.
- Selected state: `AccentMuted` background, `Accent` text.
- Never use `FilterChip` default colors — override with tokens.

---

## 9. Screen-Level Contracts

### Home Screen
- **Job**: Instant balance status + recent activity at a glance.
- **Layout**: Balance block (top 25% of screen), then `LazyColumn` of recent transactions.
- **Never**: Stat cards with icons in a row. Charts on first load (charts belong in Reports). Greeting text ("Good morning, User!").

### Add Transaction Screen
- **Job**: 3-tap entry. Speed is the entire value.
- **Layout**: Numeric keypad dominates (60%+ of screen). Amount preview at top. Category row below preview.
- **Never**: Form-style input fields. Floating label `TextField` for the amount. Modal dialogs for category selection (use bottom sheet).

### Reports Screen
- **Job**: Help the user understand patterns, not celebrate spending.
- **Charts**: Use `Vico` or custom `Canvas`-drawn charts, not generic MPAndroidChart defaults.
- **Never**: Count-up animations on totals. Pie charts with rainbow 12-slice palettes.

### Inbox Screen (SMS Parser)
- **Job**: Review and approve/reject parsed transactions in bulk.
- **Layout**: Card-per-item (here, cards are correct — each is a decision unit). Approve = green swipe right, Reject = red swipe left.
- **Never**: Approval dialogs. Approve-all without review prompt.

---

## 10. What "Done" Means for UI Work

A UI composable is done when:
1. ✅ Uses only `KsColor`, `KsType`, `KsShape` tokens — no inline magic numbers.
2. ✅ Passes preview at `fontScale = 1f`, `1.5f`, `2f` without clipping.
3. ✅ Passes preview at `Compact` and `Medium` window size class.
4. ✅ All icons have `contentDescription`.
5. ✅ All interactive elements are `≥ 48dp` tap target.
6. ✅ No hardcoded `Color(0xFF...)` outside the design token file.
7. ✅ No gradient fills, no glassmorphism, no dynamic color.
8. ✅ Motion respects `ANIMATOR_DURATION_SCALE`.
9. ✅ An honest person looking at it cannot tell it was AI-generated.

---

*Last updated: 2026-11-12. Sources: 8 open-source design rule repos (see header). Maintained in `/Expence/DESIGN.md`.*
