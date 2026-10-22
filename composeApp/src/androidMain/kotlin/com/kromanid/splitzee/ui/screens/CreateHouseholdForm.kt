package com.kromanid.splitzee.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kromanid.splitzee.models.HouseholdError

/** Name form for a new household; [onCancel] adds a Cancel button when the form can be dismissed. */
@Composable
fun CreateHouseholdForm(
    title: String,
    nameError: HouseholdError?,
    onNameChange: () -> Unit,
    onCreate: (String) -> Unit,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null,
) {
    var name by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                onNameChange()
            },
            label = { Text("Household name") },
            singleLine = true,
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it.message()) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onCreate(name) }),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("householdNameField"),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            if (onCancel != null) TextButton(onClick = onCancel) { Text("Cancel") }
            Button(onClick = { onCreate(name) }, modifier = Modifier.testTag("createHouseholdButton")) {
                Text("Create")
            }
        }
    }
}

private fun HouseholdError.message() = when (this) {
    HouseholdError.BlankName -> "A name is required"
}

@Preview
@Composable
private fun CreateHouseholdFormPreview() {
    CreateHouseholdForm(
        title = "Create your first household",
        nameError = HouseholdError.BlankName,
        onNameChange = {},
        onCreate = {},
        onCancel = {},
    )
}
