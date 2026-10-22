package com.kromanid.splitzee.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kromanid.splitzee.ui.screens.HomeScreen
import com.kromanid.splitzee.ui.states.HomeUiState
import com.kromanid.splitzee.ui.states.HouseholdUiState


@Composable
fun SplitzeeApp(
    state: HouseholdUiState,
    onAddExpense: () -> Unit = {},
    onNameChange: () -> Unit = {},
    onCreateHousehold: (String) -> Unit = {},
    onSelectHousehold: (String) -> Unit = {},
    onNewHousehold: () -> Unit = {},
    onCancelCreate: () -> Unit = {},
    onDeleteHousehold: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onCancelDelete: () -> Unit = {},
) {
    MaterialTheme {
        HomeScreen(
            state = state,
            addExpense = onAddExpense,
            onNameChange = onNameChange,
            onCreateHousehold = onCreateHousehold,
            onSelectHousehold = onSelectHousehold,
            onNewHousehold = onNewHousehold,
            onCancelCreate = onCancelCreate,
            onDeleteHousehold = onDeleteHousehold,
            onConfirmDelete = onConfirmDelete,
            onCancelDelete = onCancelDelete,
        )
    }
}

@Preview
@Composable
private fun SplitzeeAppPreview() {
    SplitzeeApp(state = HouseholdUiState(home = HomeUiState.NoHousehold))
}
