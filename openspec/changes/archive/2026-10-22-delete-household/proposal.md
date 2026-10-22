# Proposal

## Why

The shared layer can already delete a household, but neither app lets the user do it, so a household created by mistake stays forever. Only the person who created a household should be able to delete it. Shared households are planned, so the database should record each household's creator now, while every household on a device still belongs to one local user.

## What Changes

- A local user id is generated once per install and stored on the device. It stands in for the user until accounts exist.
- Each household stores `created_by`, the local user id of its creator. New households get the current local user id.
- **Database migration (schema version 1 to 2)**: adds the local user table, generates its id, and adds `created_by` to `household`, filled with that local user id for existing rows.
- The `Household` model exposes whether the local user created it. Deleting a household the local user did not create is refused and changes nothing.
- Android and iOS add a "Delete household" entry to the household menu. It is shown only for a selected household the user created, and it asks for confirmation before deleting.
- Out of scope: blocking deletion when expenses are unsettled (a separate task), user accounts, sharing and sync, and undo.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `household-members`: households record their creator, and only the creator can delete one.
- `household-ui`: the household menu gets a "Delete household" entry with a confirmation step.

## Impact

- Shared database: a new `LocalUser.sq`, `Household.sq` (`created_by` column, a creator flag on the selects, a soft delete limited to the creator), a new migration `1.sqm`, and `sqldelight` migration settings in `shared/build.gradle.kts`.
- Shared Kotlin: the `Household` model, `HouseholdRepo.deleteHousehold` (now returns `Boolean`), `DeleteHouseholdUC`, `Fakes.kt`, `UseCasesTest`, `HouseholdRepoTest`, plus a migration test.
- Android: `MainViewModel`, `di/androidModules.kt`, `ui/screens/HomeScreen.kt`, `ui/SplitzeeApp.kt`, `MainActivity`.
- iOS: `HomeView.swift` (including `HomeViewModel`).
- Existing installs: the migration runs automatically when the app opens through the SQLDelight driver `Schema`.
