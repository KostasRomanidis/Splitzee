import SwiftUI
import Shared
import KMPNativeCoroutinesAsync
import KMPNativeCoroutinesCore

struct ExpenseUI: Identifiable, Equatable {
    let id: String
    let title: String
    let amount: Decimal
}

enum HomeUiState: Equatable {
    case loading
    case empty
    case content
}

struct HomeView: View {
    let state: HomeUiState
    
    var body: some View {
        NavigationStack {
            content.navigationTitle("Splitzee")
                .toolbar {
                    ToolbarItem(placement: .primaryAction) {
                        Button(action: {
                            
                        }) {
                            Image(systemName: "plus")
                        }.accessibilityIdentifier("addExpenseButton")
                            .accessibilityLabel("Add Expense")
                    }
                }
        }.accessibilityIdentifier("homeScreen")
    }
    
    @ViewBuilder
    private var content: some View {
        switch state {
        case .loading:
            VStack(spacing: 12) {
                ProgressView()
                    .accessibilityIdentifier("loadingIndicator")
                Text("Loading…")
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        case .empty:
            EmptyExpensesView()
                .accessibilityIdentifier("emptyState")
        case .content:
            EmptyExpensesView()
                .accessibilityIdentifier("emptyState")
        }
    }
}


private struct EmptyExpensesView: View {
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "list.bullet.rectangle.portrait")
                .font(.system(size: 48, weight: .regular))
                .foregroundStyle(.secondary)
            
            Text("No expenses yet")
                .font(.title3).bold()
            
            Text("When you add your first expense, it will show up here.\nTap the + to get started.")
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)
        }
        .padding(.horizontal, 24)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("No expenses yet. When you add your first expense, it will show up here. Tap the plus button to get started.")
    }
}


class HomeViewModel: ObservableObject {
    private let addExpenseUC: AddExpenseUC = UseCaseHelper().getAddExpenseUC()
    @Published var input: String = ""
    @Published var greetings: String = "Welcome to Splitzee!"
    
    func addExpense() {
        print("inpt: \(input)")
        //        addExpenseUC.execute(expense: 10, description: "")
    }
}

#Preview {
    HomeView(
        state: .empty
    )
}
