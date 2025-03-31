import SwiftUI
import Shared
import KMPNativeCoroutinesAsync
import KMPNativeCoroutinesCore

struct ContentView: View {
    private(set) var viewModel: ViewModel
    
    let phrases = Greeting().greet()
     
    var body: some View {
        VStack {
            Spacer()
            Text(viewModel.greetings).padding()
            Spacer()
            Button(action: {}) {
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

extension ContentView {
    class ViewModel: ObservableObject {
        var greetings: String = "Welcome to Splitzee!"
    }
}
