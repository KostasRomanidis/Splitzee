package com.kromanid.splitzee

import androidx.lifecycle.ViewModel
import com.kromanid.splitzee.usecases.AddExpenseUC
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(
    private val addExpenseUC: AddExpenseUC
) : ViewModel() {
    private val _greetingList: MutableStateFlow<String> = MutableStateFlow("Welcome to Splitzee!")
    val greetings: StateFlow<String> = _greetingList.asStateFlow()

    fun addExpense() {
        addExpenseUC.execute(10L)
    }
}