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

        setContent {
            val uiState = mainViewModel.uiState.collectAsStateWithLifecycle().value

            SplitzeeApp(
                uiState = uiState,
                onAddAmount = mainViewModel::setAmount,
                onDescriptionChange = mainViewModel::setDescription,
                onAddExpense = mainViewModel::addExpense,
            )
        }
    }
}