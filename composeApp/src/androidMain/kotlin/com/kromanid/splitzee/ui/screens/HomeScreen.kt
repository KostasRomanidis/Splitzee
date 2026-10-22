package com.kromanid.splitzee.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kromanid.splitzee.models.Household
import com.kromanid.splitzee.ui.states.HomeUiState
import com.kromanid.splitzee.ui.states.HouseholdUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HouseholdUiState,
    addExpense: () -> Unit,
    onNameChange: () -> Unit,
    onCreateHousehold: (String) -> Unit,
    onSelectHousehold: (String) -> Unit,
    onNewHousehold: () -> Unit,
    onCancelCreate: () -> Unit,
) {
    val uiState = state.home
    if (state.showCreateDialog) {
        Dialog(onDismissRequest = onCancelCreate) {
            Surface(shape = MaterialTheme.shapes.extraLarge) {
                CreateHouseholdForm(
                    title = "New household",
                    nameError = state.nameError,
                    onNameChange = onNameChange,
                    onCreate = onCreateHousehold,
                    onCancel = onCancelCreate,
                )
            }
        }
    }
    Scaffold(
        modifier = Modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val selected = state.selected
                    if (selected == null) {
                        Text("Splitzee")
                    } else {
                        HouseholdMenu(
                            selected = selected,
                            households = state.households,
                            onSelect = onSelectHousehold,
                            onNewHousehold = onNewHousehold,
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState != HomeUiState.NoHousehold) {
                ExtendedFloatingActionButton(
                    onClick = addExpense,
                    modifier = Modifier,
                    shape = FloatingActionButtonDefaults.largeShape,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add expense") },
                    text = { Text("Add expense") }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { paddingValues ->
        when (uiState) {
            HomeUiState.Loading -> LoadingHomeScreen(modifier = Modifier.padding(paddingValues))
            HomeUiState.NoHousehold -> CreateHouseholdForm(
                title = "Create your first household",
                nameError = state.nameError,
                onNameChange = onNameChange,
                onCreate = onCreateHousehold,
                modifier = Modifier.padding(paddingValues),
            )
            HomeUiState.Empty -> EmptyHomeScreen(modifier = Modifier.padding(paddingValues))
            is HomeUiState.Expenses -> ExpensesHomeScreen(
                modifier = Modifier.padding(paddingValues),
                uiState.expenses
            )
        }

    }
}

@Composable
private fun HouseholdMenu(
    selected: Household,
    households: List<Household>,
    onSelect: (String) -> Unit,
    onNewHousehold: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .clickable(onClickLabel = "Switch household") { expanded = true }
                .testTag("householdMenu"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(selected.name)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            households.forEach { household ->
                DropdownMenuItem(
                    text = { Text(household.name) },
                    trailingIcon = {
                        if (household.id == selected.id) Icon(Icons.Default.Check, contentDescription = "Selected")
                    },
                    onClick = {
                        expanded = false
                        onSelect(household.id)
                    },
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("New household") },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = {
                    expanded = false
                    onNewHousehold()
                },
            )
        }
    }
}

@Composable
private fun LoadingHomeScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.testTag("loading"))
    }
}

@Composable
private fun EmptyHomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .semantics {
                contentDescription = "No expenses yet"
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No expenses yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.W300
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "When you add your first expense, it will appear here.\nTap the + button to get started!",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ExpensesHomeScreen(modifier: Modifier = Modifier, expenses: List<Any>) {}


@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        state = HouseholdUiState(home = HomeUiState.Empty),
        addExpense = {},
        onNameChange = {},
        onCreateHousehold = {},
        onSelectHousehold = {},
        onNewHousehold = {},
        onCancelCreate = {},
    )
}