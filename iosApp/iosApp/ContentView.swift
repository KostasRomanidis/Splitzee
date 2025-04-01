import SwiftUI
import Shared
import KMPNativeCoroutinesAsync
import KMPNativeCoroutinesCore

struct ContentView: View {
    @ObservedObject var viewModel: ViewModel
    
    var body: some View {
        VStack {
            Spacer()
            Text(viewModel.greetings).padding()
            Spacer()
            Button(action: {viewModel.addExpense()}) {
                Text("Add an expense")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }.padding()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.white)
        .padding(20)
    }
    
}


class ViewModel: ObservableObject {
    private let addExpenseUC: AddExpenseUC = AddExpenseUCImpl()
    
    var greetings: String = "Welcome to Splitzee!"
    
    func addExpense() {
        addExpenseUC.execute(expense: 10)
    }
}
