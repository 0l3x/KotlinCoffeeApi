package edu.olexandergalaktionov.apirestcoffee.model

import com.google.gson.annotations.SerializedName

sealed class LoginState {
    object Idle : LoginState() // Estado inactivo (esperando acción del usuario)
    object Loading : LoginState() // Estado cargando (esperando respuesta del servidor)
    data class Success(val response: LoginResponse) : LoginState() // Estado éxito
    data class Error(val message: String) : LoginState() // Estado error
}

/**
 * LoginRequest, data class para la petición del login, con usuario y contraseña.
 * Deben serializarse los campos para que coincidan con los campos del servidor.
 */
data class LoginRequest(
    @SerializedName("usuario")
    val user: String,
    @SerializedName("password")
    val password: String
)

/**
 * LoginResponse, data class para la respuesta del login.
 */
data class LoginResponse(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("token") val token: String?,
    @SerializedName("message") val message: String?,
    val username: String
)
