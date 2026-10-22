# household-members Specification

## Purpose

Defines the selected household that owns all shared expenses on and the members whose costs are split, in local-only mode before user accounts exist.

## Requirements

### Requirement: Add member
The system SHALL let the owner of a household to add a member to the selected household by name. The name SHALL be trimmed and MUST NOT be blank.

#### Scenario: Valid name
- **WHEN** the user adds a member named "  Ana "
- **THEN** a member named "Ana" belongs to the selected household

### Scenario: Name format
- **WHEN** the user adds a member full name eg named "Ana Doe"
- **THEN** a member named "Ana Doe" belongs to the selected household

#### Scenario: Blank name
- **WHEN** the user adds a member with a blank or whitespace-only name
- **THEN** the member is rejected with a blank-name error and nothing is stored

### Requirement: List members
The system SHALL provide the selected household's members, excluding removed members, as an observable list that updates when members are added or removed, ordered by the time they were added.

#### Scenario: Member added while observing
- **WHEN** a member is added while the member list is being observed
- **THEN** the observer receives an updated list including the new member

#### Scenario: Member removed while observing
- **WHEN** a member is removed while the member list is being observed
- **THEN** the observer receives an updated list without that member

### Requirement: Stable record identity
Every household and member SHALL have a globally unique identifier generated on the device, plus created and updated timestamps, so records can later be merged across devices without collisions.

#### Scenario: Two members with the same name
- **WHEN** two members named "Ana" are added
- **THEN** both are stored as distinct members with different identifiers

### Requirement: Create household
The system SHALL let the user create a household by name. The name SHALL be trimmed and MUST NOT be blank. The household SHALL use the currency EUR. A new household SHALL start with one member named "Me" representing the device owner and SHALL become the selected household. Several households with the same name MAY exist.

#### Scenario: Valid name
- **WHEN** the user creates a household named "  Beach house "
- **THEN** a household named "Beach house" with currency EUR and a single member "Me" exists and is the selected household

#### Scenario: Blank name
- **WHEN** the user creates a household with a blank or whitespace-only name
- **THEN** it is rejected with a blank-name error, nothing is stored and the selected household is unchanged

#### Scenario: Several households
- **WHEN** the user creates "Home" and then "Beach house"
- **THEN** both households exist, each with its own "Me" member, and "Beach house" is selected

### Requirement: List households
The system SHALL provide the user's households, excluding deleted ones, as an observable list that updates when households are created or deleted, ordered by the time they were created.

#### Scenario: Household created while observing
- **WHEN** a household is created while the household list is being observed
- **THEN** the observer receives an updated list including the new household

### Requirement: Selected household
At most one household SHALL be selected at a time, and member, expense and summary data SHALL be provided for the selected household only. The user SHALL be able to select any existing household. The selection SHALL survive app restarts.

#### Scenario: Switch household
- **WHEN** "Home" is selected and the user selects "Beach house"
- **THEN** members, expenses and summaries are those of "Beach house" only

#### Scenario: Observers follow a switch
- **WHEN** the member list, expense list or summary is being observed and the user selects another household
- **THEN** the observer receives the newly selected household's data without subscribing again

#### Scenario: Selection persists
- **WHEN** the user selects "Beach house" and the app restarts
- **THEN** "Beach house" is still the selected household

### Requirement: No household state
When no household exists, the system SHALL report that there is no selected household. In this state member, expense and summary data SHALL be empty, and adding a member or an expense SHALL be rejected with a no-household result and store nothing. No household SHALL be created automatically.

#### Scenario: First launch
- **WHEN** the app starts and no household is stored
- **THEN** there is no selected household and no household is created

#### Scenario: Add member without household
- **WHEN** the user cannot add a member while no household exists
- **THEN** it is rejected with a no-household result and nothing is stored

#### Scenario: Add expense without household
- **WHEN** the user adds an expense while no household exists
- **THEN** it is rejected with a no-household result and nothing is stored

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

### Requirement: Remove member
The system SHALL let the user remove a member from the selected household. A removed member SHALL no longer be listed and SHALL NOT be accepted as payer or split participant of new expenses, but SHALL remain on existing expenses and in summaries. The device owner "Me" MUST NOT be removable.

#### Scenario: Remove a member
- **WHEN** the user removes Ana from the selected household
- **THEN** Ana is no longer in the member list

#### Scenario: Removed member in a new expense
- **WHEN** Ana has been removed and the user adds an expense paid by or split with Ana
- **THEN** the expense is rejected because Ana is not a member of the household

#### Scenario: Existing expenses are kept
- **WHEN** Ana paid an expense and is then removed
- **THEN** the expense is still listed with Ana as payer and the household total is unchanged

#### Scenario: Owner cannot be removed
- **WHEN** the user tries to remove "Me"
- **THEN** it is rejected and "Me" stays a member
