package edu.olexandergalaktionov.apirestcoffee.model

import com.google.gson.annotations.SerializedName

data class CoffeeList(
    val id: Int,
    @SerializedName("coffee_name")
    val coffeeName: String,
    val comments: String
)