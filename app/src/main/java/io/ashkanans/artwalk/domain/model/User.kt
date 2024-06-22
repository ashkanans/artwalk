package io.ashkanans.artwalk.domain.model

data class User(
    val username: String = "",
    val id: Int = 0,
    val name: String = "",
    val sirname: String = "",
    val token: String = "",
    val phone_number: String = ""
)