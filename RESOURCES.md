# RESOURCES.md — Design Resources Catalogue

> Master reference for all design resources available to this project.
> Read alongside [`DESIGN.md`](./DESIGN.md) for the KnowSpends design contract.
> Updated: 2026-11-12

---

## ✅ Part 1: Installed Agent Skills (GitHub Repos → `~/.claude/skills/`)

These are **cloned and symlinked** into `~/.claude/skills/` and are active in every Claude Code / Antigravity session.

| Skill | Source | What it does |
|---|---|---|
| `ui-skills` | [ibelick/ui-skills](https://github.com/ibelick/ui-skills) · [ui-skills.com](https://ui-skills.com) | Routing layer for UI work — categories: baseline-ui, component-canvas, create-canvas, create-design-md, improve-ui, fixing-accessibility, fixing-motion-performance, fixing-metadata |
| `flawless-ui-ux` | [smokeeaasd/flawless-ui-ux](https://github.com/smokeeaasd/flawless-ui-ux) | UI/UX inspection CLI — product-specific visual design, anti-slop directives, rendered accessibility testing via Playwright/axe |
| `taste-skill` | [Leonxlnx/taste-skill](https://github.com/Leonxlnx/taste-skill) | Design discernment — typography, layout variance, color systems, motion |
| `no-slop-ui` | [LeoStehlik/no-slop-ui](https://github.com/LeoStehlik/no-slop-ui) | Rules against glassmorphism, gradient abuse, generic UI tells |
| `avoid-ai-design` | [funboy322/avoid-ai-design](https://github.com/funboy322/avoid-ai-design) | Audits AI-generated frontend, scanner (`detect.mjs`), rewrites around intentional direction |
| `anti-slop` | [miqdadbadjuber/anti-slop](https://github.com/miqdadbadjuber/anti-slop) | 40 hard-gate rules, Delivery Gate, Liveliness Toolkit, mobile layout skill |
| `netxeo-security` | [Netxeo/skill-file-security](https://github.com/Netxeo/skill-file-security) | OWASP Top 10 + CWE Top 25 security skill for AI agents |
| `design-guide` | [GrubbyLee/design-guide](https://github.com/GrubbyLee/design-guide) | Pick direction → lock contract → build UI → verify. Includes 15+ reference files |
| `web-design-skills` | [lotfb86/web-design-skills](https://github.com/lotfb86/web-design-skills) | Frontend design philosophy, responsive, guidelines review |
| `huashu-design` | [alchaincyf/huashu-design](https://github.com/alchaincyf/huashu-design) | Prototypes, slides, motion, MP4 export via HTML |
| `islandcn` (`islands`) | [opencoredev/islandcn](https://github.com/opencoredev/islandcn) | Open-source Dynamic Island for web apps — live activity pill engine, spring physics, split/minimal/expanded states, and presets (upload, timer, playback, call, ETA, battery) |

### CLI Tools Installed (pnpm global)

```bash
antislop-ai --version    # 3.2.28  — slop scanner
flawless-ui-ux --version # 1.0.0   — UI/UX inspection CLI
npx ui-skills start      # routes to right skill category
```

---

## 📦 Part 2: npm / Web Libraries (Web Projects Only)

> ⚠️ **KnowSpends is Android/Jetpack Compose** — these are web-only.
> Use for any future web project. Install commands ready to copy.

### Animation & Motion

| Library | Install | Use |
|---|---|---|
| **Anime.js** | `pnpm add animejs` | Lightweight JS animation for the web — timelines, keyframes, SVG, CSS |
| **Animate UI** | `pnpm add @animate-ui/react motion` | Animated Radix/Base UI/React Aria drop-in primitives built with Motion |
| **Motion Primitives** | `pnpm add motion` | Ready-to-use animation & interaction components — [motion-primitives.com](https://motion-primitives.com) |
| **SmoothUI** | `pnpm dlx smoothui-cli@latest add <component>` | Micro-interactions, Tailwind+Motion — [smoothui.dev](https://smoothui.dev) |
| **Kinetics** | Via copy from [kinetics.colorion.co](https://kinetics.colorion.co) | 150+ motion effects with React code + prompts |

### UI Component Libraries

| Library | Install | Use |
|---|---|---|
| **shadcn/ui** | `pnpm dlx shadcn@latest add <component>` | Gold standard — clean, reusable, copy-paste components |
| **Aceternity UI** | Copy from [ui.aceternity.com](https://ui.aceternity.com) | 200+ animated React/Tailwind components |
| **Magic UI** | `pnpm add magic-ui` | Beautifully designed modern web components — [magicui.design](https://magicui.design) |
| **Arc UI** | `pnpm dlx shadcn@latest add @uiarc/<component>` | React component library with built-in motion — [uiarc.dev](https://uiarc.dev) |
| **Uiverse** | Copy from [uiverse.io](https://uiverse.io) | Community-built interactive UI elements |

### MCP Servers (Agent Integration)

```bash
# 21st.dev — component registry plugs into agents
claude mcp add --transport http 21st https://21st.dev/api/mcp

# Arc UI — component docs for agents
claude mcp add --transport http arc https://uiarc.dev/api/mcp

# UI Skills — dynamic skill routing
claude mcp add --transport http ui-skills https://www.ui-skills.com/mcp
```

---

## 🌐 Part 3: Reference Websites — Design Inspiration & Resources

> These are curated websites, not installable. Bookmark them. Feed them to your agent as context.

### 🎨 Design Systems & Component References

| Site | What it has |
|---|---|
| [component.gallery](https://component.gallery) | 2,600+ examples of how top design systems solve the same UI element |
| [styles.refero.design](https://styles.refero.design) | 2,000+ real product styles with typography from live products |
| [designmd.ai](https://designmd.ai) | Design systems as Markdown — your agent can read them directly |
| [ui.shadcn.com](https://ui.shadcn.com) | shadcn/ui docs — gold standard component reference |
| [ui.aceternity.com](https://ui.aceternity.com) | Aceternity UI — 200+ animated components |
| [magicui.design](https://magicui.design) | Magic UI component gallery |
| [21st.dev](https://21st.dev) | Component registry with MCP agent integration |
| [beui.dev](https://beui.dev) | Curated UI component showcase |
| [uiable.com](https://uiable.com) | Ready-to-use UI components collection |
| [microkit.co](https://microkit.co) | Micro-interactions that make interfaces feel polished |

### 📱 Mobile & App Design Inspiration

| Site | What it has |
|---|---|
| [mobbin.com](https://mobbin.com) | Real app screenshots — flows, screens, onboarding for iOS & Android |
| [appshot.gallery](https://appshot.gallery) | Real app screenshots for mobile UI reference |
| [navbar.gallery](https://navbar.gallery) | Nav bar design patterns and examples |

### 🎞️ Motion & Animation References

| Site | What it has |
|---|---|
| [transitions.dev](https://transitions.dev) | Page and element transition examples with code |
| [60fps.design](https://60fps.design) | Motion design principles and performance-safe patterns |
| [motionin.design](https://motionin.design) | Motion design inspiration and interaction patterns |
| [motion-primitives.com](https://motion-primitives.com) | Animation components ready to use |
| [kinetics.colorion.co](https://kinetics.colorion.co) | 150+ motion effects with React code + prompts |
| [smoothui.dev](https://smoothui.dev) | Smooth micro-interaction components |
| [animate-ui.com](https://animate-ui.com) | Animated UI primitives |

### 🌈 CSS Effects & Visual Elements

| Site | What it has |
|---|---|
| [scrolltide.co](https://scrolltide.co) | 600+ design prompts + 200+ components for 3D & scroll-driven sites |
| [scrollshaders.com](https://scrollshaders.com) | 117 animated shaders in 11 styles |
| [backgrounds.supply](https://backgrounds.supply) | Background textures, gradients, and patterns |
| [text-effects.colorion.co](https://text-effects.colorion.co) | Creative CSS text animations and effects |
| [gradientbuttons.colorion.co](https://gradientbuttons.colorion.co) | Gradient button styles (use sparingly — per DESIGN.md) |
| [glass.samasante.com](https://glass.samasante.com) | Liquid glass refraction components (reference only — banned in KnowSpends) |
| [circleloaders.dominikakissi.com](https://circleloaders.dominikakissi.com) | Creative loading animations |

### ✍️ Typography & Fonts

| Site | What it has |
|---|---|
| [fontshare.com](https://fontshare.com) | High-quality free fonts for commercial use |
| [iter0.com](https://iter0.com) | Type specimen and font pairing tools |

### 🤖 AI Design Prompts & Direction

| Site | What it has |
|---|---|
| [jiro.build](https://jiro.build) | 1,400+ AI-ready design prompts for beautiful websites |
| [kage.design](https://kage.design) | Real UI inspiration mapped straight to agent prompts |
| [vibeprompts.dev](https://vibeprompts.dev) | Ready-made prompts for dashboards and landing pages |
| [designbookmark.com/m](https://designbookmark.com/m) | Curated design bookmarks for AI coding |
| [v0.app/templates](https://v0.app/templates) | Vercel v0 UI templates — good reference starting points |

### 🖼️ Inspiration Galleries

| Site | What it has |
|---|---|
| [minimal.gallery](https://minimal.gallery) | Curated high-end minimal sites — feed as reference to your agent |
| [landdding.com](https://landdding.com) | Landing page design inspiration |
| [wedoflow.com](https://wedoflow.com) | Flow and app design showcase |
| [footer.design](https://footer.design) | Better footer patterns and inspiration |
| [cta.gallery](https://cta.gallery) | CTA (call-to-action) design inspiration |

### 🎁 Assets & Illustrations

| Site | What it has |
|---|---|
| [kitbitz.art](https://kitbitz.art) | 2,000+ hand-drawn illustrations |
| [3dicons.co](https://3dicons.co) | Free 3D icons for modern interfaces |
| [mapcn.dev](https://mapcn.dev) | Map components — markers, routes, popups |

### 🛠️ Developer Tools

| Site | What it has |
|---|---|
| [animejs.com](https://animejs.com) | Anime.js docs — lightweight JS animation library |
| [uiarc.dev](https://uiarc.dev) | Arc UI component library with MCP integration |

---

## 🤖 Part 4: Android/Compose Equivalents

Since KnowSpends is **Jetpack Compose**, here are the Compose-native equivalents of the web libraries above:

| Web library | Compose equivalent | Notes |
|---|---|---|
| Anime.js / Motion | `AnimationSpec`, `Animatable`, `animate*AsState` | Built-in — no extra dep |
| shadcn/ui | Material 3 + custom `KsColor`/`KsShape` tokens | Per DESIGN.md |
| Lottie (animations) | `com.airbnb.android:lottie-compose` | For complex animations only |
| Coil (images) | `io.coil-kt.coil3:coil-compose` | Already in project |
| Chart libraries | `com.patrykandpatrick.vico:compose` | Recommended in DESIGN.md |
| Liquid Glass | ❌ Not applicable | Banned in DESIGN.md |

---

*All GitHub repos are cached at: `/scratch/design-rules-cache/`*
*All agent skills are linked at: `~/.claude/skills/`*
