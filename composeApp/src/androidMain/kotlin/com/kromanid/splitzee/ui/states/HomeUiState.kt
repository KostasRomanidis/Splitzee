package com.kromanid.splitzee.ui.states

/**
 * A class the
 */
sealed interface HomeUiState {
    /**
     * Represents loading state
     */
    data object Loading : HomeUiState

    /**
     * Represents empty state (no expenses)
     */
    data object Empty : HomeUiState

    /**
     * Represents a list of expenses on home screen
     */
    data class Expenses(
        val expenses: List<Any>
    ) : HomeUiState
}