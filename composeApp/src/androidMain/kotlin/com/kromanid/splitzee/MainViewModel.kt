package com.kromanid.splitzee

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _greetingList: MutableStateFlow<String> = MutableStateFlow("Welcome to Splitzee!")
    val greetings: StateFlow<String> = _greetingList.asStateFlow()
}