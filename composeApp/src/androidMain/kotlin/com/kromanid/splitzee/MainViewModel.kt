package com.kromanid.splitzee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kromanid.splitzee.models.Category
import com.kromanid.splitzee.models.FixedCategory
import com.kromanid.splitzee.models.HouseholdError
import com.kromanid.splitzee.models.Money
import com.kromanid.splitzee.models.SplitRule
import com.kromanid.splitzee.ui.states.HomeUiState
import com.kromanid.splitzee.ui.states.HouseholdUiState
import com.kromanid.splitzee.ui.states.SplitzeeUIState
import com.kromanid.splitzee.usecases.AddExpenseResult
import com.kromanid.splitzee.usecases.AddExpenseUC
import com.kromanid.splitzee.usecases.CreateHouseholdResult
import com.kromanid.splitzee.usecases.CreateHouseholdUC
import com.kromanid.splitzee.usecases.GetCurrentHouseholdUC
import com.kromanid.splitzee.usecases.GetExpensesUC
import com.kromanid.splitzee.usecases.GetHouseholdsUC
import com.kromanid.splitzee.usecases.GetMembersUC
import com.kromanid.splitzee.usecases.SelectHouseholdUC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val addExpenseUC: AddExpenseUC,
    private val getExpensesUC: GetExpensesUC,
    private val getMembersUC: GetMembersUC,
    private val createHouseholdUC: CreateHouseholdUC,
    private val getHouseholdsUC: GetHouseholdsUC,
    private val getCurrentHouseholdUC: GetCurrentHouseholdUC,
    private val selectHouseholdUC: SelectHouseholdUC,
) : ViewModel() {

    private val _uiState: MutableStateFlow<SplitzeeUIState> =
        MutableStateFlow(SplitzeeUIState())
    val uiState: StateFlow<SplitzeeUIState> = _uiState.asStateFlow()

    private val showCreateDialog = MutableStateFlow(false)
    private val nameError = MutableStateFlow<HouseholdError?>(null)

    val householdState: StateFlow<HouseholdUiState> = combine(
        getCurrentHouseholdUC.execute(),
        getHouseholdsUC.execute(),
        showCreateDialog,
        nameError,
    ) { current, households, showDialog, error ->
        HouseholdUiState(
            home = if (current == null) HomeUiState.NoHousehold else HomeUiState.Empty,
            households = households,
            selected = current,
            showCreateDialog = showDialog,
            nameError = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HouseholdUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getExpensesUC.execute().collectLatest { expenses ->
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
            val members = getMembersUC.execute().first().map { it.id }
            // No household yet: the spec forbids creating one implicitly, and there is no household UI.
            if (members.isEmpty()) return@launch println("No household: create one before adding expenses")
            val result = addExpenseUC.execute(
                description = _uiState.value.description,
                amount = Money(_uiState.value.expense),
                category = Category.Fixed(FixedCategory.Other),
                paidBy = members.first(),
                split = SplitRule.Equal(members),
            )
            if (result is AddExpenseResult.Invalid) println("Invalid expense: ${result.errors}")
        }
    }

    fun openCreateHousehold() {
        nameError.value = null
        showCreateDialog.value = true
    }

    fun closeCreateHousehold() {
        nameError.value = null
        showCreateDialog.value = false
    }

    fun clearNameError() {
        nameError.value = null
    }

    fun createHousehold(name: String) {
        viewModelScope.launch {
            when (val result = createHouseholdUC.execute(name)) {
                is CreateHouseholdResult.Invalid -> nameError.value = result.error
                // The new household is selected by the repo, so householdState switches screens on its own.
                is CreateHouseholdResult.Success -> closeCreateHousehold()
            }
        }
    }

    fun selectHousehold(id: String) {
        viewModelScope.launch { selectHouseholdUC.execute(id) }
    }
}
