package com.kromanid.splitzee.models

typealias MemberId = String

/** [isCreator] is true when the local user created the household; only they may delete it. */
data class Household(val id: String, val name: String, val currency: String, val isCreator: Boolean)

data class Member(val id: MemberId, val name: String)
