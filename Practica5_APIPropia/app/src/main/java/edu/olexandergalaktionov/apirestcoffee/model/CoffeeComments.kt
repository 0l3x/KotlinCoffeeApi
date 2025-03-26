package edu.olexandergalaktionov.apirestcoffee.model

data class CoffeeComments(
    val id: Int,
    val idCoffee: Int,
    val user: String,
    val comment: String
)