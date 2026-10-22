# Spec Delta

## Purpose

Records shared household expenses (what was bought, how much, which category, when, who paid and how it is split) and keeps them persistently on the device.

## ADDED Requirements

### Requirement: Add expense
The system SHALL let the user add an expense to the current household with a description, a positive amount in the household currency, a category, a date, the paying member and a split rule. On success the expense and its computed per-member shares SHALL be stored together, atomically.

#### Scenario: Valid expense
- **WHEN** the user adds "Groceries" for 45.00, category Groceries, paid by Ana, split equally between Ana and Ben
- **THEN** the expense is stored with Ana's share 22.50 and Ben's share 22.50

#### Scenario: Same description twice
- **WHEN** two expenses both described "Groceries" are added
- **THEN** both are stored as separate expenses

### Requirement: Expense validation
The system MUST reject an expense, store nothing, and report every failed rule when: the description is blank, the amount is zero or negative, the payer is not a member of the household, any split participant is not a member of the household, or the split rule is invalid.

#### Scenario: Blank description and zero amount
- **WHEN** the user adds an expense with a blank description and amount 0
- **THEN** the expense is rejected with both a blank-description error and a non-positive-amount error

#### Scenario: Unknown payer
- **WHEN** the payer is not a member of the current household
- **THEN** the expense is rejected with an unknown-member error

### Requirement: Payer need not share the cost
The payer SHALL NOT be required to be a split participant.

#### Scenario: Paying for others
- **WHEN** Ana pays 20.00 split equally between Ben and Cleo only
- **THEN** the expense is stored with Ben and Cleo each sharing 10.00 and Ana sharing nothing

### Requirement: Expense category
Each expense SHALL have exactly one category: either one of the fixed categories (Groceries, Rent, Utilities, Household, Transport, Dining, Entertainment, Other) or a custom category given as free text on the expense. A custom name SHALL be trimmed, MUST NOT be blank, and SHALL NOT be added to any reusable category list.

#### Scenario: Custom category
- **WHEN** the user adds an expense with custom category " Pet food "
- **THEN** the expense is stored with custom category "Pet food" and the fixed category list is unchanged

#### Scenario: Blank custom category
- **WHEN** the user adds an expense with a custom category that is blank
- **THEN** the expense is rejected with a blank-category error

### Requirement: Expense date
Each expense SHALL carry a calendar date (no time of day) that defaults to the current local date when not provided.

#### Scenario: No date given
- **WHEN** the user adds an expense without a date on 2026-10-19
- **THEN** the expense date is 2026-10-19

### Requirement: Exact money amounts
Amounts and shares SHALL be represented as whole numbers of the currency's minor unit (for example cents), so no rounding error accumulates.

#### Scenario: Cent precision
- **WHEN** an expense of 0.10 and an expense of 0.20 are added
- **THEN** their combined total is exactly 0.30

### Requirement: List expenses
The system SHALL provide the current household's expenses, with their shares, as an observable list ordered by date descending and then by creation time descending, updating when expenses are added.

#### Scenario: Newest first
- **WHEN** expenses dated 2026-10-01 and 2026-10-15 exist
- **THEN** the 2026-10-15 expense is listed first

### Requirement: Expenses persist
Stored households, members and expenses SHALL survive app restarts on both Android and iOS.

#### Scenario: Restart
- **WHEN** an expense is added and the app is restarted
- **THEN** the expense is listed with the same amount, category, date, payer and shares

### Requirement: Sync-ready expense records
Every expense and share record SHALL have a device-generated globally unique identifier, created and updated timestamps, and a deletion marker. Records marked deleted SHALL be excluded from lists and summaries.

#### Scenario: Deleted record excluded
- **WHEN** an expense record is marked deleted
- **THEN** it does not appear in the expense list or in any summary
