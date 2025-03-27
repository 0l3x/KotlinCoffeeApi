package edu.olexandergalaktionov.apirestcoffee.model

import com.google.gson.annotations.SerializedName
/**
 * Class CoffeeId.kt
 *
 * Data class that represents the detailed information of a single coffee item.
 *
 * @property id Unique identifier of the coffee.
 * @property coffeeName Name of the coffee.
 * @property coffeeDesc Description of the coffee in HTML format.
 *
 * The field names are annotated to match the JSON keys from the server response.
 *
 * @author Olexandr Galaktionov Tsisar
 */
data class CoffeeId(
    val id: Int,
    @SerializedName("coffee_name")
    val coffeeName: String,
    @SerializedName("coffee_desc")
    val coffeeDesc: String
)
