package com.kromanid.splitzee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kromanid.splitzee.ui.states.SplitzeeUIState


@Composable
fun SplitzeeApp(
    uiState: SplitzeeUIState,
    onAddAmount: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddExpense: () -> Unit,
) {
    var description by rememberSaveable { mutableStateOf("") }
    var expense by rememberSaveable { mutableStateOf("") }

    MaterialTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
                .padding(all = 20.dp),
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        value = description,
                        onValueChange = {
                            onDescriptionChange.invoke(description)
                            description = it
                        },
                        label = { Text("Description") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        placeholder = { Text("Add expense description") })

                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = expense,
                        onValueChange = {
                            onAddAmount.invoke(expense)
                            expense = it
                        },
                        label = { Text("Expense") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("add expense") })

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        onClick = {
                            onAddExpense.invoke()
                        }
                    ) {
                        Text("Add expense")
                    }
                }

            }
        ) { paddings ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.padding(paddingValues = paddings),
                    text = "Welcome to Splitzee!"
                )
            }
        }
    }
}