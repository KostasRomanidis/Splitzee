# Design

## Context

`shared/` already follows a use case -> repository -> source layering with Koin (`commonModules`, `initKoin`). The source is an `expect interface ExpensesSource` with a DataStore actual on Android and an empty actual on iOS. iOS `initKoin` registers no source, so iOS cannot resolve `AddExpenseUC` at runtime. There is no `commonTest`. Kotlin 2.2.20, kotlinx-datetime 0.7.1 and coroutines 1.10.2 are in the catalog. `MainViewModel` (Android) and `ContentView.swift` (iOS) are the only callers of the use cases.

## Goals / Non-Goals

**Goals:**
- Business rules live in plain Kotlin with no I/O, fully covered by `commonTest`.
- One SQLDelight schema shared by both platforms. The only platform code is the `SqlDriver`.
- The record shape (ids, timestamps, deletion marker, `householdId`) supports later sync without schema rewrites.

**Non-Goals:**
- No new Gradle modules. Clean-architecture boundaries are packages plus `internal` visibility inside `shared`, which matches today's structure.
- No sync engine, remote source, auth, or conflict resolution. Only the data shape is prepared for them.
- No Swift-interop tooling change. SKIE vs KMP-NativeCoroutines is a follow-up change.
- No edit or delete use cases. The deletion marker exists and is respected by queries, but nothing sets it yet.

## Decisions

### Package layout (inside `shared/commonMain`, `com.kromanid.splitzee`)
```
models/        Household, Member, Expense, ExpenseShare, Money, Category,
               FixedCategory, SplitRule, Period, Summary, error types   (pure)
rules/         split calculation, expense validation, summary aggregation (pure)
repositories/  HouseholdRepo, ExpensesRepo interfaces + SQLDelight Impl (existing pattern: interface + internal Impl in one file)
usecases/      existing UC pattern: public interface + internal Impl with execute()
database/      .sq files (sqldelight srcDir), generated SplitzeeDatabase
di/            commonModules + expect val platformModule (SqlDriver)
```
The `sources/` package is deleted, because the repository talks to the generated SQLDelight queries directly. A separate source interface would only wrap the generated code. When a backend arrives, a remote source and sync engine slot in behind the same repository interfaces.

### Money: `value class Money(val minor: Long)`
Integer minor units keep the existing `Long` convention and give exact sums. The currency is stored once on `Household` (an ISO code, default `"EUR"`), not on each amount, because multi-currency is out of scope. Alternatives: `Double` was rejected because it is inexact. A `BigDecimal` library was rejected because it adds a dependency with no benefit at household scale.

### Category: sealed `Fixed(FixedCategory) | Custom(name)`
`FixedCategory` is an enum. Stored as two columns, `category_kind` (`FIXED`/`CUSTOM`) and `category_value` (the enum name or the trimmed custom name). The summary groups custom categories by `trim().lowercase()`. The display name is the first one encountered. Upgrading later to a stored custom list (the reading rejected for now) only needs a new table and a migration of the `CUSTOM` rows.

### SplitRule: sealed `Equal(participants: List<MemberId>) | Exact(shares: List<Pair<MemberId, Money>>) | Percent(shares: List<Pair<MemberId, Int>>)`
`Percent` holds basis points (33.33% = 3333, total 10000), so percentages stay integers like `Money`. These are ordered lists, not maps, because the first selected participant takes the remainder when the payer is not a participant. A pure `computeShares(amount, payer, rule)` returns either `List<ExpenseShare>` or errors. It floors every share and adds the leftover to the payer's share, or to the first participant's. Equal and Percent share that one remainder step.

The payer absorbs the remainder because then every non-payer in an equal split owes exactly the same, which was the stated goal. Alternatives: round-robin by selection order was rejected because it gives non-payers different amounts. Sub-cent (thousandths) storage was rejected because 10.00 / 3 still leaves a remainder, now invisible, and the shares no longer sum to the amount. "Full amount owed" needs no rule of its own. It is `Equal` with one participant, and only a UI shortcut.

### Persist computed shares, not just the rule
Shares are written to `expense_share` rows, with `position`, in the same transaction as the expense. Summaries then read stored facts, history never changes if the split algorithm changes, and a future backend receives final numbers. `expense.split_type` (`EQUAL`/`EXACT`/`PERCENT`) and the nullable `expense_share.percent_bp` record what the user entered, for later editing UI. Percentages cannot be recovered from rounded amounts.

### Validation result instead of exceptions
`AddExpenseUC.execute(...)` returns a sealed `AddExpenseResult` with two cases: `Success(expense)` and `Invalid(errors: List<ExpenseError>)`. All rule failures are collected, not just the first. Kotlin exceptions crash when they cross into Swift without `@Throws`, and a sealed result maps cleanly to a Swift enum once SKIE is adopted. The same pattern applies to `AddMemberUC`.

### Schema (SQLite via SQLDelight)
```
household     (id TEXT PK, name, currency, created_at, updated_at, deleted_at)
member        (id TEXT PK, household_id FK, name, created_at, updated_at, deleted_at)
expense       (id TEXT PK, household_id FK, description, amount_minor INTEGER,
               category_kind, category_value, date TEXT 'YYYY-MM-DD',
               paid_by FK member, split_type, created_at, updated_at, deleted_at)
expense_share (id TEXT PK, expense_id FK, member_id FK, amount_minor INTEGER,
               percent_bp INTEGER NULL, position INTEGER, created_at, updated_at, deleted_at)
index expense(household_id, date)
```
- Ids are `kotlin.uuid.Uuid.random().toString()` (stdlib; opt in to `ExperimentalUuidApi`). Client-generated ids are what make offline creation and later sync collision-free. Autoincrement was rejected for that reason.
- Timestamps are epoch millis from an injected `kotlin.time.Clock`. The injected clock also gives "today" for the default expense date via `TimeZone.currentSystemDefault()`, and tests can fix both.
- The date is ISO text, so period filters are plain lexical range queries (`date >= ? AND date < ?`).
- Every read query filters `deleted_at IS NULL`.
- Schema version 1. Future changes use SQLDelight `.sqm` migrations.

### Summaries: SQL range filter + pure aggregator
SQL selects the period's expenses with their shares (one join). A pure `summarize(members, expenses)` builds the household total, the by-category totals and per-member paid/share/balance, with zeros for members who have no activity. `Period` (`AllTime | Month(year, month) | Year(year)`) maps to a `[start, end)` date range. Doing the aggregation in Kotlin keeps it in `commonTest`. Data volume is household-sized, so this costs nothing. Pure SQL `GROUP BY` was rejected because it would move the rules out of testable domain code.

### Observability
The repositories expose `Flow`s via SQLDelight `coroutines-extensions` (`asFlow().mapToList(Dispatchers.IO)`). The summary combines the members flow and the period's expenses flow.

### Default household
`HouseholdRepo.current()` reads the single household. If none exists, it creates the household plus a "Me" member in one transaction. The household name defaults to "Home". Use cases resolve the household through this method, so callers never pass a `householdId` in local mode.

### DI
`expect val platformModule: Module` is included in `initKoin`.
- Android actual: `AndroidSqliteDriver(SplitzeeDatabase.Schema, androidContext(), "splitzee.db")`.
- iOS actual: `NativeSqliteDriver(SplitzeeDatabase.Schema, "splitzee.db")`.

This fixes iOS DI, and the app-level `androidMain` module goes away. Repositories become `single` because they hold the database. Use cases stay `factory`.

### Tests
- `commonTest` (kotlin-test) covers split, validation and summary rules. These are the spec scenarios.
- `androidUnitTest` uses an in-memory `JdbcSqliteDriver` (`app.cash.sqldelight:sqlite-driver`). It covers repository round-trips, atomic expense + shares writes, deleted-row exclusion, ordering and the default-household seed.

## Risks / Trade-offs

- [SQLDelight/Kotlin 2.2.20 compatibility] -> SQLDelight is pinned to 2.1.0. Newer releases raise the Kotlin Gradle plugin past 2.2.20 (2.2.1 -> 2.2.21, 2.4.1 -> 2.3.10), which breaks the KMP-NativeCoroutines compiler plugin in `shared` at iOS compile time. Upgrade SQLDelight, Kotlin and NativeCoroutines together.
- [iOS static framework needs SQLite linked] -> SQLDelight's native driver links `sqlite3` by default. If Xcode reports undefined `sqlite3_*` symbols, add `-lsqlite3` to the iOS target's Other Linker Flags.
- [`kotlin.uuid` is experimental] -> The API is small and stable in practice. If it changes, the single call site is easy to swap.
- [Existing DataStore data discarded] -> It held only dev test values. The orphaned `user_expenses` preferences file is harmless and is left in place.
- [Deletion marker unused] -> A deliberate prep for sync, at the cost of one column per table and one predicate per query.
- [Default currency hard-coded to EUR] -> It is stored per household, so a later settings screen or locale default can change it without a migration.

## Migration Plan

This is a fresh SQLDelight database (schema v1) with no data migration from DataStore. Rollback means reverting the change. The old DataStore file is still on the device and the old code reads it again.
