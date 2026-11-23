---
name: Atacama Fieldwork
colors:
  surface: '#fff8f4'
  surface-dim: '#ebd6c5'
  surface-bright: '#fff8f4'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#fff1e7'
  surface-container: '#ffead9'
  surface-container-high: '#fae5d2'
  surface-container-highest: '#f4dfcd'
  on-surface: '#241a0f'
  on-surface-variant: '#4f453c'
  inverse-surface: '#3a2e22'
  inverse-on-surface: '#ffeee0'
  outline: '#81756b'
  outline-variant: '#d3c4b9'
  surface-tint: '#78583a'
  primary: '#452b11'
  on-primary: '#ffffff'
  primary-container: '#5e4125'
  on-primary-container: '#d6ae8a'
  inverse-primary: '#e9bf9a'
  secondary: '#006970'
  on-secondary: '#ffffff'
  secondary-container: '#94f1fb'
  on-secondary-container: '#006f77'
  tertiary: '#2d340c'
  on-tertiary: '#ffffff'
  tertiary-container: '#434b20'
  on-tertiary-container: '#b1bb85'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdcbf'
  primary-fixed-dim: '#e9bf9a'
  on-primary-fixed: '#2c1601'
  on-primary-fixed-variant: '#5e4125'
  secondary-fixed: '#94f1fb'
  secondary-fixed-dim: '#77d5de'
  on-secondary-fixed: '#002022'
  on-secondary-fixed-variant: '#004f55'
  tertiary-fixed: '#dee8af'
  tertiary-fixed-dim: '#c2cc95'
  on-tertiary-fixed: '#181e00'
  on-tertiary-fixed-variant: '#424b20'
  background: '#fff8f4'
  on-background: '#241a0f'
  surface-variant: '#f4dfcd'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: 0em
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 26px
    letterSpacing: 0em
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '500'
    lineHeight: 26px
    letterSpacing: 0em
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.01em
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-lg:
    fontFamily: Inter
    fontSize: 15px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.02em
  label-md:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '600'
    lineHeight: 18px
    letterSpacing: 0.03em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.04em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system is engineered for field operators, wilderness guides, and transit drivers navigating the diverse environments of Chile—from direct high-altitude solar glare in San Pedro de Atacama to shaded temperate rainforest routes in Patagonia. The core user operates under active physical conditions: single-handed terminal interaction, vehicle vibrations, direct sunlight, and rapid field check-ins.

The visual style merges Material Design 3 structural ergonomics with a rugged, tactile field-tool aesthetic. It rejects delicate lines, low-contrast text, and micro-interactions in favor of high-legibility typographic hierarchies, high contrast ratios, large minimum targets (48dp+), and warm, mineral-derived earth tones. The interface feels dependable, mission-critical, calm, and impervious to harsh outdoor glare.

## Colors

The color palette is grounded in Chilean geology and optimized for high ambient light readability:

- **Primary (`#5E4125`)**: Roasted earth tone. Applied to structural navigation anchors including the Top App Bar, Bottom Navigation Bar background, and high-priority Floating Action Buttons (FABs).
- **Secondary (`#3297A0`)**: Andean mineral teal. Serves as the active interactive state, highlighting selection rings, active tabs, route segments, and interactive filter chips.
- **Tertiary (`#6B7445`)**: Olive brush. Applied exclusively to operational confirmations, on-time passenger manifests, check-in validation, and active green-status indicators.
- **Alert (`#C0623F`)**: Terracotta clay. Deployed for urgent disruptions, delayed pickups, route warnings, and critical itinerary cancellations.
- **Surface & Backgrounds**: Base background is `#F7F3EE` (soft warm parchment), selected specifically to minimize solar reflection compared to pure white while preserving contrast against `#3A2E22` primary text. `#C6AF94` provides container fills, structural grouping dividers, and inactive state fills.
- **Text Hierarchies**: `#3A2E22` provides high-contrast legibility for primary labels and data, while `#6B5B4B` handles timestamps, sub-labels, and metadata without fading into the parchment base.

## Typography

Inter serves as the operational typeface across all levels. Its neutral grotesque geometry, high x-height, and open apertures preserve legibility during quick glances and on vibrating vehicle-mounted displays.

- **Weight Bumping**: Standard body text shifts up to `500` (Medium) for `body-lg` and `body-sm` to maintain letterform clarity against sunlight glare.
- **Letter Spacing**: Labels and status pills utilize increased tracking (`+0.02em` to `+0.04em`) to ensure legibility when rendered in condensed badges or uppercase metadata tags.
- **Display Scaling**: Font sizes scale to prevent cramped line breaks on standard handheld terminals (360dp–412dp screen widths). Headlines prioritize horizontal brevity and avoid hyphenation.

## Layout & Spacing

The layout model adheres to native Android 4dp/8dp incremental grid systems with fluid vertical stacking:

- **Mobile Viewports (<600dp)**: Employs a 4-column fluid layout with `16px` (`1rem`) outer canvas margins and `16px` gutters. Critical operational elements span full card widths to accommodate finger tapping while wearing gloves.
- **Tablet / In-Vehicle Dash Mounts (600dp–840dp+)**: Shifts to an 8-column layout with `24px` margins. Dual-pane patterns partition the passenger manifest list on the left and active route/passenger detail cards on the right.
- **Touch Target Integrity**: No interactive element may possess a hit target smaller than 48dp × 48dp, even when visual icons or chips are smaller.
- **Vertical Padding**: Card internals strictly use `space-lg` (24px) for critical grouping headers and `space-md` (16px) for item rows to prevent accidental taps during transit.

## Elevation & Depth

Visual hierarchy uses Material Design 3 tonal surface layering supplemented by subtle, warm ambient shadows rather than stark grayscale drops:

- **Level 0 (Base)**: `#F7F3EE` parchment background. Flat, no elevation.
- **Level 1 (Cards & Lists)**: `#FFFFFF` or `#C6AF94` with 20% alpha on top of `#F7F3EE`. Uses a low-contrast warm shadow: `0px 1px 3px rgba(58, 46, 34, 0.08), 0px 2px 6px rgba(58, 46, 34, 0.04)`.
- **Level 2 (Active Sheets & App Bars)**: Solid `#5E4125` for structural bars with crisp tonal boundaries. Sticky components use a tinted shadow: `0px 4px 12px rgba(94, 65, 37, 0.16)`.
- **Level 3 (Modals & Alert Drawers)**: Pure `#FFFFFF` surface resting on a darkened 40% `#3A2E22` scrim, utilizing `0px 8px 24px rgba(58, 46, 34, 0.20)`.
- **Tonal Borders**: Elevation transitions are reinforced with a 1px solid stroke in `#C6AF94` at 40% opacity to preserve card definition in wash-out ambient light conditions.

## Shapes

The design system implements Material 3 roundedness geometry:

- **Cards & Manifest Containers**: Use `rounded-2xl` (16dp / 1rem radius) to maintain a modern, friendly, tactile feel that defines interactive content clusters.
- **Pills, Badges & Chips**: Strict full rounding (`rounded-full` / 9999px) for operational status chips, passenger count badges, and filter toggles.
- **Action Buttons**: `rounded-full` for Floating Action Buttons and primary operational triggers; `rounded-lg` (8dp) for secondary inline buttons and text field containers.
- **Bottom Sheets**: Top-left and top-right radii use `28dp` for comfortable thumb drag interactions.

## Components

### Buttons
- **Primary Field Action**: Filled container with `#5E4125`, content in `#F7F3EE`. Height 52dp, min-width 120dp, fully rounded (`rounded-full`). Pressed state transitions to an inner overlay of `rgba(255, 255, 255, 0.12)`.
- **Secondary Action**: Bordered container with a 2dp outline of `#5E4125`, transparent background, and `#5E4125` typography.
- **Tonal Action**: Filled container with `#C6AF94` at 35% opacity, text in `#3A2E22`.

### Chips & Badges
- **Status Pills**: `rounded-full`, 32dp height, padding horizontal 16px.
  - *Confirmed / Boarded*: `#6B7445` background with `#FFFFFF` text.
  - *Active / In Transit*: `#3297A0` background with `#FFFFFF` text.
  - *Delayed / Cancelled*: `#C0623F` background with `#FFFFFF` text.
  - *Pending / Neutral*: `#C6AF94` at 30% fill with `#3A2E22` text.

### Manifest Cards
- Background is `#FFFFFF` resting on the `#F7F3EE` canvas, framed by a 1px border in `#C6AF94` (50% opacity). Border radius is 16dp (`rounded-2xl`).
- Interior content follows a standard structure: Left edge holds a 6dp vertical status bar (`#6B7445` or `#C0623F`), followed by passenger name (`headline-sm`), hotel/pickup zone (`body-md`), and a 48dp quick-action check-in toggle button on the right.

### Input Fields & Search
- Container height 56dp with `rounded-lg` (8dp) corners. Background is `#C6AF94` at 20% opacity.
- Border is 1px `#6B5B4B` (resting) and shifts to 2dp `#3297A0` (focused).
- Text color is `#3A2E22` with placeholder text in `#6B5B4B`. Trailing clear and barcode/QR scanner icons are sized to 24dp with a 48dp tap target.

### Checkboxes & Radio Selection
- 24dp interactive mark centered within a 48dp touch footprint.
- Selected state fill is `#3297A0` with a pure `#FFFFFF` checkmark/indicator. Unselected border is 2dp solid `#6B5B4B`.

### Lists & Manifest Dividers
- Single-line or multi-line passenger rows maintain a minimum 64dp height.
- Row separators use `#C6AF94` with 30% opacity, inset by 16px to align with text content edges.

### App Bars & Navigation
- **Top App Bar**: Grounded in `#5E4125` with `#F7F3EE` iconography and title. Elevation level 2.
- **Bottom Navigation**: Background `#5E4125`. Inactive icons/labels use `#C6AF94` at 70% opacity; active items feature a `#3297A0` pill container with `#FFFFFF` iconography.