# Design

## Context

- `HouseholdRepoImpl.deleteHousehold` already soft-deletes and falls back to the most recently selected household. `DeleteHouseholdUC` is registered in Koin and exposed to iOS through `UseCaseHelper.getDeleteHouseholdUC()`, but neither app calls it.
- The database is at schema version 1 with no `.sqm` files. Android (`AndroidSqliteDriver`) and iOS (`NativeSqliteDriver`) both receive `SplitzeeDatabase.Schema`, so the drivers run any migrations when the database opens.
- There are no user accounts. Each household has an owner member "Me" (`member.is_owner`), which identifies a member inside one household rather than a person.
- `Household` is mapped straight from SQL with `::Household` in `selectCurrent` and `selectAll`.

## Goals / Non-Goals

**Goals:**
- Record a creator id that can later map to an account id, without adding accounts now.
- Enforce "only the creator deletes" in the shared layer, so the UI does not have to.
- Upgrade existing installs with no data loss.

**Non-Goals:**
- Linking members to users, or replacing the "Me" owner member.
- Checking for unsettled expenses before delete. That will be a separate guard inside `DeleteHouseholdUC`.

## Decisions

**1. A one-row `local_user` table, created by SQL.**
`LocalUser.sq` declares `CREATE TABLE local_user (id TEXT NOT NULL PRIMARY KEY)` and an unlabeled `INSERT INTO local_user(id) VALUES (lower(hex(randomblob(16))));`, which SQLDelight runs during `Schema.create`. The migration runs the same two statements, so fresh installs and upgraded installs go through the same path and no Kotlin bootstrap code is needed.
- *Alternative: generating the id in Kotlin on first launch.* That needs a "create if missing" call before any household query, plus a place to call it from on both platforms. Rejected.
- *Alternative: platform storage (SharedPreferences or UserDefaults).* The id would sit outside the database the migration has to backfill from. Rejected.
- The id is 32 hex characters, not a dashed UUID. It is an opaque value, so the format doesn't matter.

**2. `household.created_by TEXT NOT NULL`, filled from `local_user` in SQL.**
`insert` uses `(SELECT id FROM local_user)` for `created_by`, so `HouseholdRepoImpl.createHousehold` keeps its current arguments. Creating a household never writes a creator id other than the local user's.

**3. Migration `1.sqm`.**
```sql
CREATE TABLE local_user (id TEXT NOT NULL PRIMARY KEY);
INSERT INTO local_user(id) VALUES (lower(hex(randomblob(16))));
ALTER TABLE household ADD COLUMN created_by TEXT NOT NULL DEFAULT '';
UPDATE household SET created_by = (SELECT id FROM local_user);
```
SQLite needs a default to add a `NOT NULL` column, so `Household.sq` declares `created_by TEXT NOT NULL DEFAULT ''` too. That way fresh installs and migrated installs end up with the same schema. Before editing any `.sq` file, the version 1 schema is generated into `src/commonMain/sqldelight/databases/1.db` with `schemaOutputDirectory`, and `verifyMigrations` is set to true. The SQLDelight Gradle check then fails if `1.db` plus `1.sqm` differs from the new `.sq` schema.

**4. The creator check runs in SQL, and `deleteHousehold` returns `Boolean`.**
`softDelete` adds `AND created_by = (SELECT id FROM local_user)`. The repo returns `rowsAffected > 0`, the same way `removeMember` returns `false` for the owner. `DeleteHouseholdUC.execute` returns that `Boolean`. A typed result like `CreateHouseholdResult` isn't needed yet; the unsettled-expenses task can turn it into a sealed result when there is a second reason to refuse.

**5. `Household` gains `isCreator: Boolean`, not `createdBy: String`.**
`selectCurrent` and `selectAll` select `created_by = (SELECT id FROM local_user) AS is_creator`. The UI only needs to know "can I delete this", and with this flag it never has to load or compare the local user id. A raw `createdBy` can be added when sharing needs it. If SQLDelight types the comparison as `Long` rather than `Boolean`, map with a lambda instead of `::Household`.

**6. The UI shows the delete entry only when `selected.isCreator`, and always confirms first.**
- Android: `HouseholdUiState` gets `showDeleteDialog`. `HouseholdMenu` adds a `DropdownMenuItem("Delete household")` in the error color after "New household". An `AlertDialog` names the household and has Delete and Cancel buttons. `MainViewModel.deleteHousehold()` calls the use case with `selected.id`.
- iOS: a `Button(role: .destructive)` after "New household" sets `showDeleteConfirmation`. A `.confirmationDialog` (or `.alert`) shows the same text and calls `HomeViewModel.deleteHousehold()`.
- The rest happens through the existing current-household and households flows: the title, the menu and the no-household screen update by themselves.

## Risks / Trade-offs

- [The `DEFAULT ''` on `created_by` lets a row without a creator exist if some future insert skips the column] → `insert` is the only write path and always sets the column. The repo test checks that a new household is `isCreator`.
- [Reinstalling the app creates a new local user id] → All data is deleted along with it, so no orphaned households are left. Sync will have to handle linking the local id to an account.
- [`verifyMigrations` needs `1.db` to be generated *before* the schema edits] → This is the first task. If it's skipped, the migration test below still checks the data path.
- [SKIE maps the `Boolean` returned from a suspend function to `KotlinBoolean` in Swift] → The iOS view model can ignore the result, because a refused delete leaves the flows unchanged.

## Migration Plan

1. Generate `1.db` from the current schema, then add `1.sqm` and the `.sq` edits.
2. A JVM test copies `1.db` to a temporary file, inserts a household and a member as version 1 rows, runs `SplitzeeDatabase.Schema.migrate(driver, 1, 2)`, and checks that the household is still listed with `isCreator = true` and can be deleted.
3. Rollback: SQLDelight has no down-migrations. An app older than this change can't open a version 2 database, which is acceptable before release.
