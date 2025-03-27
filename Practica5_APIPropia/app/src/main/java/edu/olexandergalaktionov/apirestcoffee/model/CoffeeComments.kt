package edu.olexandergalaktionov.apirestcoffee.model
/**
 * Class CoffeeComments.kt
 *
 * Data class that represents a comment associated with a specific coffee.
 *
 * @property id Unique identifier of the comment.
 * @property idCoffee ID of the coffee the comment belongs to.
 * @property user Name of the user who posted the comment.
 * @property comment The actual comment text.
 *
 * @author Olexandr Galaktionov Tsisar
 */
data class CoffeeComments(
    val id: Int,
    val idCoffee: Int,
    val user: String,
    val comment: String
)