# Tasks

## 1. Build setup

- [x] 1.1 Add SQLDelight 2.x to `gradle/libs.versions.toml` (plugin, `android-driver`, `native-driver`, `coroutines-extensions`, `sqlite-driver`) and apply the plugin in `shared/build.gradle.kts` with database `SplitzeeDatabase`, package `com.kromanid.splitzee.database`. Verify `./gradlew :shared:generateCommonMainSplitzeeDatabaseInterface` succeeds.
- [x] 1.2 Add `commonTest` (`kotlin-test`) and `androidUnitTest` (`sqlite-driver`, `kotlin-test-junit`) dependencies, and opt in to `kotlin.uuid.ExperimentalUuidApi` for all source sets. Verify `./gradlew :shared:allTests` runs (no tests yet).

## 2. Domain models and rules (pure Kotlin, `commonTest`)

- [x] 2.1 Add `models/`: `Money` (value class over minor units, with `+`/`-`), `Household`, `Member`, `FixedCategory` enum (Groceries, Rent, Utilities, Household, Transport, Dining, Entertainment, Other), sealed `Category`, sealed `SplitRule` (`Equal`, `Exact`, `Percent` in basis points), `Expense`, `ExpenseShare`, sealed `Period` with `[start, end)` date range, `Summary`, and error types. Verify with `PeriodTest` covering the month-boundary and year scenarios.
- [x] 2.2 Implement `computeShares(amount, payer, rule)` in `rules/` for equal, exact and percentage splits (floor then remainder to the payer if participating, else to the first participant; exact sum check; percent sum = 10000 bp; positive shares; at least one participant; no duplicates). Verify with `SplitRulesTest` covering every `expense-splitting` scenario.
- [x] 2.3 Implement expense validation in `rules/` (blank description, non-positive amount, unknown payer or participant, blank custom category trimmed, all errors collected, split errors included). Verify with `ExpenseValidationTest` covering the `expenses` validation, category and payer-not-participant scenarios.
- [x] 2.4 Implement summary aggregation in `rules/` (household total, by category with case-insensitive custom grouping, per-member paid/share/balance with zero rows for inactive members). Verify with `SummaryRulesTest` covering every `expense-summaries` aggregation scenario, including balances netting to zero across a multi-expense fixture.

## 3. Persistence (SQLDelight)

- [x] 3.1 Write `.sq` files for `household`, `member`, `expense`, `expense_share` (schema with `split_type` and nullable `percent_bp`, index, queries filtering `deleted_at IS NULL`, expenses ordered by date desc then created_at desc, period range query with shares join). Verify that code generation succeeds.
- [x] 3.2 Add `expect val platformModule` with Android (`AndroidSqliteDriver`) and iOS (`NativeSqliteDriver`) actuals providing `SqlDriver` and `SplitzeeDatabase`. Include it in `initKoin`. Verify `./gradlew :shared:compileKotlinIosSimulatorArm64 :shared:compileDebugKotlinAndroid` succeeds.
- [x] 3.3 Implement `HouseholdRepo` (current household, seeded with a "Home" household, currency EUR and member "Me" in one transaction; add member; observe members) using an injected `Clock` and `Uuid` ids. Verify with an `androidUnitTest` on an in-memory driver: the seed happens once, two "Ana" members get distinct ids, and observers see an added member.
- [x] 3.4 Rewrite `ExpensesRepo` (add the expense and its shares in one transaction with `position`; observe expenses with shares; observe expenses in a period). Verify with an `androidUnitTest`: round-trip of all fields, same-description expenses both kept, ordering, deleted rows excluded, and the shares write rolls back with the expense on failure.

## 4. Use cases and DI

- [x] 4.1 Replace `AddExpenseUC` (returns sealed `AddExpenseResult`, defaults the date to today from `Clock`, resolves the current household, validates, computes shares with the payer, stores) and `GetExpensesUC` (observes the current household's expenses). Add `AddMemberUC` (sealed result, blank-name error), `GetMembersUC` and `GetSummaryUC(period)` (combines the members and period-expense flows into the aggregator). Verify with `commonTest` tests using fake repositories, including the "no date given" scenario with a fixed clock.
- [x] 4.2 Update `commonModules`: repositories as `single`, use cases as `factory`, `Clock.System` as `single`. Delete the `sources/` package (common, Android and iOS) and the `androidMain` Koin module, and remove `androidx-datastore-preferences` from `shared` and the catalog. Verify that `./gradlew :shared:allTests` passes and that grep finds no `ExpensesSource` or `datastore` references.

## 5. Call sites

- [x] 5.1 Update `SplitzeeApplication` (drop the `androidMain` module) and `MainViewModel` (add the expense with payer = first member, an equal split across all members, category `Other`, the amount entered as minor units, and a log of `Invalid` errors). Verify `./gradlew :composeApp:assembleDebug` succeeds, and that adding an expense in the running app logs it through the new `GetExpensesUC` flow and still lists it after a restart.
- [x] 5.2 Update `iosApp/iosApp/ContentView.swift` `ViewModel` so it compiles against the new API. The add action only logs, with a `// shortcut: add-expense UI lands in the add-expense change` comment. Verify the iOS app builds in Xcode and launches without a Koin resolution crash.

## 6. Integration check

- [x] 6.1 Run `./gradlew :shared:allTests :composeApp:assembleDebug` and `openspec validate expense-domain-and-persistence --strict`. Verify both pass.
