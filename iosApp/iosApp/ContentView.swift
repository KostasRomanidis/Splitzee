import SwiftUI
import Shared
import KMPNativeCoroutinesAsync
import KMPNativeCoroutinesCore

struct ContentView: View {
    private(set) var viewModel: ViewModel
    
    let phrases = Greeting().greet()
     
    var body: some View {
        ZStack {
            Text(viewModel.greetings)
        }
    }
}

extension ContentView {
    class ViewModel: ObservableObject {
        var greetings: String = "Welcome to Splitzee!"
    }
}
