import SwiftUI
import Shared
import KMPNativeCoroutinesAsync
import KMPNativeCoroutinesCore

struct ContentView: View {
    @StateObject private var viewModel: ViewModel = ViewModel()
    @FocusState private var isInputFocused: Bool

    var body: some View {
        VStack {
            Spacer()
            
            Text(viewModel.greetings).padding()
            
            Spacer()
            
            TextField("add Expense", text: $viewModel.input)
                .keyboardType(.decimalPad)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .frame(maxWidth: .infinity)
                .padding()
                .focused($isInputFocused)
            
            Button(action: {viewModel.addExpense()}) {
                Text("Add an expense")
                    .frame(maxWidth: .infinity)
                    .buttonStyle(.borderedProminent)
                    .padding()
                    .cornerRadius(8)
            }.padding()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.white)
        .padding(20)
        .onTapGesture {
            isInputFocused = false
        }
    }
}


class ViewModel: ObservableObject {
    private let addExpenseUC: AddExpenseUC = UseCaseHelper().getAddExpenseUC()
    @Published var input: String = ""
    @Published var greetings: String = "Welcome to Splitzee!"
    
    func addExpense() {
        print("inpt: \(input)")
        addExpenseUC.execute(expense: 10)
    }
}
