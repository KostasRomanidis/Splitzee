# household-ui Specification

## Purpose

Defines how the Android and iOS apps let the user create households and switch between them, on top of the household data behavior in household-members.

## Requirements

### Requirement: First household prompt
When no household exists, both apps SHALL show a create-household screen with a name field and a create action in place of the home content. No household SHALL be created without the user's action.

#### Scenario: Fresh install
- **WHEN** the app starts and no household is stored
- **THEN** the create-household screen is shown and no household exists

#### Scenario: First household created
- **WHEN** the user enters "Home" on the create-household screen and confirms
- **THEN** the home screen is shown with "Home" as the selected household

### Requirement: Household name validation
The create action SHALL reject a blank or whitespace-only name with a visible error, keep the form open and store nothing. A name with surrounding whitespace SHALL be saved trimmed.

#### Scenario: Blank name
- **WHEN** the user confirms the form with an empty or whitespace-only name
- **THEN** an error says a name is required, the form stays open and no household is created

#### Scenario: Name is trimmed
- **WHEN** the user confirms the form with "  Beach house "
- **THEN** a household named "Beach house" is created

### Requirement: Selected household in the title
While a household is selected, the home screen title SHALL show its name and SHALL update when the selection changes.

#### Scenario: Title follows selection
- **WHEN** "Home" is selected and the user selects "Beach house"
- **THEN** the home screen title shows "Beach house"

### Requirement: Household menu
Tapping the home screen title SHALL open a menu listing every household in creation order, marking the selected one, and ending with a "New household" entry, followed by a "Delete household" entry when the user created the selected household. Choosing a household SHALL select it and close the menu.

#### Scenario: Switch household from the menu
- **WHEN** households "Home" and "Beach house" exist, "Home" is selected, and the user chooses "Beach house" in the menu
- **THEN** the menu closes and "Beach house" is the selected household

#### Scenario: Menu lists households
- **WHEN** the user opens the menu with households "Home" and "Beach house", both created by the user
- **THEN** both are listed in that order, the selected one is marked, "New household" follows them and "Delete household" is the last entry

#### Scenario: Household created by someone else
- **WHEN** the selected household was created by a different user and the user opens the menu
- **THEN** "New household" is the last entry and no "Delete household" entry is shown

### Requirement: Create another household
Choosing "New household" SHALL open the create-household form. Confirming a valid name SHALL create the household, select it and close the form. Cancelling SHALL close the form and leave the households and selection unchanged.

#### Scenario: Add a second household
- **WHEN** "Home" is selected and the user creates "Beach house" from the menu
- **THEN** the form closes, "Beach house" is selected and both households are in the menu

#### Scenario: Cancel
- **WHEN** the user opens "New household" and cancels
- **THEN** the form closes, no household is created and the selection is unchanged

### Requirement: Delete the selected household
Choosing "Delete household" SHALL ask for confirmation with a message that names the selected household and says its members and expenses will be removed, with a destructive confirm action and a cancel action. Confirming SHALL delete the selected household; cancelling SHALL leave the households and the selection unchanged.

#### Scenario: Confirm delete
- **WHEN** "Home" and "Beach house" exist, "Beach house" was selected after "Home", and the user deletes "Beach house" and confirms
- **THEN** "Beach house" is no longer in the menu and the title shows "Home"

#### Scenario: Cancel delete
- **WHEN** the user chooses "Delete household" and cancels the confirmation
- **THEN** no household is deleted and the selection is unchanged

#### Scenario: Delete the last household
- **WHEN** "Home" is the only household and the user deletes it and confirms
- **THEN** the create-household screen is shown
