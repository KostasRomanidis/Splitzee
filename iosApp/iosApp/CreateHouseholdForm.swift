import SwiftUI

/// Name form for a new household, shown on first launch and from "New household".
struct CreateHouseholdForm: View {
    let title: String
    let nameError: String?
    let onNameChange: () -> Void
    let onCreate: (String) -> Void

    @State private var name = ""

    var body: some View {
        Form {
            Section {
                TextField("Household name", text: $name)
                    .textInputAutocapitalization(.sentences)
                    .submitLabel(.done)
                    .onSubmit { onCreate(name) }
                    .onChange(of: name) { _ in onNameChange() }
                    .accessibilityIdentifier("householdNameField")
            } header: {
                Text(title)
            } footer: {
                if let nameError {
                    Text(nameError)
                        .foregroundStyle(.red)
                        .accessibilityIdentifier("householdNameError")
                }
            }
            Button("Create") { onCreate(name) }
                .accessibilityIdentifier("createHouseholdButton")
        }
    }
}

#Preview {
    CreateHouseholdForm(
        title: "Create your first household",
        nameError: "A name is required",
        onNameChange: {},
        onCreate: { _ in }
    )
}
