import SwiftUI
import Shared

struct ExpenseUI: Identifiable, Equatable {
    let id: String
    let title: String
    let amount: Decimal
}

enum HomeUiState: Equatable {
    case loading
    case noHousehold
    case empty
    case content
}

struct HomeView: View {
    @StateObject private var viewModel = HomeViewModel()

    var body: some View {
        NavigationStack {
            content.navigationTitle("Splitzee")
                .navigationBarTitleDisplayMode(viewModel.current == nil ? .large : .inline)
                .toolbar {
                    if let current = viewModel.current {
                        ToolbarItem(placement: .principal) {
                            householdMenu(current: current)
                        }
                    }
                    if viewModel.state != .noHousehold {
                        ToolbarItem(placement: .primaryAction) {
                            Button(action: {

                            }) {
                                Image(systemName: "plus")
                            }.accessibilityIdentifier("addExpenseButton")
                                .accessibilityLabel("Add Expense")
                        }
                    }
                }
        }.accessibilityIdentifier("homeScreen")
            .task { await viewModel.observe() }
            .sheet(isPresented: $viewModel.showCreateSheet, onDismiss: { viewModel.nameError = nil }) {
                NavigationStack {
                    CreateHouseholdForm(
                        title: "Name",
                        nameError: viewModel.nameError,
                        onNameChange: { viewModel.nameError = nil },
                        onCreate: { name in Task { await viewModel.createHousehold(name: name) } }
                    )
                    .navigationTitle("New household")
                    .navigationBarTitleDisplayMode(.inline)
                    .toolbar {
                        ToolbarItem(placement: .cancellationAction) {
                            Button("Cancel") { viewModel.showCreateSheet = false }
                        }
                    }
                }
            }
    }

    private func householdMenu(current: Household_) -> some View {
        Menu {
            Picker("Household", selection: Binding(
                get: { current.id },
                set: { id in Task { await viewModel.selectHousehold(id: id) } }
            )) {
                ForEach(viewModel.households, id: \.id) { household in
                    Text(household.name).tag(household.id)
                }
            }
            Divider()
            Button {
                viewModel.showCreateSheet = true
            } label: {
                Label("New household", systemImage: "plus")
            }
        } label: {
            HStack(spacing: 4) {
                Text(current.name).font(.headline)
                Image(systemName: "chevron.down").font(.caption.weight(.semibold))
            }
            .foregroundStyle(.primary)
        }
        .accessibilityIdentifier("householdMenu")
        .accessibilityLabel("Switch household, \(current.name) selected")
    }

    @ViewBuilder
    private var content: some View {
        switch viewModel.state {
        case .loading:
            VStack(spacing: 12) {
                ProgressView()
                    .accessibilityIdentifier("loadingIndicator")
                Text("Loading…")
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        case .noHousehold:
            CreateHouseholdForm(
                title: "Create your first household",
                nameError: viewModel.nameError,
                onNameChange: { viewModel.nameError = nil },
                onCreate: { name in Task { await viewModel.createHousehold(name: name) } }
            )
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


@MainActor
class HomeViewModel: ObservableObject {
    private let addExpenseUC: AddExpenseUC
    private let createHouseholdUC: CreateHouseholdUC
    private let getHouseholdsUC: GetHouseholdsUC
    private let getCurrentHouseholdUC: GetCurrentHouseholdUC
    private let selectHouseholdUC: SelectHouseholdUC

    @Published private(set) var state: HomeUiState = .loading
    @Published private(set) var households: [Household_] = []
    @Published private(set) var current: Household_? = nil
    @Published var nameError: String? = nil
    @Published var showCreateSheet = false

    init(helper: UseCaseHelper = UseCaseHelper()) {
        addExpenseUC = helper.getAddExpenseUC()
        createHouseholdUC = helper.getCreateHouseholdUC()
        getHouseholdsUC = helper.getHouseholdsUC()
        getCurrentHouseholdUC = helper.getCurrentHouseholdUC()
        selectHouseholdUC = helper.getSelectHouseholdUC()
    }

    /// Collects the household flows until the calling task is cancelled.
    func observe() async {
        async let currentLoop: Void = observeCurrent()
        async let householdsLoop: Void = observeHouseholds()
        _ = await (currentLoop, householdsLoop)
    }

    private func observeCurrent() async {
        for await household in getCurrentHouseholdUC.execute() {
            current = household
            state = household == nil ? .noHousehold : .empty
        }
    }

    private func observeHouseholds() async {
        for await list in getHouseholdsUC.execute() {
            households = list
        }
    }

    /// Returns true when the household was created; the new household is selected by the repo.
    func createHousehold(name: String) async -> Bool {
        do {
            switch onEnum(of: try await createHouseholdUC.execute(name: name)) {
            case .invalid(let invalid):
                switch onEnum(of: invalid.error) {
                case .blankName: nameError = "A name is required"
                }
                return false
            case .success:
                nameError = nil
                showCreateSheet = false
                return true
            }
        } catch {
            print("Create household failed: \(error)")
            return false
        }
    }

    func selectHousehold(id: String) async {
        do {
            try await selectHouseholdUC.execute(id: id)
        } catch {
            print("Select household failed: \(error)")
        }
    }

    func addExpense() {
        // shortcut: add-expense UI lands in the add-expense change
    }
}

#Preview {
    EmptyExpensesView()
}
