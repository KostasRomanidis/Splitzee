package com.kromanid.splitzee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kromanid.splitzee.ui.states.SplitzeeUIState
import com.kromanid.splitzee.usecases.AddExpenseUC
import com.kromanid.splitzee.usecases.GetExpensesUC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val addExpenseUC: AddExpenseUC,
    private val getExpensesUC: GetExpensesUC
) : ViewModel() {

    private val _uiState: MutableStateFlow<SplitzeeUIState> =
        MutableStateFlow(SplitzeeUIState())
    val uiState: StateFlow<SplitzeeUIState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getExpensesUC.getExpenses().collectLatest { expenses ->
                println("Get Expenses: $expenses")
            }
        }
    }

    fun setDescription(description: String) {
        _uiState.update {
            it.copy(description = description)
        }
    }

    fun setAmount(amount: String) {
        _uiState.update {
            it.copy(expense = amount.toLongOrNull() ?: 0L)
        }
    }

    fun addExpense() {
        viewModelScope.launch(Dispatchers.IO) {
            println("Expense: ${_uiState.value.description} = ${_uiState.value.expense}")
            addExpenseUC.execute(_uiState.value.description, _uiState.value.expense)
        }
    }
}