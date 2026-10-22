# Proposal

## Why

The shared layer can create, list and select households, but neither app exposes it. On a fresh install there is no household, so adding an expense silently does nothing and the user has no way out. Users need to create their first household and add more later.

## What Changes

- Android and iOS show a create-household screen when no household exists, with a name field and a blank-name error.
- The home screen title shows the selected household and opens a menu that lists all households, switches between them, and offers "New household".
- "New household" opens the same name form; a created household becomes the selected one and the home screen follows it.
- Android `MainViewModel` and iOS `HomeViewModel` call the existing `CreateHouseholdUC`, `GetHouseholdsUC`, `GetCurrentHouseholdUC` and `SelectHouseholdUC`. No shared, repository or database changes.
- Out of scope: deleting or renaming households, member management UI, the expense list.

## Capabilities

### New Capabilities
- `household-ui`: How the apps let the user create a household and switch between households.

### Modified Capabilities
<!-- None: household-members already specifies the data behavior this UI relies on. -->

## Impact

- Android: `MainViewModel`, `di/androidModules.kt`, `MainActivity`, `ui/SplitzeeApp.kt`, `ui/screens/HomeScreen.kt`, `ui/states/HomeUiState.kt`, plus a new create-household composable.
- iOS: `iOSApp.swift`, `HomeView.swift` (including `HomeViewModel` and `HomeUiState`), plus a new create-household view.
- Shared: none; `UseCaseHelper` already exposes the needed use cases.
