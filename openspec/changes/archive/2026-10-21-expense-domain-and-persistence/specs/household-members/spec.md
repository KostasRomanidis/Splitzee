# Spec Delta

## Purpose

Defines the household that owns all shared expenses on a device and the members whose costs are split, in local-only mode before user accounts exist.

## ADDED Requirements

### Requirement: Default household exists
The system SHALL ensure exactly one household exists on the device. On first launch it SHALL create a default household with a single currency and one member named "Me" representing the device owner, and SHALL reuse that household on every later launch.

#### Scenario: First launch
- **WHEN** the app starts and no household is stored
- **THEN** one household is created with a single member "Me" and becomes the current household

#### Scenario: Later launch
- **WHEN** the app starts and a household is already stored
- **THEN** the stored household is used and no new household is created

### Requirement: Add member
The system SHALL let the user add a member to the current household by name. The name SHALL be trimmed and MUST NOT be blank.

#### Scenario: Valid name
- **WHEN** the user adds a member named "  Ana "
- **THEN** a member named "Ana" belongs to the current household

#### Scenario: Blank name
- **WHEN** the user adds a member with a blank or whitespace-only name
- **THEN** the member is rejected with a blank-name error and nothing is stored

### Requirement: List members
The system SHALL provide the current household's members as an observable list that updates when members are added, ordered by the time they were added.

#### Scenario: Member added while observing
- **WHEN** a member is added while the member list is being observed
- **THEN** the observer receives an updated list including the new member

### Requirement: Stable record identity
Every household and member SHALL have a globally unique identifier generated on the device, plus created and updated timestamps, so records can later be merged across devices without collisions.

#### Scenario: Two members with the same name
- **WHEN** two members named "Ana" are added
- **THEN** both are stored as distinct members with different identifiers
