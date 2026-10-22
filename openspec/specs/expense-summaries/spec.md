# expense-summaries Specification

## Purpose

Answers how much the household spent, on what, and where each member stands (paid, consumed, owed) over all time, a given month or a given year.

## Requirements

### Requirement: Summary period
The system SHALL compute a summary for one of these periods: all time, a calendar month (year and month), or a calendar year. An expense SHALL belong to a period when its date falls inside it.

#### Scenario: Month boundary
- **WHEN** expenses are dated 2026-09-30 and 2026-10-01 and the summary is for October 2026
- **THEN** only the 2026-10-01 expense is included

#### Scenario: Year
- **WHEN** expenses are dated 2025-12-31 and 2026-01-01 and the summary is for 2026
- **THEN** only the 2026-01-01 expense is included

### Requirement: Household total
The summary SHALL include the sum of the amounts of all expenses in the period.

#### Scenario: Total
- **WHEN** the period contains expenses of 45.00 and 12.50
- **THEN** the household total is 57.50

#### Scenario: Empty period
- **WHEN** the period contains no expenses
- **THEN** the household total is 0.00 and every member's paid, share and balance are 0.00

### Requirement: Total by category
The summary SHALL include the total per category for the period. Custom categories whose names match ignoring case and surrounding whitespace SHALL be grouped together.

#### Scenario: Grouping custom names
- **WHEN** the period has a 10.00 expense in custom "Pet food" and a 5.00 expense in custom "pet food"
- **THEN** the summary has a single custom category total of 15.00

### Requirement: Per-member figures
For every household member, and for every removed member who paid or has a share in an expense of the period, the summary SHALL include: paid (sum of amounts of expenses they paid), share (sum of their split shares), and balance (paid minus share). A positive balance SHALL mean the member is owed money and a negative balance that they owe.

#### Scenario: Paid and share
- **WHEN** Ana pays 45.00 split equally between Ana and Ben
- **THEN** Ana has paid 45.00, share 22.50, balance +22.50, and Ben has paid 0.00, share 22.50, balance -22.50

#### Scenario: Balances net to zero
- **WHEN** any set of valid expenses is summarized
- **THEN** the sum of all members' balances is exactly 0.00

#### Scenario: Removed member with expenses in the period
- **WHEN** Ana pays 45.00 split equally between Ana and Ben, and Ana is then removed
- **THEN** the summary still shows Ana with paid 45.00, share 22.50, balance +22.50, and all balances still sum to 0.00

#### Scenario: Removed member without expenses in the period
- **WHEN** Ana has been removed and has no expenses in the summarized period
- **THEN** Ana does not appear in the summary

### Requirement: Observable summary
The system SHALL provide the summary for a chosen period as an observable value that updates when expenses or members change.

#### Scenario: New expense updates summary
- **WHEN** an expense dated inside the observed period is added
- **THEN** the observer receives a summary whose total includes it
