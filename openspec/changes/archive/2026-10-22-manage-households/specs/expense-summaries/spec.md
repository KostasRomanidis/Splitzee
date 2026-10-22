# Spec Delta

## MODIFIED Requirements

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
