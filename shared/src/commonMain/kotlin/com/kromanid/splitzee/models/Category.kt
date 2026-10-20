package com.kromanid.splitzee.models

enum class FixedCategory { Groceries, Rent, Utilities, Household, Transport, Dining, Entertainment, Other }

sealed interface Category {
    data class Fixed(val category: FixedCategory) : Category
    data class Custom(val name: String) : Category
}
