# Spec Delta

## ADDED Requirements

### Requirement: Local user
The device SHALL have one local user id, generated once and kept across app restarts and upgrades. It identifies the user on this device until accounts exist.

#### Scenario: Same id after restart
- **WHEN** the app is restarted
- **THEN** the local user id is the same as before the restart

### Requirement: Household creator
Every household SHALL record the local user id of the user who created it. The system SHALL report, for each household, whether the local user created it. Households that existed before creators were recorded SHALL be attributed to the local user when the app is upgraded.

#### Scenario: New household records its creator
- **WHEN** the user creates "Home"
- **THEN** "Home" is reported as created by the local user

#### Scenario: Existing household after upgrade
- **WHEN** "Home" was created before the upgrade that records creators and the app is upgraded
- **THEN** "Home" is still listed with its members and expenses and is reported as created by the local user

## MODIFIED Requirements

### Requirement: Delete household
The system SHALL let the user delete a household they created. Deleting a household created by another user SHALL be refused and leave it unchanged. A deleted household, with its members and expenses, SHALL no longer be listed, selectable or included in any data. If the deleted household was selected, the most recently selected remaining household SHALL become selected; if none remains, the no-household state applies.

#### Scenario: Delete a household that is not selected
- **WHEN** "Home" is selected and the user deletes "Beach house"
- **THEN** "Beach house" is no longer listed and "Home" stays selected

#### Scenario: Delete the selected household
- **WHEN** the user selected "Flat", then "Home", then "Beach house", and deletes "Beach house"
- **THEN** "Home" becomes the selected household

#### Scenario: Delete the last household
- **WHEN** the user deletes the only remaining household
- **THEN** there is no selected household

#### Scenario: Household created by another user
- **WHEN** "Shared flat" was created by a different user and the local user deletes it
- **THEN** the deletion is refused and "Shared flat" is still listed with the same selection
