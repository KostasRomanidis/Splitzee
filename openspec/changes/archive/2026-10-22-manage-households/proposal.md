# Proposal

## Why

The app assumes exactly one household per device, created silently on first launch. Users who share costs with different groups (flatmates, a holiday house, a partner) need separate households, and need to manage who belongs to each and remove households they no longer use.

## What Changes

- Users can create any number of households, each by name only (currency stays EUR for now). A new household starts with the device owner "Me" and becomes the selected household.
- Users work inside one selected household at a time and can switch between households. The selection persists across restarts.
- All member, expense and summary data shown is for the selected household only, and observers follow a switch without re-subscribing.
- Users can remove a member from a household. Removal is soft: the member no longer appears in the member list and cannot be used in new expenses, but past expenses and summaries still include them. "Me" cannot be removed.
- Users can delete a household. The selection falls back to the most recently selected remaining household.
- **BREAKING**: No household is created automatically. On first launch, and after the last household is deleted, the app is in a "no household" state until the user creates one. `HouseholdRepo.current()` becomes nullable, and the add-member and add-expense use cases report a no-household result.
- Summaries list removed members who appear in the period's expenses, so per-member balances still add up to the household total.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `household-members`: replaces the single default household with create / switch / delete households and a no-household state; adds member removal; add/list members act on the selected household.
- `expense-summaries`: per-member figures include removed members who have expenses in the period.

## Impact

- `shared` SQLDelight schema: `household` gains `selected_at`; `member` gains an owner marker; new queries for selecting, soft-deleting and listing households and members (including removed ones). No migration: the app is unreleased, so existing dev installs need a reinstall.
- `HouseholdRepo` / `HouseholdRepoImpl`, and the fakes in `commonTest`.
- Use cases: `GetMembersUC`, `GetExpensesUC`, `GetSummaryUC` (follow the selected household), `AddMemberUC`, `AddExpenseUC` (no-household result); new create / select / delete household and remove member use cases, registered in `di/Modules.kt`.
- `summarize` in `rules/SummaryRules.kt`.
- Callers: Android `MainViewModel` and iOS `UseCaseHelper` (expose new use cases). Real household UI screens are out of scope.
