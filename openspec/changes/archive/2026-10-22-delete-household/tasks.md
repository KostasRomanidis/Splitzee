# Tasks

## 1. Schema and migration

- [x] 1.1 Before editing any `.sq` file, set `schemaOutputDirectory` to `src/commonMain/sqldelight/databases` and `verifyMigrations` to true for `SplitzeeDatabase` in `shared/build.gradle.kts`, then run the SQLDelight generate-schema task (find its exact name with `./gradlew :shared:tasks --all | grep -i schema`). Verify `1.db` exists and contains the current `household` table with no `created_by` column
- [x] 1.2 Add `LocalUser.sq` (table and unlabeled insert), add `created_by TEXT NOT NULL DEFAULT ''` to `household` in `Household.sq`, set it with `(SELECT id FROM local_user)` in `insert`, select `created_by = (SELECT id FROM local_user) AS is_creator` in `selectCurrent` and `selectAll`, and limit `softDelete` to the creator. Add `1.sqm` as in `design.md`. Verify `./gradlew :shared:verifyCommonMainSplitzeeDatabaseMigration` (or the verify task listed by `:shared:tasks --all`) passes

## 2. Shared model, repository and use case

- [x] 2.1 Add `isCreator: Boolean` to `Household`, change `HouseholdRepo.deleteHousehold` and `DeleteHouseholdUC.execute` to return `Boolean` (true when deleted), and update `Fakes.kt` (households created through the fake are creators; deleting a non-creator returns false) and every `Household(...)` constructor call. Verify `./gradlew :shared:compileKotlinMetadata` succeeds
- [x] 2.2 In `HouseholdRepoTest`: a new household is `isCreator`, the local user id stays the same across two `HouseholdRepoImpl` instances on the same driver, and a household whose `created_by` was changed directly in SQL to another id is not `isCreator`, cannot be deleted (returns false) and stays listed and selected. In `UseCasesTest`: `DeleteHouseholdUC` returns false for a non-creator and changes nothing. Verify `./gradlew :shared:testDebugUnitTest` passes
- [x] 2.3 Add a migration test in `shared/src/androidUnitTest`: copy `1.db` to a temporary file, open it with `JdbcSqliteDriver`, insert a household and a "Me" member, run `SplitzeeDatabase.Schema.migrate(driver, 1, 2)`, and check that the household is listed with its member and `isCreator = true`, and that deleting it returns true. Verify `./gradlew :shared:testDebugUnitTest` passes

## 3. Android

- [x] 3.1 Inject `DeleteHouseholdUC` into `MainViewModel` (register it in `di/androidModules.kt`), add `showDeleteDialog` to `HouseholdUiState` with open and close functions, and add `deleteHousehold()`, which deletes `selected` and closes the dialog. Verify `./gradlew :composeApp:assembleDebug` succeeds and the app launches without a Koin error
- [x] 3.2 In `HomeScreen.HouseholdMenu`, add a "Delete household" item in the error color after "New household", shown only when `selected.isCreator`. Add an `AlertDialog` that names the household and says its members and expenses will be removed, with Delete and Cancel buttons, and pass the new callbacks through `SplitzeeApp` and `MainActivity`. Verify on an emulator the "Household menu" and "Delete the selected household" scenarios in `specs/household-ui/spec.md`, including deleting the last household

## 4. iOS

- [x] 4.1 In `HomeViewModel`, take `getDeleteHouseholdUC()` from `UseCaseHelper`, publish `showDeleteConfirmation`, and add `deleteHousehold()`, which deletes `current` and logs failures like `selectHousehold` does. Check the SKIE names for `isCreator` and the `Boolean` result in Xcode. Verify `./gradlew :shared:compileKotlinIosSimulatorArm64` succeeds and the iOS app builds
- [x] 4.2 In `HomeView.householdMenu`, add `Button(role: .destructive) { ... } label: { Label("Delete household", systemImage: "trash") }` after "New household", shown only when `current.isCreator`. Add a confirmation dialog with the same message and a destructive Delete button. Verify on a simulator the "Household menu" and "Delete the selected household" scenarios in `specs/household-ui/spec.md`, including deleting the last household

## 5. Integration check

- [x] 5.1 On a simulator or emulator, install the build from `main` (schema version 1), create a household, then install this branch over it. Verify the household is still selected and can be deleted. Run `openspec validate delete-household --strict` and `./gradlew :shared:allTests :composeApp:assembleDebug`, and verify both pass

## Workflow follow-up

- Archive the change once reviewed.
- The unsettled-expenses guard is a separate change in `DeleteHouseholdUC`.
