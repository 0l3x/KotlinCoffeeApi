package edu.olexandergalaktionov.apirestcoffee.model

import com.google.gson.annotations.SerializedName
/**
 * Class CoffeeList.kt
 *
 * Data class representing a coffee item displayed in the main list.
 *
 * @property id Unique identifier of the coffee.
 * @property coffeeName Name of the coffee.
 * @property comments Number of comments associated with the coffee, returned as a string.
 *
 * Fields are annotated with @SerializedName to match the JSON keys from the API.
 *
 * @author Olexandr Galaktionov Tsisar
 */
data class CoffeeList(
    val id: Int,
    @SerializedName("coffee_name")
    val coffeeName: String,
    val comments: String
)