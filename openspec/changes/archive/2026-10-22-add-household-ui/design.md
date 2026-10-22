# Design

## Context

The shared layer is complete for this change: `CreateHouseholdUC` trims and validates the name and the repository selects the new household in the same transaction; `GetCurrentHouseholdUC` and `GetHouseholdsUC` return flows that re-emit on create and select. `UseCaseHelper` exposes all four use cases to Swift, and SKIE is applied to `:shared`, so Swift can `await` suspend functions, iterate flows with `for await`, and `switch` over sealed results.

Neither app is wired to state today. Android's `SplitzeeApp` passes a hardcoded `HomeUiState.Empty`, and iOS's `iOSApp` passes `.empty` to `HomeView`, which does not own its `HomeViewModel`. There is no navigation library on either side.

## Goals / Non-Goals

**Goals:**
- Drive both home screens from the current-household flow so create and select need no navigation code.

**Non-Goals:**
- Navigation libraries, a separate household screen, or new shared code.
- Showing expenses or members on the home screen.

## Decisions

**The current-household flow is the single source of truth for which screen shows.** Both viewmodels collect `GetCurrentHouseholdUC`: `null` maps to a new no-household state, a household maps to the existing home state with its name as title. After a successful create the viewmodel does nothing extra, because the repository selects the new household and the flow re-emits. Alternative: navigate explicitly after create. Rejected because it duplicates state the flow already carries and can disagree with it.

**Extend the existing viewmodels instead of adding household viewmodels.** Each app has one screen; Android's `MainViewModel` gains `CreateHouseholdUC`, `GetHouseholdsUC`, `GetCurrentHouseholdUC` and `SelectHouseholdUC` through Koin in `androidModules.kt`; iOS's `HomeViewModel` takes them from `UseCaseHelper`. Split later if the screen count grows.

**State shape.**
- Android: add `NoHousehold` to `HomeUiState`. Expose `households: List<Household>` and the current household alongside it in `MainViewModel` (one `StateFlow` combining both flows). The form's blank-name error is a nullable field cleared when the name changes or the form closes.
- iOS: add `.noHousehold` to the Swift `HomeUiState` enum. `HomeViewModel` becomes `@MainActor`, publishes `state`, `households`, `current` and `nameError`, and starts collection from a `.task` on `HomeView` so it is cancelled with the view. `iOSApp` creates `HomeView()` without passing a state.

**One form, two presentations.** A single create-household form (name field, create button, error text) is shown full-screen for the no-household state and as a sheet (iOS) or dialog (Android) from "New household". On `CreateHouseholdResult.Invalid` the viewmodel sets the error and the form stays; on `Success` the sheet or dialog closes.

**Title menu.** Android: the `CenterAlignedTopAppBar` title becomes a clickable row with a dropdown arrow that opens a `DropdownMenu`. iOS: the navigation title is replaced by a toolbar `Menu` in the principal position. Each lists households with a checkmark on the selected one, then a divider and "New household". Choosing a household calls `SelectHouseholdUC`.

**Add-expense button with no household.** It stays hidden on the no-household screen, so the silent no-op in `addExpense` is no longer reachable from the UI. The guard stays as is.

## Risks / Trade-offs

- [SKIE mapping of `CreateHouseholdResult` and the flows differs from what is assumed above] → Check the generated Swift API in the first iOS task before writing the view model.
- [No UI or viewmodel test setup exists in `composeApp` or `iosApp`] → The logic in the viewmodels is delegation to tested use cases; verify each task by running the app on an emulator or simulator against the spec scenarios. Adding UI test infrastructure is left for a later change.
- [Flow collection on iOS outlives the view] → Run it in `.task` so SwiftUI cancels it.
