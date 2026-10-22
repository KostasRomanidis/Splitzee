# Tasks

## 1. Android viewmodel

- [x] 1.1 Inject `CreateHouseholdUC`, `GetHouseholdsUC`, `GetCurrentHouseholdUC` and `SelectHouseholdUC` into `MainViewModel` and register them in `di/androidModules.kt`. Verify `./gradlew :composeApp:assembleDebug` succeeds and the app launches without a Koin error
- [x] 1.2 Add `NoHousehold` to `HomeUiState`; in `MainViewModel` combine the current-household and households flows into one exposed state (home state, title, households, selected id), and add `createHousehold(name)` (sets a nullable blank-name error on `Invalid`, clears it and closes the dialog on `Success`), `selectHousehold(id)` and dialog open/close. Verify it compiles with `./gradlew :composeApp:assembleDebug`

## 2. Android UI

- [x] 2.1 Add a create-household composable (name field, create button, error text) and show it full screen from `HomeScreen` for `NoHousehold`, with the add-expense FAB hidden. Collect the viewmodel state in `MainActivity`/`SplitzeeApp` instead of the hardcoded `Empty`. Verify on an emulator with a fresh install: the form shows, a blank name shows the error, and "Home" leads to the home screen titled "Home"
- [x] 2.2 Make the top bar title open a `DropdownMenu` listing households with a check on the selected one, a divider and "New household", which opens the form in a dialog with Cancel. Verify on an emulator the "Household menu" and "Create another household" scenarios in `specs/household-ui/spec.md`, including that the selection survives an app restart

## 3. iOS viewmodel

- [x] 3.1 Check the SKIE-generated Swift API for `CreateHouseholdUC.execute`, `CreateHouseholdResult` and the two household flows (Xcode jump-to-definition on `Shared`), and note any naming difference from `design.md` in this task. Verify `./gradlew :shared:compileKotlinIosSimulatorArm64` succeeds
  - Finding: the `Household` model is `Household_` in Swift; `Household` is the SQLDelight table class. `CreateHouseholdUC.execute(name:)` and `SelectHouseholdUC.execute(id:)` are `async throws`; switch on the result with `onEnum(of:)`. `GetHouseholdsUC.execute()` is a flow of `[Household_]` and `GetCurrentHouseholdUC.execute()` an optional flow of `Household_`, both iterable with `for await`.
- [x] 3.2 Make `HomeViewModel` `@MainActor`, take the four use cases from `UseCaseHelper`, publish `state` (adding `.noHousehold` to `HomeUiState`), `households`, `current` and `nameError`, and add `observe()` (iterates both flows), `createHousehold(name:)` and `selectHousehold(id:)`. Remove the unused `input`/`greetings`. Verify the iOS app builds in Xcode

## 4. iOS UI

- [x] 4.1 Add a create-household view (name field, create button, error text). `HomeView` owns `@StateObject HomeViewModel`, runs `observe()` in `.task`, and shows the form for `.noHousehold` with the add button hidden; `iOSApp` creates `HomeView()` without a state. Keep a `#Preview` building. Verify on a simulator with a fresh install: the form shows, a blank name shows the error, and "Home" leads to the home screen titled "Home"
- [ ] 4.2 Add a principal toolbar `Menu` showing the selected household's name, listing households with a checkmark, a divider and "New household", which presents the form as a sheet with Cancel. Verify on a simulator the "Household menu" and "Create another household" scenarios in `specs/household-ui/spec.md`, including that the selection survives an app restart

## 5. Integration check

- [ ] 5.1 Run `openspec validate add-household-ui --strict` and `./gradlew :shared:allTests :composeApp:assembleDebug`, and verify both pass

## Workflow follow-up

- Archive the change once reviewed.
