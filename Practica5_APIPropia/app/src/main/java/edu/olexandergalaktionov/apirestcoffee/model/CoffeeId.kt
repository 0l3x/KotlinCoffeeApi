package edu.olexandergalaktionov.apirestcoffee.model

import com.google.gson.annotations.SerializedName

data class CoffeeId(
    val id: Int,
    @SerializedName("coffee_name")
    val coffeeName: String,
    @SerializedName("coffee_desc")
    val coffeeDesc: String
)
