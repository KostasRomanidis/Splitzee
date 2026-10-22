# Spec Delta

## MODIFIED Requirements

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

## ADDED Requirements

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
