package com.kromanid.splitzee.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onAmountChange: (Int) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onEqualSplitToggle: (Boolean) -> Unit,
    onDateChange: (Long) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
//    val ui by vm.ui.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current
//    val dateLabel = remember(ui.dateMillis) {
//        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(ui.dateMillis))
//    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add expense") },
                navigationIcon = {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                },
                actions = {
//                    val enabled = ui.isValid && !ui.isSaving
                    TextButton(
                        enabled = true, //TODO add logic to enable button
                        onClick = {
                            focus.clearFocus()
                            onSave.invoke()
                        }
                    ) { Text("Save") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = "ui.amount",
                onValueChange = { onAmountChange.invoke(10) },
                label = { Text(text = "Amount") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                supportingText = {
                    Text("Enter a valid amount > 0") //TODO add validation logic for hint
                }
            )

            Text(text = "Paid by", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                //TODO Add logic for selecting user
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text("Person A") }
                )
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text("Person B") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = true,
                    onCheckedChange = { onEqualSplitToggle.invoke(true) }) //TODO split equally logic
                Spacer(Modifier.width(8.dp))
                Text("Split equally")
            }

            OutlinedTextField(
                value = "ui.description",
                onValueChange = { onDescriptionChange.invoke("description") }, //TODO add description logic
                label = { Text(text = "Description (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Simple date selector (replace with Material DatePicker if you like)
            ListItem(
                headlineContent = { Text("Date") },
                supportingContent = { Text("dateLabel") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Replace with your own date picker; for now set to "today"
                        onDateChange(System.currentTimeMillis())
                    }
            )
        }
    }
}

@Composable
@Preview
fun AddExpenseScreenPreview() {
    AddExpenseScreen({}, {}, {}, {}, {}, {})
}