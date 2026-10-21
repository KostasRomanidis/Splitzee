package com.kromanid.splitzee.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kromanid.splitzee.ui.screens.HomeScreen
import com.kromanid.splitzee.ui.states.HomeUiState


@Composable
fun SplitzeeApp(onAddExpense: () -> Unit = {}) {
    MaterialTheme {
        HomeScreen(uiState = HomeUiState.Empty, addExpense = onAddExpense)
    }
}

@Preview
@Composable
private fun SplitzeeAppPreview() {
    SplitzeeApp()
}