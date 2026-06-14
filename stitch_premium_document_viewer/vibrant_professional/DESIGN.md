---
name: Vibrant Professional
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#444653'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#757684'
  outline-variant: '#c4c5d5'
  surface-tint: '#3755c3'
  primary: '#00288e'
  on-primary: '#ffffff'
  primary-container: '#1e40af'
  on-primary-container: '#a8b8ff'
  inverse-primary: '#b8c4ff'
  secondary: '#006a61'
  on-secondary: '#ffffff'
  secondary-container: '#86f2e4'
  on-secondary-container: '#006f66'
  tertiary: '#4c2e00'
  on-tertiary: '#ffffff'
  tertiary-container: '#6b4200'
  on-tertiary-container: '#ffa929'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dde1ff'
  primary-fixed-dim: '#b8c4ff'
  on-primary-fixed: '#001453'
  on-primary-fixed-variant: '#173bab'
  secondary-fixed: '#89f5e7'
  secondary-fixed-dim: '#6bd8cb'
  on-secondary-fixed: '#00201d'
  on-secondary-fixed-variant: '#005049'
  tertiary-fixed: '#ffddb8'
  tertiary-fixed-dim: '#ffb95f'
  on-tertiary-fixed: '#2a1700'
  on-tertiary-fixed-variant: '#653e00'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
  status-success: '#10B981'
  status-warning: '#F59E0B'
  status-error: '#EF4444'
  status-inactive: '#E2E8F0'
  brand-whatsapp: '#25D366'
  ui-surface: '#F8FAFC'
typography:
  display-currency:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-caps:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.05em
  tamil-body:
    fontFamily: Noto Sans Tamil
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 24px
  tamil-label:
    fontFamily: Noto Sans Tamil
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 18px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  touch-target-sm: 44px
  touch-target-lg: 52px
  gutter: 16px
  margin-mobile: 20px
  stack-gap: 12px
---

## Brand & Style
The brand personality is **Efficient, Smart, and Localized**. It balances the reliability of an institutional tool with the warmth of a community-focused tuition center. The UI should evoke a sense of professional mastery—making the tuition center owner feel in control—while remaining approachable for students and parents.

The chosen style is **Modern Corporate with Tactile Affordance**. It leverages high-quality typography and generous whitespace, but departs from cold minimalism by using large, friendly radii and vibrant semantic signaling. The design avoids heavy effects to ensure peak performance on entry-level Android devices, relying on clear hierarchy and purposeful color instead of decorative flair.

**Core Principles:**
- **Utility First:** Prioritize data clarity and tap targets over stylistic ornamentation.
- **Bilingual Harmony:** Treat English and Tamil with equal visual weight.
- **Action-Oriented:** Use color primarily to drive the user toward their next task (collecting fees, marking attendance).

## Colors
The palette is rooted in a **Trustworthy Blue** (`primary`) to establish authority. This is paired with an **Energetic Teal** (`secondary`) for primary actions and a **Warm Orange** (`tertiary`) for highlights and gentle alerts.

**Color Usage Guidelines:**
- **Primary Blue:** Used for headers, primary buttons, and navigation active states.
- **Secondary Teal:** Reserved for "Success" pathways and positive growth metrics.
- **Semantic Triple:** Green, Amber, and Red are strictly reserved for fee status and attendance. Do not use these colors for decorative purposes.
- **Neutral:** A range of Slate grays provides a soft, low-contrast background for text to ensure long-term readability without eye strain.
- **WhatsApp Green:** Used exclusively for the "Send Reminder" triggers to provide instant platform recognition.

## Typography
The typography system uses **Inter** for all UI elements and numerical data, ensuring maximum legibility on varied screen resolutions. **Noto Sans Tamil** is integrated for bilingual support.

**Bilingual Implementation:**
- When displaying Tamil subtitles (e.g., under a field label), use the `tamil-label` style in a secondary gray.
- For student names or center names in Tamil, use `tamil-body` with the same color weight as the English equivalent to ensure parity.
- **Numbers:** Dashboard metrics and fee amounts should use `display-currency` with bold weights to make financial health instantly visible.

## Layout & Spacing
This design system uses a **Fluid Mobile-First Layout**. It rejects complex grids in favor of a single-column stack that prioritizes vertical scrolling—optimized for one-handed thumb operation.

**Key Layout Rules:**
- **Touch Targets:** Interactive elements must never be smaller than 44px. Critical attendance buttons (Present/Absent) use the 52px `touch-target-lg` for high-speed, error-free input.
- **Card-Based Hierarchy:** Content is organized into cards with a standard 20px margin from the screen edge.
- **Bottom Navigation:** The primary app navigation is fixed to the bottom of the viewport for easy reach.
- **Reflow:** On tablets, the single column may transition to a dual-pane layout (List on left, Detail on right), but the core card-stack logic remains.

## Elevation & Depth
To maintain high performance on budget hardware, the design system utilizes **Tonal Layers** and **Ultra-Soft Shadows**. 

- **Level 0 (Base):** The main background uses `ui-surface` (#F8FAFC).
- **Level 1 (Cards):** White background with a subtle 1px border (#E2E8F0) and a very soft, diffused shadow (Offset: 0, 2px; Blur: 8px; Opacity: 0.05).
- **Level 2 (Active/Floating):** Used for bottom nav and floating action buttons. Stronger shadow (Blur: 12px; Opacity: 0.1) to clearly separate interactive layers from content.

Avoid using backdrop blurs or complex CSS filters that tax mobile GPUs. Depth should feel "flat but distinct."

## Shapes
The shape language is defined by **Large, Friendly Radii**. This "app-native" look distinguishes the product from traditional, boxy enterprise software.

- **Standard Elements:** Buttons and input fields use a 12px (0.75rem) radius.
- **Cards:** Main content containers use 16px to 24px (1rem to 1.5rem) radii for a soft, modern feel.
- **Status Badges:** Use a fully rounded "pill" shape (999px) to differentiate them from interactive buttons.
- **Icons:** Use circular containers for student avatars and attendance status indicators (Check/X).

## Components
Consistent component behavior is critical for the "Vibrant Professional" aesthetic.

**Buttons:**
- **Primary:** Solid Blue background, white text, 12px radius, 48px height.
- **Attendance (Dual):** Large 52px buttons. "Present" is Green; "Absent" is Red. High-contrast white icons for clear visibility.

**Cards:**
- Mobile-optimized cards featuring a student name in `headline-md`, fee status badge in the top right, and a "WhatsApp Reminder" button at the bottom.

**Input Fields:**
- Large 44px minimum height. Labels include Tamil translations in a smaller, secondary font directly beneath the English label. Border turns Primary Blue on focus.

**Status Indicators:**
- **Paid:** Green pill, low-opacity green background.
- **Pending:** Amber pill, low-opacity amber background.
- **Overdue:** Red pill, high-contrast red background to signal urgency.

**Tabs & Navigation:**
- Fixed bottom bar with 4-5 items. Active items use the Primary Blue for the icon and a small indicator dot below.