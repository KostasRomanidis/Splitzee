package com.kromanid.splitzee.ui.states

import com.kromanid.splitzee.models.Household
import com.kromanid.splitzee.models.HouseholdError

data class HouseholdUiState(
    val home: HomeUiState = HomeUiState.Loading,
    val households: List<Household> = emptyList(),
    val selected: Household? = null,
    val showCreateDialog: Boolean = false,
    val nameError: HouseholdError? = null,
)
