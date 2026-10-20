package com.kromanid.splitzee.models

typealias MemberId = String

data class Household(val id: String, val name: String, val currency: String)

data class Member(val id: MemberId, val name: String)
