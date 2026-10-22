package com.kromanid.splitzee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kromanid.splitzee.ui.SplitzeeApp
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = mainViewModel // created eagerly so the expenses flow logs at launch
        setContent {
            val state by viewModel.householdState.collectAsStateWithLifecycle()
            SplitzeeApp(
                state = state,
                // shortcut: fixed test expense, the add-expense change brings the input UI
                onAddExpense = {
                    viewModel.setDescription("Test expense")
                    viewModel.setAmount("1000")
                    viewModel.addExpense()
                },
                onNameChange = viewModel::clearNameError,
                onCreateHousehold = viewModel::createHousehold,
            )
        }
    }
}