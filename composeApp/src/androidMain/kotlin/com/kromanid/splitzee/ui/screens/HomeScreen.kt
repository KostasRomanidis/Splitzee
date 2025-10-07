package com.kromanid.splitzee.ui.screens

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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kromanid.splitzee.ui.states.HomeUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    addExpense: () -> Unit
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Splitzee") }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = addExpense,
                modifier = Modifier,
                shape = FloatingActionButtonDefaults.largeShape,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add expense") },
                text = { Text("Add expense") }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { paddingValues ->
        when (uiState) {
            HomeUiState.Loading -> LoadingHomeScreen(modifier = Modifier.padding(paddingValues))
            HomeUiState.Empty -> EmptyHomeScreen(modifier = Modifier.padding(paddingValues))
            is HomeUiState.Expenses -> ExpensesHomeScreen(
                modifier = Modifier.padding(paddingValues),
                uiState.expenses
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
    HomeScreen(uiState = HomeUiState.Empty) {}
}