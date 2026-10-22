# Proposal

## Why

Expenses are stored today as a `Map<String, Long>` in Android DataStore, keyed by description. Same-named expenses overwrite each other, there is no payer, participants, category or date, and iOS has no storage at all. Splitting and totals need a real domain model and relational storage before any UI can be built on them, and the model must be shaped so a backend, auth and sync can be added later without rewriting it.

## What Changes

- Introduce a shared domain model: Household, Member, Expense, Money (integer minor units), Category (fixed set or custom free-text name), and split rule (equal across selected members, exact amounts per member, or percentages per member).
- Add pure, unit-tested business rules: expense validation, equal and percentage splits whose leftover cents go to the payer (so everyone else owes the same), exact-amount and percentage validation (shares must sum to the total, percentages to 100), and summaries (household total, by category, per member paid/share/balance) for all time, a month, or a year.
- Replace DataStore with SQLDelight in shared code, one schema for Android and iOS. Only the SQL driver is platform-specific.
- Make every stored record sync-ready: client-generated UUID ids, `createdAt`/`updatedAt` timestamps, soft-delete marker, and a `householdId` on household-owned rows.
- Seed one default household on first launch (local-only mode; no accounts).
- **BREAKING**: `AddExpenseUC` and `GetExpensesUC` change signatures; the `expect interface ExpensesSource` and its Android/iOS actuals are removed. Existing DataStore data is discarded (it held only throwaway test values).
- Minimal call-site updates so the Android and iOS apps still build against the new API. No new screens.

## Capabilities

### New Capabilities
- `household-members`: the local household and the members expenses are split between.
- `expenses`: recording expenses with description, amount, category, date, payer and split, and reading them back persistently.
- `expense-splitting`: rules that turn an expense's split rule into per-member shares.
- `expense-summaries`: totals per household, per category and per member over all time, a month or a year.

### Modified Capabilities
<!-- none: no existing specs -->

## Impact

- `shared/`: new domain models and rules, new SQLDelight database and repositories, rewritten use cases and Koin modules; `sources/` package and DataStore removed; new `commonTest` and `androidUnitTest` suites.
- Dependencies: add SQLDelight (Gradle plugin, android-driver, native-driver, coroutines-extensions, sqlite-driver for tests); remove `androidx-datastore-preferences`.
- `composeApp/`: `MainViewModel` and Koin setup updated to the new use cases.
- `iosApp/`: `ContentView.swift` view model call updated to compile. Swift interop tooling (SKIE vs KMP-NativeCoroutines) is left for a follow-up change.
- Out of scope: add-expense and summary screens, editing/deleting expenses, settle-up payments, multiple households, stored custom-category list, backend/auth/sync, multi-currency.
