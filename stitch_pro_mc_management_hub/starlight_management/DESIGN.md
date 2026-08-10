---
name: Starlight Management
colors:
  surface: '#f9f9ff'
  surface-dim: '#d3daea'
  surface-bright: '#f9f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f0f3ff'
  surface-container: '#e7eefe'
  surface-container-high: '#e2e8f8'
  surface-container-highest: '#dce2f3'
  on-surface: '#151c27'
  on-surface-variant: '#45464c'
  inverse-surface: '#2a313d'
  inverse-on-surface: '#ebf1ff'
  outline: '#76777d'
  outline-variant: '#c6c6cc'
  surface-tint: '#585e6f'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#151b29'
  on-primary-container: '#7e8395'
  inverse-primary: '#c1c6d9'
  secondary: '#775a19'
  on-secondary: '#ffffff'
  secondary-container: '#fed488'
  on-secondary-container: '#785a1a'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#191c1d'
  on-tertiary-container: '#828485'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dde2f6'
  primary-fixed-dim: '#c1c6d9'
  on-primary-fixed: '#151b29'
  on-primary-fixed-variant: '#414756'
  secondary-fixed: '#ffdea5'
  secondary-fixed-dim: '#e9c176'
  on-secondary-fixed: '#261900'
  on-secondary-fixed-variant: '#5d4201'
  tertiary-fixed: '#e1e3e4'
  tertiary-fixed-dim: '#c5c7c8'
  on-tertiary-fixed: '#191c1d'
  on-tertiary-fixed-variant: '#454748'
  background: '#f9f9ff'
  on-background: '#151c27'
  surface-variant: '#dce2f3'
typography:
  headline-display:
    fontFamily: Playfair Display
    fontSize: 48px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Playfair Display
    fontSize: 32px
    fontWeight: '600'
    lineHeight: '1.3'
  headline-lg-mobile:
    fontFamily: Playfair Display
    fontSize: 28px
    fontWeight: '600'
    lineHeight: '1.3'
  headline-md:
    fontFamily: Playfair Display
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.4'
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.5'
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: '1'
    letterSpacing: 0.05em
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: '1'
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  unit: 8px
  container-max: 1280px
  gutter: 24px
  margin-mobile: 16px
  margin-desktop: 40px
---

## Brand & Style

The design system is crafted for Master of Ceremony (MC) professionals who command the stage. The brand personality is **distinguished, authoritative, and seamless**, mirroring the poise required for high-stakes event hosting. It targets professional presenters, talent agents, and event planners who require a tool that feels as premium as the galas they manage.

The visual style is a blend of **Corporate Modern** and **Luxury Editorial**. It utilizes high-contrast typography and a refined "Stage Mode" to ensure the interface transitions perfectly from the bright lights of a planning office to the dim periphery of a live event stage. The aesthetic prioritizes clarity and executive presence, using wide margins and a disciplined palette to evoke a sense of calm under pressure.

## Colors

The palette is anchored by **Midnight Charcoal (#121826)**, providing a deep, authoritative foundation. **Champagne Gold (#C5A059)** is used sparingly for primary actions, signifying the "spotlight" and high-value status. 

For the "Stage Mode" (Dark Mode), the background shifts to Midnight Charcoal, with surface elements utilizing a slightly lighter charcoal to maintain depth. The Champagne Gold accents remain consistent but gain a subtle outer glow to improve visibility in low-light environments. **Off-White (#F8F9FA)** serves as the primary canvas in light mode to reduce eye strain while maintaining a crisp, professional appearance.

## Typography

This design system employs a high-contrast typographic pairing to balance editorial elegance with functional utility. **Playfair Display** is reserved for headlines and "Stage Titles," providing a sophisticated, literary feel that commands attention.

**Inter** is the workhorse for all functional UI, body copy, and data-heavy tables. It ensures maximum legibility for script reading and schedule tracking. Label styles use increased letter-spacing and uppercase transformations to distinguish metadata from content. In "Stage Mode," font weights for body text should be increased by one tier (e.g., 400 to 500) to ensure high readability against dark backgrounds.

## Layout & Spacing

The layout follows a **Fixed Grid** philosophy for desktop to maintain an "executive dashboard" feel, transitioning to a fluid model for mobile. We use an 8px base unit to ensure consistent vertical rhythm. 

- **Desktop (1280px+):** 12-column grid with 24px gutters. Content is centered with generous 40px outer margins to create an airy, premium feel.
- **Tablet:** 8-column grid with 24px gutters.
- **Mobile:** 4-column grid with 16px gutters and 16px margins. 

Information density is kept low in management views but increases in "Stage Mode" where the MC needs to see the run-of-sheet at a glance. Use "Safe Areas" for primary navigation on mobile to account for one-handed operation during events.

## Elevation & Depth

Visual hierarchy is achieved through **Tonal Layers** supplemented by **Ambient Shadows**. 

1. **Base:** The off-white background (#F8F9FA).
2. **Surface:** White cards with a very soft, diffused shadow (0px 4px 20px rgba(18, 24, 38, 0.05)).
3. **Overlay:** Modals and dropdowns use a more pronounced shadow (0px 10px 30px rgba(18, 24, 38, 0.12)) and a 1px border of the neutral color at 10% opacity.

In "Stage Mode," depth is conveyed through luminosity rather than shadow. Primary cards are slightly lighter than the background, and active items feature a thin 1px gold border to indicate focus.

## Shapes

The design system utilizes **Soft (0.25rem)** roundedness to maintain a professional and architectural feel. Sharp enough to feel precise, yet softened enough to feel modern and accessible.

- **Buttons & Inputs:** 4px (0.25rem) corner radius.
- **Cards & Containers:** 8px (0.5rem) corner radius.
- **Large Modals:** 12px (0.75rem) corner radius.

Data visualization elements (bars, progress indicators) should use flat ends rather than rounded caps to reinforce the precise, analytical nature of the app.

## Components

### Buttons
- **Primary:** Midnight Charcoal background with White text. For "Special" actions (like Bookings), use Gold background with Midnight Charcoal text.
- **Secondary:** Transparent background with 1px Midnight Charcoal border.
- **Tertiary:** Text-only with an underline appearing on hover.

### Cards
Cards are the primary container for event details. They should feature a clean white background, a 1px light grey border, and the subtle ambient shadow defined in the Elevation section. Header areas within cards use the Gold accent for a "spotlight" strip.

### Inputs
Fields use a subtle 1px border. On focus, the border transitions to Midnight Charcoal with a 2px "Champagne Gold" outer glow (2px blur).

### Stage Mode Toggle
A prominent, easily accessible switch in the global navigation. When active, it triggers a system-wide theme swap to high-contrast dark mode, increases the base font size by 10%, and highlights the "Current Event Segment" with a gold border.

### Analytics & Data
Charts use the Midnight Charcoal as the primary data color, with Emerald Green for growth/success and Amber for alerts. Use clean, thin lines for axes and remove unnecessary grid lines to maintain the minimalist aesthetic.