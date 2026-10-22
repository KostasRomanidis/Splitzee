# Tasks

## 1. Schema and queries

- [x] 1.1 In `Household.sq` add `selected_at INTEGER NOT NULL` and change `selectCurrent` to order by `selected_at DESC`; add `selectAll` (non-deleted, by `created_at`), `select` (sets `selected_at`/`updated_at`) and `softDelete` (sets `deleted_at`/`updated_at`). Verify `./gradlew :shared:generateCommonMainSplitzeeDatabaseInterface` succeeds
- [x] 1.2 In `Member.sq` add `is_owner INTEGER NOT NULL DEFAULT 0` and an `is_owner` parameter on `insert`; add `softDelete` (`WHERE household_id = ? AND id = ? AND is_owner = 0`) and `selectRemovedByHousehold`. Verify code generation succeeds

## 2. HouseholdRepo

- [x] 2.1 Change `HouseholdRepo`: `current(): Household?` (no seeding), `observeCurrent(): Flow<Household?>`, `observeHouseholds()`, `createHousehold(name)` (inserts the household with `selected_at = now` and an owner "Me" in one transaction), `selectHousehold(id)`, `deleteHousehold(id)`, `removeMember(householdId, memberId): Boolean` (false when owner), `observeRemovedMembers(householdId)`. Verify it compiles
- [x] 2.2 Rewrite `HouseholdRepoTest` for the new behavior: empty DB gives `current() == null`; create makes the household selected with one "Me"; select switches and persists across a new `HouseholdRepoImpl` on the same driver; deleting the selected household falls back to the most recently selected survivor, and deleting the last gives null; `observeCurrent` emits on create/select/delete; remove hides a member from `observeMembers` and shows them in `observeRemovedMembers`; removing the owner returns false. Verify `./gradlew :shared:testDebugUnitTest` passes

## 3. Summary rule

- [x] 3.1 Change `summarize(members, removed, expenses)` to append removed members who are a payer or share member in `expenses`. Add cases to `SummaryRulesTest`: a removed payer keeps their figures and balances sum to zero; a removed member with no expenses is absent. Verify the common tests pass

## 4. Use cases

- [x] 4.1 Update `FakeHouseholdRepo` in `Fakes.kt` to the new interface (nullable selected household held in a `MutableStateFlow`, households list, removed members). Verify the existing `UseCasesTest` compiles
- [x] 4.2 Make `GetMembersUC`, `GetExpensesUC` and `GetSummaryUC` follow `observeCurrent()` with `flatMapLatest`, emitting empty data when null; `GetSummaryUC` also combines `observeRemovedMembers`. Add tests: switching the fake's selected household re-emits the other household's data on the same subscription; null gives empty data. Verify tests pass
- [x] 4.3 Add a `NoHousehold` variant to `AddMemberResult` and `AddExpenseResult`, returned when `current()` is null. Add tests that nothing is stored in that case. Verify tests pass
- [x] 4.4 Add `HouseholdError.BlankName` to `Errors.kt` and the new use cases `CreateHouseholdUC` (trim, reject blank), `SelectHouseholdUC`, `DeleteHouseholdUC`, `GetHouseholdsUC`, `GetCurrentHouseholdUC` (observable) and `RemoveMemberUC` (`Removed` / `CannotRemoveOwner` / `NoHousehold`). Add tests for blank name, trimming and owner removal. Verify tests pass
- [x] 4.5 Register the new use cases in `di/Modules.kt` and expose them in iOS `UseCaseHelper`. Verify `./gradlew :shared:compileKotlinIosSimulatorArm64` and `:composeApp:assembleDebug` succeed

## 5. App callers

- [x] 5.1 In `MainViewModel.addExpense`, guard against an empty member list / `NoHousehold` result so it no longer throws with no household (create a household first, or log and return). Verify the Android app launches on a fresh install without crashing

## Workflow follow-up

- Reinstall dev builds after this lands: the schema changed without a migration.
- Archive the change once reviewed.
