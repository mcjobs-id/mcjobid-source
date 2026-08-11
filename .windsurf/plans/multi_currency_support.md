# Multi-Currency Support Implementation Plan

## Overview
Implement multi-currency support to allow users to display and handle financial values in different currencies (IDR, USD, EUR, SGD, etc.) throughout the application.

## Current State
- `Formatter.kt` uses hardcoded Indonesian Rupiah (IDR) locale
- `UserProfile` and `UserProfileEntity` do not have a currency preference field
- All financial displays are hardcoded to IDR format

## Implementation Steps

### Step 1: Modify Formatter Utility Class
**File:** `app/src/main/java/com/isankamil/mcjobid/util/Formatter.kt`

Changes:
- Add currency enum with supported currencies (IDR, USD, EUR, SGD, MYR, THB)
- Add locale mapping for each currency
- Modify `formatCurrency()` to accept currency parameter
- Add default currency fallback (IDR)
- Maintain backward compatibility

### Step 2: Update UserProfile Domain Model
**File:** `app/src/main/java/com/isankamil/mcjobid/domain/model/UserProfile.kt`

Changes:
- Add `preferredCurrency: String = "IDR"` field
- Update `toEntity()` to include currency field
- Update `fromEntity()` to include currency field

### Step 3: Update UserProfileEntity
**File:** `app/src/main/java/com/isankamil/mcjobid/data/local/entity/UserProfileEntity.kt`

Changes:
- Add `preferredCurrency: String = "IDR"` field

### Step 4: Update SettingsScreen
**File:** `app/src/main/java/com/isankamil/mcjobid/ui/screen/settings/SettingsScreen.kt`

Changes:
- Add currency selection dropdown/composable
- Add currency options (IDR, USD, EUR, SGD, MYR, THB)
- Save selected currency to user profile
- Display current selected currency

### Step 5: Update SettingsViewModel
**File:** `app/src/main/java/com/isankamil/mcjobid/ui/screen/settings/SettingsViewModel.kt`

Changes:
- Add currency selection state
- Add `updatePreferredCurrency()` method
- Include currency in profile save logic

### Step 6: Update ProfileViewModel
**File:** `app/src/main/java/com/isankamil/mcjobid/ui/screen/profile/ProfileViewModel.kt`

Changes:
- Ensure currency field is preserved during profile updates

### Step 7: Update All Currency Display Calls
Files to update:
- `InvoiceScreen.kt` - Invoice amounts, totals
- `ProfileScreen.kt` - Revenue stats
- `AnalyticsScreen.kt` - All financial metrics
- `FollowUpScreen.kt` - Outstanding amounts
- `FinanceScreen.kt` - All financial displays
- `HomeScreen.kt` - Financial summary
- `ClientScreen.kt` - Client metrics
- `AnalyticsViewModel.kt` - Share performance summary
- Any other screen displaying currency

Pattern:
- Replace `Formatter.formatCurrency(amount)` with `Formatter.formatCurrency(amount, userProfile?.preferredCurrency ?: "IDR")`

### Step 8: Update Wizard (Optional)
**File:** `app/src/main/java/com/isankamil/mcjobid/ui/screen/wizard/WizardViewModel.kt`

Changes:
- Add currency selection step or include in existing step
- Default to IDR for new users

## Testing Checklist
- [ ] Currency selection in Settings saves correctly
- [ ] Currency preference persists after app restart
- [ ] All financial displays update to selected currency
- [ ] Backward compatibility with existing profiles (default to IDR)
- [ ] Currency symbols display correctly (Rp, $, €, S$, RM, ฿)
- [ ] Number formatting follows locale conventions
- [ ] Invoice PDF generation uses selected currency
- [ ] Analytics share summary uses selected currency

## Notes
- Default currency: IDR (Indonesian Rupiah)
- Supported currencies: IDR, USD, EUR, SGD, MYR, THB
- Locale mapping:
  - IDR: Locale("id", "ID")
  - USD: Locale("en", "US")
  - EUR: Locale("de", "DE") or Locale("fr", "FR")
  - SGD: Locale("en", "SG")
  - MYR: Locale("en", "MY")
  - THB: Locale("th", "TH")
