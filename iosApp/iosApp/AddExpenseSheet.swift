import SwiftUI

struct AddExpenseSheetStatic: View {
    @Environment(\.dismiss) private var dismiss

    // Static demo values
    private let amountText = "12.00"
//    private let payer: Payer = .personA
    private let equalSplit: Bool = true
    private let descriptionText: String = "Groceries"
    private let date: Date = Date()


    var body: some View {
        NavigationStack {
            Form {
                Section {
                    // Read-only look, no editing yet
                    LabeledContent("Amount") { Text(amountText) }
                    LabeledContent("Paid by") { Text("payer") }
                    LabeledContent("Split equally") { Text(equalSplit ? "Yes" : "No") }
                    LabeledContent("Description") { Text(descriptionText) }
                    LabeledContent("Date") { Text(date.formatted(date: .abbreviated, time: .omitted)) }
                }
            }
            .navigationTitle("Add expense")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}

// 1) Preview the sheet view directly
#Preview("Add Expense Sheet") {
    AddExpenseSheetStatic()
}
