# Spec Delta

## Purpose

Turns an expense's split rule into exact per-member shares: divided equally among selected members, assigned as exact amounts, or assigned as percentages. Shares always add up to the whole amount.

## ADDED Requirements

### Requirement: Equal split
For an equal split the system SHALL give every participant the expense amount divided by the number of participants, rounded down to the minor unit, and then add the leftover minor units to one participant (see Remainder absorption). Shares MUST sum exactly to the amount.

#### Scenario: Divides evenly
- **WHEN** 30.00 paid by Ana is split equally between Ana, Ben and Cleo
- **THEN** each share is 10.00

#### Scenario: Single participant owes everything
- **WHEN** Ana pays 40.00 split equally with Ben as the only participant
- **THEN** Ben's share is 40.00 and Ana has no share

### Requirement: Remainder absorption
Leftover minor units from rounding SHALL all go to the payer when the payer is a participant. Otherwise they SHALL all go to the first participant selected. Every other participant's share SHALL be the rounded-down amount, so the participants who did not pay owe identical amounts in an equal split.

#### Scenario: Payer absorbs the leftover cent
- **WHEN** 10.00 paid by Ana is split equally between Ben, Ana and Cleo, selected in that order
- **THEN** the shares are Ben 3.33, Ana 3.34, Cleo 3.33

#### Scenario: Payer absorbs several leftover cents
- **WHEN** 0.05 paid by Ana is split equally between Ana, Ben and Cleo
- **THEN** the shares are Ana 0.03, Ben 0.01, Cleo 0.01

#### Scenario: Payer not a participant
- **WHEN** 10.00 paid by Ana is split equally between Ben and Cleo and Dan, selected in that order
- **THEN** the shares are Ben 3.34, Cleo 3.33, Dan 3.33

### Requirement: Exact amounts split
For an exact amounts split the system SHALL use the amount given for each selected member as that member's share. The shares MUST sum exactly to the expense amount, and each share MUST be greater than zero.

#### Scenario: Valid exact split
- **WHEN** a 50.00 expense is split as Ana 30.00 and Ben 20.00
- **THEN** the shares are Ana 30.00 and Ben 20.00

#### Scenario: Shares do not add up
- **WHEN** a 50.00 expense is split as Ana 30.00 and Ben 15.00
- **THEN** the split is rejected with a sum-mismatch error stating the 5.00 difference

#### Scenario: Zero share
- **WHEN** an exact split gives a member 0.00
- **THEN** the split is rejected with a non-positive-share error

### Requirement: Percentage split
For a percentage split the system SHALL take a percentage for each selected member with at most two decimal places. The percentages MUST sum to exactly 100 and each MUST be greater than zero. Each share SHALL be the amount times its percentage, rounded down to the minor unit, with the leftover handled by Remainder absorption.

#### Scenario: Percentages divide cleanly
- **WHEN** 100.00 paid by Ana is split as Ana 50%, Ben 30%, Cleo 20%
- **THEN** the shares are Ana 50.00, Ben 30.00, Cleo 20.00

#### Scenario: Percentages with a leftover
- **WHEN** 10.00 paid by Cleo is split as Ana 33.33%, Ben 33.33%, Cleo 33.34%
- **THEN** the shares are Ana 3.33, Ben 3.33, Cleo 3.34

#### Scenario: Percentages do not add up
- **WHEN** a split gives Ana 60% and Ben 30%
- **THEN** the split is rejected with a percent-sum error stating the 10% difference

#### Scenario: Zero percentage
- **WHEN** a percentage split gives a member 0%
- **THEN** the split is rejected with a non-positive-share error

### Requirement: Valid participants
A split MUST have at least one participant and MUST NOT list the same member twice.

#### Scenario: No participants
- **WHEN** an expense is split with no participants
- **THEN** the split is rejected with a no-participants error

#### Scenario: Duplicate participant
- **WHEN** an equal split lists Ana twice
- **THEN** the split is rejected with a duplicate-participant error
