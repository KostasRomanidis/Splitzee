# Design

## Context

Everything in `shared` resolves "the household" through `HouseholdRepo.current()`. It returns the oldest non-deleted household and creates "Home" + "Me" if none exists. Each use case calls it once: the observing ones (`GetMembersUC`, `GetExpensesUC`, `GetSummaryUC`) inside `flow { emitAll(...) }`, so the household is fixed for the lifetime of the subscription. Every table already has `deleted_at`, and every read query filters on it. `summarize` builds one row per member it receives. The SQLDelight database has no migrations, and the app is unreleased. The Android and iOS layers only exercise use cases; there are no real screens yet.

## Goals / Non-Goals

**Goals:**
- Keep "which household" answered in one place, so use cases stay small.
- Observing use cases follow a household switch or deletion without re-subscribing.
- Reuse the existing soft-delete columns instead of adding new state.

**Non-Goals:**
- Household UI (list, switcher, empty state screens), renaming households, per-household currency.
- Cascading `deleted_at` to a deleted household's members and expenses.
- Restoring removed members or deleted households.

## Decisions

### Selection stored as `household.selected_at`
`selectCurrent` orders by `selected_at DESC` instead of `created_at`. Creating a household sets `selected_at = now`, selecting one updates it, and deleting sets `deleted_at`. The fallback after a delete ("most recently selected survivor") and the no-household case then come from the query with no extra code.
*Alternative:* a settings/key-value row holding the selected id. Rejected: it adds a table and a dangling-id case when that household is deleted.

### `current()` becomes `Household?` plus `observeCurrent(): Flow<Household?>`
`current()` stops seeding. `observeCurrent()` is `selectCurrent` as a SQLDelight flow (`asFlow().mapToOneOrNull`), so it re-emits on create, select and delete. The observing use cases become `observeCurrent().flatMapLatest { h -> h?.let { observeX(it.id) } ?: flowOf(empty) }`, where empty is an empty list, or `summarize(emptyList(), emptyList(), emptyList())` for the summary. The one-shot use cases (`AddMemberUC`, `AddExpenseUC`) call `current()` and return a new `NoHousehold` result variant when it is null.
*Alternative:* pass `householdId` into every use case. Rejected: the UI only ever needs the selected household, and it would move the switching logic into both platforms.
`flatMapLatest` needs `@OptIn(ExperimentalCoroutinesApi::class)`.

### Owner marked by a column, not by name
`member` gets `is_owner INTEGER NOT NULL DEFAULT 0`, set to 1 only for the "Me" row inserted by `createHousehold`. Removal is refused when it is set. Matching on the name "Me" would break when the user adds another member called "Me".

### Member removal is a soft delete
`removeMember(householdId, memberId)` sets `deleted_at`/`updated_at` with `WHERE household_id = ? AND id = ? AND is_owner = 0`. Existing behavior then covers the rest: `selectByHousehold` hides the member, and `AddExpenseUC` validates against that list, so a removed payer or participant fails with the existing `UnknownMember` error. The use case returns `Removed` or `CannotRemoveOwner`. An unknown id is a no-op returning `Removed`, since the end state is the same.

### Summary includes removed members who have activity
A new query `selectRemovedByHousehold` (rows with `deleted_at IS NOT NULL`) backs `HouseholdRepo.observeRemovedMembers`. `summarize(members, removed, expenses)` appends each removed member whose id is a payer or share member in `expenses`. Active members keep their row even with no activity, as before. `Member` does not get a removed flag; a "(removed)" label is out of scope.

### Household delete soft-deletes the row only
Members and expenses are only reached through the household id, which is no longer returned by any query, so they are invisible without a cascade.

### Household name validation
`HouseholdError.BlankName` sits next to `MemberError` in `models/Errors.kt`. `CreateHouseholdUC` trims and validates the name, the same way `AddMemberUC` does.

## Risks / Trade-offs

- [Schema edited in place, no migration] → Existing dev installs crash on open; reinstall. Acceptable while unreleased. Add `.sqm` migrations before the first release.
- [`selected_at` written on every switch] → It is an `UPDATE` of one row; negligible.
- [Deleted households keep their members and expenses as rows] → Storage only; a sync design can decide cascade semantics later.
- [**BREAKING** nullable `current()`] → All callers are in `shared` plus `MainViewModel.addExpense`, which already uses `getMembersUC.first()`. With no household that list is empty and `members.first()` throws. The Android test harness must create a household first or guard on the empty list.

## Migration Plan

None beyond reinstalling dev builds (see Risks). No rollback concerns: there is no released data.
